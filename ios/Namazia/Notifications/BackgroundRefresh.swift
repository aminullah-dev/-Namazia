import BackgroundTasks
import Foundation

/// Tops up the pending azan queue while the app is closed.
///
/// The queue is bounded at 64 notifications — roughly six days of prayers — and only
/// the running app can refill it. Without this, someone who does not open the app for
/// a week simply stops being called to prayer, with nothing on screen to explain why.
///
/// iOS decides if and when this runs, based on how the person uses the app; it is a
/// top-up, never the primary mechanism. Every foreground launch re-arms the window too,
/// which is what actually keeps most installs healthy.
enum BackgroundRefresh {

    /// Must also appear in `BGTaskSchedulerPermittedIdentifiers` in Info.plist, or
    /// registration throws at launch.
    static let identifier = "af.namazia.app.refresh"

    /// Called once, before the app finishes launching. Registering later is a hard
    /// error rather than a no-op.
    static func register() {
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: identifier,
            using: nil
        ) { task in
            handle(task)
        }
    }

    /// Asks for another run. Requested on every launch and after each background run,
    /// because a submitted request is consumed once it fires.
    static func schedule() {
        let request = BGAppRefreshTaskRequest(identifier: identifier)
        // No sooner than six hours out. Prayer times for a given day never change, so
        // there is nothing to gain from asking more often — and asking too often is how
        // iOS learns to deprioritise an app.
        request.earliestBeginDate = Date().addingTimeInterval(6 * 3600)

        // Throws in the simulator and when the user has switched Background App Refresh
        // off. Neither is worth surfacing: the foreground path still works.
        try? BGTaskScheduler.shared.submit(request)
    }

    private static func handle(_ task: BGTask) {
        schedule()

        let work = Task { @MainActor in
            let services = AppServices.shared
            let settings = services.settings.settings

            let days = await services.repository.week(
                city: settings.city,
                method: settings.calculationMethod,
                school: settings.asrSchool
            )
            guard !days.isEmpty else { return false }

            await services.notifications.reschedule(days: days, settings: settings)
            return true
        }

        // The system reclaims the app if the work outlives its window; cancelling first
        // means the next run starts clean instead of being killed mid-write.
        task.expirationHandler = { work.cancel() }

        Task {
            let refreshed = await work.value
            task.setTaskCompleted(success: refreshed)
        }
    }
}
