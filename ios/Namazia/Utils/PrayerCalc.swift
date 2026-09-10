import Foundation

/// A prayer paired with the moment it happens.
struct ScheduledPrayer: Equatable, Identifiable {
    let prayer: PrayerName
    /// `"HH:mm"` as stored.
    let clock: String
    /// The same time as an absolute instant, for countdowns and notifications.
    let date: Date

    var id: String { "\(prayer.rawValue)-\(clock)" }
}

/// The Swift half of `utils/PrayerCalc.kt`.
extension DayPrayerTimes {

    /// This day's prayers in chronological order, resolved to absolute instants.
    /// Entries whose time cannot be parsed are dropped rather than crashing — a
    /// malformed row from a future API change should cost one line, not the screen.
    var schedule: [ScheduledPrayer] {
        PrayerName.allCases.compactMap { prayer in
            let clock = time(for: prayer)
            guard let date = AppTime.instant(day: day, clock: clock) else { return nil }
            return ScheduledPrayer(prayer: prayer, clock: clock, date: date)
        }
    }

    /// Only the prayers the user has switched on.
    func enabledSchedule(_ settings: AppSettings) -> [ScheduledPrayer] {
        schedule.filter { settings.isEnabled($0.prayer) }
    }

    /// The next enabled prayer strictly after `now`, or `nil` once the day is done —
    /// in which case the caller moves to tomorrow's row.
    func upcoming(_ settings: AppSettings, now: Date = Date()) -> ScheduledPrayer? {
        enabledSchedule(settings).first { $0.date > now }
    }

    /// The prayer currently in effect: the last one whose time has passed.
    func current(_ settings: AppSettings, now: Date = Date()) -> ScheduledPrayer? {
        enabledSchedule(settings).last { $0.date <= now }
    }

    var isToday: Bool { day == AppTime.dayKey() }
}

extension ScheduledPrayer {
    /// Seconds until this prayer, never negative.
    func secondsAway(from now: Date = Date()) -> Int {
        max(0, Int(date.timeIntervalSince(now)))
    }
}

/// Formats a countdown as ۰۲:۱۴:۰۹ — hours only when there are any, so the common case
/// reads as minutes and seconds rather than a wall of zeroes.
func countdownText(seconds: Int) -> String {
    let total = max(0, seconds)
    let hours = total / 3600
    let minutes = (total % 3600) / 60
    let secs = total % 60

    let text = hours > 0
        ? String(format: "%d:%02d:%02d", hours, minutes, secs)
        : String(format: "%02d:%02d", minutes, secs)

    return text.persianDigits
}
