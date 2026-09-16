import Foundation
import UserNotifications

/// Schedules the azan.
///
/// This is the one place where iOS cannot do what Android does. Android arms an exact
/// alarm and plays the full azan through a foreground service. iOS has no equivalent:
/// with the app closed, the only way to make a sound at a given moment is a local
/// notification that was scheduled *in advance*. So the app keeps a rolling window of
/// pending notifications and re-arms it every time it runs.
///
/// Two limits shape everything here:
///   * **64 pending notifications**, app-wide. The system silently drops the rest.
///   * **30 seconds** of sound, from a CAF/WAV/AIFF file inside the bundle.
@MainActor
final class NotificationScheduler: ObservableObject {

    @Published private(set) var authorization: UNAuthorizationStatus = .notDetermined

    /// False until the short azan files have been generated into the bundle — see the
    /// `afconvert` step in `ios/README.md`. Without them iOS falls back to its own
    /// chime, which still tells you a prayer has arrived but is not the azan.
    let hasAzanSound: Bool

    private let center = UNUserNotificationCenter.current()

    /// Four short of the system's 64 so that a notification scheduled from elsewhere —
    /// a future widget, a test — never pushes a prayer out of the queue.
    private let budget = 60

    init() {
        hasAzanSound = Sound.azan.isBundled && Sound.fajr.isBundled
    }

    // MARK: - Permission

    var isAllowed: Bool {
        authorization == .authorized || authorization == .provisional || authorization == .ephemeral
    }

    func refreshAuthorization() async {
        authorization = await center.notificationSettings().authorizationStatus
    }

    /// Asks once. Re-asking after a refusal does nothing — iOS only ever shows the
    /// system prompt a single time, which is why the UI offers a route to Settings
    /// instead of a second prompt.
    func requestAuthorizationIfNeeded() async {
        await refreshAuthorization()
        guard authorization == .notDetermined else { return }

        _ = try? await center.requestAuthorization(options: [.alert, .sound, .badge])
        await refreshAuthorization()
    }

    // MARK: - Scheduling

    /// Replaces the whole pending queue with the next prayers from `days`.
    ///
    /// Replace rather than patch: prayer times, which prayers are on, and the reminder
    /// offset can all have changed since the last run, and reconciling that in place is
    /// far more error-prone than rebuilding a list of at most sixty items.
    func reschedule(days: [DayPrayerTimes], settings: AppSettings, now: Date = Date()) async {
        await refreshAuthorization()
        guard isAllowed else {
            center.removeAllPendingNotificationRequests()
            return
        }

        let requests = plan(days: days, settings: settings, now: now)

        center.removeAllPendingNotificationRequests()
        for request in requests {
            try? await center.add(request)
        }
    }

    func cancelAll() {
        center.removeAllPendingNotificationRequests()
    }

    /// Builds the queue: everything still ahead of us, in time order, until the budget
    /// runs out.
    ///
    /// Taking them chronologically means the budget is spent on the days closest to
    /// now, and the far end of the window is simply not scheduled yet — it will be, the
    /// next time the app runs. That is the right trade: a reminder eight days out is
    /// worth nothing if it costs tomorrow's Fajr.
    private func plan(days: [DayPrayerTimes], settings: AppSettings, now: Date) -> [UNNotificationRequest] {
        var planned: [(date: Date, request: UNNotificationRequest)] = []

        for day in days.sorted(by: { $0.day < $1.day }) {
            for entry in day.enabledSchedule(settings) where entry.prayer.callsAzan {
                if entry.date > now {
                    planned.append((entry.date, azanRequest(day: day.day, entry: entry)))
                }

                // A reminder before the azan, so there is time to get ready. Zero
                // minutes means the user turned it off.
                let offset = TimeInterval(settings.reminderMinutes * 60)
                if offset > 0 {
                    let remindAt = entry.date.addingTimeInterval(-offset)
                    if remindAt > now {
                        planned.append((
                            remindAt,
                            reminderRequest(
                                day: day.day,
                                entry: entry,
                                at: remindAt,
                                minutes: settings.reminderMinutes
                            )
                        ))
                    }
                }
            }
        }

        return planned
            .sorted { $0.date < $1.date }
            .prefix(budget)
            .map(\.request)
    }

    // MARK: - Requests

