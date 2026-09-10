import SwiftUI

@main
struct NamaziaApp: App {
    var body: some Scene {
        WindowGroup {
            RootView()
                .themed()
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
