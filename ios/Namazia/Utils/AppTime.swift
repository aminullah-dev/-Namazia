import Foundation

/// Everything date- and time-shaped in this app happens in Kabul, mirroring
/// `utils/PrayerCalc.kt`'s `APP_ZONE`.
///
/// Prayer times arrive from the API as Afghanistan wall-clock times, so "today" and
/// "has this prayer passed?" must be answered in that zone regardless of where the
/// phone thinks it is — an Afghan in Germany still wants to see Kabul's times when the
/// city is set to Kabul.
///
/// The `en_US_POSIX` locale on every formatter is not decoration. On a device whose
/// region is Afghanistan or Iran, the default locale's calendar can be Hijri, and
/// `yyyy-MM-dd` would then format as ۱۴۰۴-۰۶-۱۹ — silently poisoning every cache key.
enum AppTime {
    static let zone = TimeZone(identifier: "Asia/Kabul") ?? TimeZone(secondsFromGMT: 16200)!

    static let calendar: Calendar = {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = zone
        calendar.locale = Locale(identifier: "en_US_POSIX")
        return calendar
    }()

    /// `yyyy-MM-dd` — the cache key format and the API's day identity.
    static let dayFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.calendar = Calendar(identifier: .gregorian)
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.timeZone = zone
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter
    }()

    /// `HH:mm` — how the API gives prayer times and how they are stored.
    static let clockFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.calendar = Calendar(identifier: .gregorian)
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.timeZone = zone
        formatter.dateFormat = "HH:mm"
        return formatter
    }()

    static func dayKey(_ date: Date = Date()) -> String {
        dayFormatter.string(from: date)
    }

    static func date(fromDayKey key: String) -> Date? {
        dayFormatter.date(from: key)
    }

    static func dayKey(byAddingDays days: Int, to date: Date = Date()) -> String {
        let shifted = calendar.date(byAdding: .day, value: days, to: date) ?? date
        return dayKey(shifted)
    }

    /// Resolves a stored `"HH:mm"` on a stored `"yyyy-MM-dd"` to an absolute instant.
    ///
    /// Built from date components rather than by string concatenation so that a DST
    /// change — which Afghanistan does not observe today, but has before — would be
    /// handled by the calendar instead of producing a wrong instant.
    static func instant(day: String, clock: String) -> Date? {
        guard let dayDate = date(fromDayKey: day) else { return nil }

        let parts = clock.split(separator: ":")
        guard parts.count >= 2,
              let hour = Int(parts[0]),
              let minute = Int(parts[1]) else { return nil }

        var components = calendar.dateComponents([.year, .month, .day], from: dayDate)
        components.hour = hour
        components.minute = minute
        components.second = 0
        components.timeZone = zone

        return calendar.date(from: components)
    }
}
