import SwiftUI

/// The app's five destinations.
///
/// A tab bar rather than a navigation stack: every one of these is a place you go back
/// to, not a step in a flow, and a Muslim checking the qibla in an unfamiliar room
/// should reach it in one tap.
struct RootTabView: View {
    @Environment(\.colors) private var colors
    @State private var selection = RootTabView.initialTab

    var body: some View {
        TabView(selection: $selection) {
            HomeView()
                .tabItem { Label("tab.times".localized, systemImage: "clock") }
                .tag(0)

            QiblaView()
                .tabItem { Label("tab.qibla".localized, systemImage: "location.north.line") }
                .tag(1)

            CalendarView()
                .tabItem { Label("tab.calendar".localized, systemImage: "calendar") }
                .tag(2)

            DhikrView()
                .tabItem { Label("tab.dhikr".localized, systemImage: "hands.sparkles") }
                .tag(3)

            SettingsView()
                .tabItem { Label("tab.settings".localized, systemImage: "gearshape") }
                .tag(4)
        }
        .tint(colors.primary)
    }

    /// `-initialTab 2` on launch opens the calendar — how the store screenshots are
    /// taken from the simulator without tapping. Debug builds only.
    #if DEBUG
    private static let initialTab = UserDefaults.standard.integer(forKey: "initialTab")
    #else
    private static let initialTab = 0
    #endif
}
