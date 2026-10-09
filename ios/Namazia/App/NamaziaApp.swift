import SwiftUI

@main
struct NamaziaApp: App {
    /// The notification delegate has to be installed before launch finishes, and
    /// SwiftUI offers no hook that early — hence the UIKit delegate.
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate

    @StateObject private var settings = AppServices.shared.settings
    @StateObject private var notifications = AppServices.shared.notifications

    var body: some Scene {
        WindowGroup {
            RootTabView()
                .environmentObject(settings)
                .environmentObject(notifications)
                .themed()
                // Off means "follow the phone", not "force light" — a reader who has
                // set their whole device to dark should not be handed a white screen
                // by an app they never configured.
                .preferredColorScheme(settings.settings.darkMode ? .dark : nil)
        }
    }
}

/// Applies everything every screen needs: right-to-left layout, the resolved colour
/// set, and the app background.
///
/// Layout direction is forced rather than left to the device locale. The whole UI is
/// Dari, so a phone set to English would otherwise render a left-to-right layout with
/// Persian text in it — the same reason the Android side pins `LayoutDirection.Rtl`.
struct ThemedContainer: ViewModifier {
    @Environment(\.colorScheme) private var scheme

    func body(content: Content) -> some View {
        let colors = AppColors(scheme: scheme)

        return content
            .environment(\.colors, colors)
            .environment(\.layoutDirection, .rightToLeft)
            .tint(colors.primary)
            .background(colors.background.ignoresSafeArea())
    }
}

extension View {
    func themed() -> some View { modifier(ThemedContainer()) }
}
