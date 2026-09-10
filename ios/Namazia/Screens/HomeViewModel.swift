import Foundation
import WidgetKit

/// State for the home screen, ported from `HomeUiState` and the loading half of
/// `MainViewModel.kt`.
///
/// Errors are held as `AppError` codes, never as rendered sentences — the wording is
/// resolved at the view. And an error does not clear the times already on screen:
/// cached prayer times stay visible, labelled as coming from the cache, because times
/// the user can read are worth more than an empty screen.
@MainActor
final class HomeViewModel: ObservableObject {

    @Published private(set) var times: DayPrayerTimes?
    @Published private(set) var isLoading = false
    @Published private(set) var error: AppError?
    @Published private(set) var now = Date()

    private let services: AppServices

    /// `nil`, not `.shared`, as the default — a default argument is evaluated in a
    /// nonisolated context, where a main-actor isolated static property is off limits.
    init(services: AppServices? = nil) {
        self.services = services ?? .shared
    }

    var settings: AppSettings { services.settings.settings }
    var city: AfghanCity { settings.city }

    /// All of today's prayers, recomputed against the current second so "next" and
    /// "past" advance on their own without a reload.
    var rows: [PrayerRow] {
        times?.rows(settings, now: now) ?? []
    }

    var nextPrayer: PrayerRow? { rows.first { $0.isNext } }

    /// The prayer the countdown ring measures from — the most recent one that has
    /// passed, whether or not its azan is switched on.
    var previousPrayer: PrayerRow? { rows.last { $0.isPast } }

    var hijriDate: String { times?.hijriDate.persianDigits ?? "" }

    /// True while there is nothing at all to show — the only case that gets a skeleton.
    var isEmpty: Bool { times == nil }

    func load() async {
        guard !isLoading else { return }
        isLoading = true

        let settings = self.settings
        do {
            let loaded = try await services.repository.prayerTimes(
                day: AppTime.dayKey(),
                city: settings.city,
                method: settings.calculationMethod,
                school: settings.asrSchool
            )
            times = loaded
            error = nil

            // The widget reads the same cache but cannot fill it. Whenever the app
            // learns something new, the widget is told to redraw — otherwise it keeps
            // showing whatever it last managed to read, for hours.
            WidgetCenter.shared.reloadAllTimelines()

            // Fetch the coming week in the background, then arm the azan from it. This
            // is the only thing that keeps notifications alive: iOS cannot wake the app
            // at a prayer time, so every run has to re-arm the window ahead.
            Task { await self.armNotifications(for: settings) }
        } catch {
            self.error = AppError.classify(error)
        }

        isLoading = false
    }

    /// Re-arms the azan from the coming week's times.
    private func armNotifications(for settings: AppSettings) async {
        let days = await services.repository.week(
            city: settings.city,
            method: settings.calculationMethod,
            school: settings.asrSchool
        )
        guard !days.isEmpty else { return }
        await services.notifications.reschedule(days: days, settings: settings)
    }

    /// Which prayers call the azan, and how early the reminder comes, changed. The
    /// times on screen are still right — only the pending notifications are stale.
    func rescheduleNotifications() async {
        await armNotifications(for: settings)
    }

    /// Pull-to-refresh and the retry button.
    func refresh() async {
        error = nil
        await load()
    }

    /// Called once a second.
    ///
    /// Only the clock moves here — the list re-derives itself from `now`. The one thing
    /// that does need a reload is the date rolling over at midnight, which is easy to
    /// miss on a phone left open overnight.
    func tick(_ date: Date) {
        now = date

        guard let times else { return }
        if times.day != AppTime.dayKey(date) {
            Task { await load() }
        }
    }

    /// The city, method or school changed — whatever is on screen was computed for the
    /// old ones.
    func settingsChanged() async {
        times = nil
        await load()
    }
}