    private func azanRequest(day: String, entry: ScheduledPrayer) -> UNNotificationRequest {
        let content = UNMutableNotificationContent()
        content.title = "notif.azan.title".localized(entry.prayer.localizedName)
        content.body = "notif.azan.body".localized(entry.prayer.localizedName, entry.clock.persianDigits)
        content.sound = sound(for: entry.prayer)
        content.threadIdentifier = day
        content.userInfo = [
            Keys.kind: Kind.azan,
            Keys.prayer: entry.prayer.rawValue
        ]
        // A prayer time is the textbook case for this level: it is tied to a moment
        // that has already passed by the time a summary would deliver it. It needs the
        // Time Sensitive Notifications capability to take effect; without it iOS
        // quietly treats the notification as ordinary rather than failing.
        content.interruptionLevel = .timeSensitive

        return UNNotificationRequest(
            identifier: "azan-\(day)-\(entry.prayer.rawValue)",
            content: content,
            trigger: trigger(at: entry.date)
        )
    }

    private func reminderRequest(
        day: String,
        entry: ScheduledPrayer,
        at date: Date,
        minutes: Int
    ) -> UNNotificationRequest {
        let content = UNMutableNotificationContent()
        content.title = "notif.reminder.title".localized(minutes.persianDigits, entry.prayer.localizedName)
        content.body = "notif.reminder.body".localized(entry.prayer.localizedName, entry.clock.persianDigits)
        // Deliberately not the azan: the azan belongs to the prayer time itself, and
        // hearing it early is worse than a plain chime.
        content.sound = .default
        content.threadIdentifier = day
        content.userInfo = [
            Keys.kind: Kind.reminder,
            Keys.prayer: entry.prayer.rawValue
        ]
        content.interruptionLevel = .timeSensitive

        return UNNotificationRequest(
            identifier: "reminder-\(day)-\(entry.prayer.rawValue)",
            content: content,
            trigger: trigger(at: date)
        )
    }

    /// A calendar trigger, not an interval one: the system then resolves the moment
    /// against the calendar and the stated timezone, so a phone that travels — or a
    /// timezone whose rules change — still fires at the right Kabul wall-clock time.
    private func trigger(at date: Date) -> UNCalendarNotificationTrigger {
        var components = AppTime.calendar.dateComponents(
            [.year, .month, .day, .hour, .minute],
            from: date
        )
        components.timeZone = AppTime.zone
        return UNCalendarNotificationTrigger(dateMatching: components, repeats: false)
    }

    private func sound(for prayer: PrayerName) -> UNNotificationSound {
        let file = prayer == .fajr ? Sound.fajr : Sound.azan
        guard file.isBundled else { return .default }
        return UNNotificationSound(named: UNNotificationSoundName(file.rawValue))
    }

    // MARK: - Names

    /// The 30-second files, generated on a Mac with `afconvert` (see `ios/README.md`).
    enum Sound: String {
        case azan = "azan30.caf"
        case fajr = "azan_fajr30.caf"

        var isBundled: Bool {
            Bundle.main.url(forResource: rawValue, withExtension: nil) != nil
        }
    }

    enum Keys {
        static let kind = "kind"
        static let prayer = "prayer"
    }

    enum Kind {
        static let azan = "azan"
        static let reminder = "reminder"
    }
}

#if DEBUG
extension NotificationScheduler {
    /// Fires a real azan notification a few seconds from now, so the whole path —
    /// permission, sound file, interruption level — can be checked without waiting for
    /// a prayer. Debug builds only.
    func scheduleTest(prayer: PrayerName = .maghrib, after seconds: TimeInterval = 8) async {
        await requestAuthorizationIfNeeded()
        guard isAllowed else { return }

        let content = UNMutableNotificationContent()
        content.title = "notif.test.title".localized(prayer.localizedName)
        content.body = (hasAzanSound ? "notif.test.ok" : "notif.test.noSound").localized
        content.sound = sound(for: prayer)
        content.userInfo = [Keys.kind: Kind.azan, Keys.prayer: prayer.rawValue]
        content.interruptionLevel = .timeSensitive

        let request = UNNotificationRequest(
            identifier: "azan-test",
            content: content,
            trigger: UNTimeIntervalNotificationTrigger(timeInterval: seconds, repeats: false)
        )
        try? await center.add(request)
    }
}
#endif
