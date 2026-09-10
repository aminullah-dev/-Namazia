import SwiftUI

/// The app's five destinations.
///
/// A tab bar rather than a navigation stack: every one of these is a place you go back
/// to, not a step in a flow, and a Muslim checking the qibla in an unfamiliar room
/// should reach it in one tap.
struct RootTabView: View {
    @Environment(\.colors) private var colors

    var body: some View {
        TabView {
            HomeView()
                .tabItem { Label("tab.times".localized, systemImage: "clock") }

            QiblaView()
                .tabItem { Label("tab.qibla".localized, systemImage: "location.north.line") }

            CalendarView()
                .tabItem { Label("tab.calendar".localized, systemImage: "calendar") }

            DhikrView()
                .tabItem { Label("tab.dhikr".localized, systemImage: "hands.sparkles") }

            SettingsView()
                .tabItem { Label("tab.settings".localized, systemImage: "gearshape") }
        }
        .tint(colors.primary)
    }
}
