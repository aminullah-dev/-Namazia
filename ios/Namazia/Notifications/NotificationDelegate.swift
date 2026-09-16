import UIKit
import UserNotifications

/// Decides what a notification does while the app is already open.
///
/// Without a delegate, iOS shows nothing at all in the foreground — the app is
/// presumed to be handling it. Here the banner is shown, but the notification's own
/// 30-second sound is suppressed in favour of the full azan through `AzanPlayer`.
final class NotificationDelegate: NSObject, UNUserNotificationCenterDelegate {

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        let info = notification.request.content.userInfo
        let kind = info[NotificationScheduler.Keys.kind] as? String
        let prayer = (info[NotificationScheduler.Keys.prayer] as? String)
            .flatMap(PrayerName.init(rawValue:))

        guard kind == NotificationScheduler.Kind.azan, let prayer else {
            // A reminder keeps its own short sound.
            completionHandler([.banner, .list, .sound])
            return
        }

        Task { @MainActor in
            AppServices.shared.player.play(for: prayer)
        }

        // No `.sound`: the player is already handling the audio, and both at once would
        // be two azans over each other.
        completionHandler([.banner, .list])
    }

    /// Tapping a notification brings the app forward; nothing else is needed yet, but
    /// the callback has to be answered or the system logs a warning.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        completionHandler()
    }
}

/// SwiftUI has no hook for "before the app finishes launching", which is when the
/// notification delegate must be in place — set it later and a notification that
/// launched the app is delivered to nobody.
final class AppDelegate: NSObject, UIApplicationDelegate {
    static let notificationDelegate = NotificationDelegate()

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = AppDelegate.notificationDelegate
        BackgroundRefresh.register()
        BackgroundRefresh.schedule()
        return true
    }
}
