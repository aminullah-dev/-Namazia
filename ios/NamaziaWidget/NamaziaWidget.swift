import WidgetKit
import SwiftUI

// MARK: - Timeline

struct PrayerEntry: TimelineEntry {
    let date: Date
    let cityName: String
    let hijriDate: String
    let rows: [PrayerRow]
    /// Nil once the day's prayers are done and tomorrow is not cached.
    let next: ScheduledPrayer?
    /// False when the widget cannot see the app's data at all.
    let hasData: Bool

    static func placeholder(_ date: Date = Date()) -> PrayerEntry {
        PrayerEntry(
            date: date,
            cityName: AfghanCities.default.nameDari,
            hijriDate: "",
            rows: [],
            next: nil,
            hasData: false
        )
    }
}

/// Feeds the widget from the cache the app fills.
///
/// The widget never touches the network. It cannot ask the user for anything, it runs
/// on a budget the system controls, and prayer times for a cached day never change —
/// so reading what the app already fetched is both cheaper and more predictable.
struct PrayerProvider: TimelineProvider {

    func placeholder(in context: Context) -> PrayerEntry {
        .placeholder()
    }

    func getSnapshot(in context: Context, completion: @escaping (PrayerEntry) -> Void) {
        Task {
            completion(await entry(at: Date()) ?? .placeholder())
        }
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<PrayerEntry>) -> Void) {
        Task {
            let now = Date()
            let settings = AppSettings.load(from: AppGroup.defaults)
            let days = await cachedDays(settings: settings, from: now)

            guard !days.isEmpty else {
                // Nothing cached: show the "open the app" state and try again in an
                // hour rather than hammering a cache that only the app can fill.
                let entry = PrayerEntry.placeholder(now)
                completion(Timeline(entries: [entry], policy: .after(now.addingTimeInterval(3600))))
                return
            }

            // One entry now, then one at each prayer time — the moments when the
            // "next prayer" actually changes. Between them the countdown is drawn by
            // SwiftUI's own timer text, which needs no entries at all.
            var moments: [Date] = [now]
            for day in days {
                for entry in day.enabledSchedule(settings) where entry.date > now {
                    moments.append(entry.date)
                }
            }
            moments = Array(moments.prefix(40))

            var entries: [PrayerEntry] = []
            for moment in moments {
                if let entry = await self.entry(at: moment, days: days, settings: settings) {
                    entries.append(entry)
                }
            }

            let refreshAt = moments.last?.addingTimeInterval(60) ?? now.addingTimeInterval(3600)
            completion(Timeline(entries: entries, policy: .after(refreshAt)))
        }
    }

    // MARK: - Reading the cache

    private func cachedDays(settings: AppSettings, from now: Date) async -> [DayPrayerTimes] {
        let cache = PrayerTimesCache()
        var days: [DayPrayerTimes] = []

        // Two days is all a widget needs: today for the list, tomorrow so that the
        // hours after Isha still count down to Fajr instead of going blank.
        for offset in 0...1 {
            let key = AppTime.dayKey(byAddingDays: offset, to: now)
            if let day = await cache.get(day: key, city: settings.city.nameEn) {
                days.append(day)
            }
        }
        return days
    }

    private func entry(at date: Date) async -> PrayerEntry? {
        let settings = AppSettings.load(from: AppGroup.defaults)
        let days = await cachedDays(settings: settings, from: date)
        return await entry(at: date, days: days, settings: settings)
    }

    private func entry(
        at date: Date,
        days: [DayPrayerTimes],
        settings: AppSettings
    ) async -> PrayerEntry? {
        let key = AppTime.dayKey(date)
        guard let today = days.first(where: { $0.day == key }) ?? days.first else { return nil }

        // Past the last prayer of the day, the countdown belongs to tomorrow's Fajr.
        let next = today.upcoming(settings, now: date)
            ?? days.first { $0.day > key }?.enabledSchedule(settings).first

        return PrayerEntry(
            date: date,
            cityName: settings.city.nameDari,
            hijriDate: today.hijriDate.persianDigits,
            rows: today.rows(settings, now: date),
            next: next,
            hasData: true
        )
    }
}

// MARK: - Widget

@main
struct NamaziaWidgetBundle: WidgetBundle {
    var body: some Widget {
        NamaziaWidget()
    }
}

struct NamaziaWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "af.namazia.app.widget", provider: PrayerProvider()) { entry in
            PrayerWidgetView(entry: entry)
        }
        .configurationDisplayName("اوقات نماز")
        .description("وقت نماز بعدی و اوقات امروز")
        .supportedFamilies([
            .systemSmall,
            .systemMedium,
            .accessoryRectangular,
            .accessoryCircular
        ])
    }
}
