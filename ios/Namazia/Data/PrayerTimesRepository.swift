import Foundation

/// Cache-first access to prayer times, mirroring `data/PrayerTimesRepository.kt`.
///
/// The order matters: a day already in the cache is returned without touching the
/// network, and a network failure falls back to the cache before it becomes an error.
/// Prayer times for a fixed city and settings never change, so a cached day is not
/// stale data — it is the same answer, faster and offline.
final class PrayerTimesRepository {

    private let api: PrayerTimesAPI
    private let cache: PrayerTimesCache

    init(api: PrayerTimesAPI = PrayerTimesAPI(), cache: PrayerTimesCache = PrayerTimesCache()) {
        self.api = api
        self.cache = cache
    }

    /// Times for one day. Throws an `AppError` only when neither the network nor the
    /// cache can answer.
    func prayerTimes(
        day: String = AppTime.dayKey(),
        city: AfghanCity,
        method: Int,
        school: Int
    ) async throws -> DayPrayerTimes {
        if let cached = await cache.get(day: day, city: city.nameEn) {
            return cached
        }

        do {
            let data = try await api.timings(day: day, city: city, method: method, school: school)
            let times = Self.convert(data, day: day, city: city.nameEn)
            await cache.put(times)
            return times
        } catch {
            throw AppError.classify(error)
        }
    }

    /// Warms the cache for the coming week so the app keeps working offline and the
    /// notification scheduler has days to schedule.
    ///
    /// Failures are deliberately swallowed: this runs in the background and a missing
    /// future day is not something to interrupt the user about — the day is fetched
    /// again when it is actually needed.
    func prefetchWeek(city: AfghanCity, method: Int, school: Int) async {
        for offset in 0..<7 {
            let day = AppTime.dayKey(byAddingDays: offset)
            _ = try? await prayerTimes(day: day, city: city, method: method, school: school)
        }
        await cache.prune()
    }

    /// A whole month in one request. The rows are written into the same cache the daily
    /// lookup reads, so opening the calendar also warms the home screen.
    func monthlyCalendar(
        year: Int,
        month: Int,
        city: AfghanCity,
        method: Int,
        school: Int
    ) async throws -> [DayPrayerTimes] {
        do {
            let data = try await api.monthlyCalendar(
                year: year,
                month: month,
                city: city,
                method: method,
                school: school
            )
            let days = data.compactMap { entry -> DayPrayerTimes? in
                guard let day = Self.dayKey(fromApiDate: entry.date.gregorian.date) else { return nil }
                return Self.convert(entry, day: day, city: city.nameEn)
            }
            guard !days.isEmpty else { throw AppError.badResponse(detail: "empty calendar") }

            await cache.put(days)
            return days
        } catch {
            // The calendar is browsable offline for any month already cached.
            let cached = await cache.days(for: city.nameEn)
                .filter { Self.matches(day: $0.day, year: year, month: month) }
            if !cached.isEmpty { return cached }
            throw AppError.classify(error)
        }
    }

    /// Everything cached for a city, for the calendar screen's offline path.
    func cachedDays(city: AfghanCity) async -> [DayPrayerTimes] {
        await cache.days(for: city.nameEn)
    }

    /// Call whenever the calculation method or fiqh school changes — every cached day
    /// was computed with the old ones and would otherwise be served as if still valid.
    func clearCache() async {
        await cache.clear()
    }

    // MARK: - Mapping

    private static func convert(_ data: PrayerData, day: String, city: String) -> DayPrayerTimes {
        DayPrayerTimes(
            day: day,
            city: city,
            fajr: clean(data.timings.fajr),
            sunrise: clean(data.timings.sunrise),
            dhuhr: clean(data.timings.dhuhr),
            asr: clean(data.timings.asr),
            maghrib: clean(data.timings.maghrib),
            isha: clean(data.timings.isha),
            hijriDate: "\(data.date.hijri.day) \(data.date.hijri.month.ar) \(data.date.hijri.year)",
            cachedAt: Date()
        )
    }

    /// The API returns `"04:12 (AFT)"` — everything after the space is a timezone label.
    private static func clean(_ time: String) -> String {
        String(time.split(separator: " ").first ?? "")
    }

    /// The calendar endpoint dates each row `"dd-MM-yyyy"`; the cache keys on
    /// `"yyyy-MM-dd"`.
    private static func dayKey(fromApiDate date: String) -> String? {
        let parts = date.split(separator: "-")
        guard parts.count == 3 else { return nil }
        return "\(parts[2])-\(parts[1])-\(parts[0])"
    }

    private static func matches(day: String, year: Int, month: Int) -> Bool {
        day.hasPrefix(String(format: "%04d-%02d", year, month))
    }
}
