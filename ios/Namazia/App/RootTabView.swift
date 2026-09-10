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
                .tabItem { Label("اوقات", systemImage: "clock") }

            QiblaView()
                .tabItem { Label("قبله", systemImage: "location.north.line") }

            CalendarView()
                .tabItem { Label("تقویم", systemImage: "calendar") }

            DhikrView()
                .tabItem { Label("اذکار", systemImage: "hands.sparkles") }

            SettingsView()
                .tabItem { Label("تنظیمات", systemImage: "gearshape") }
        }
        .tint(colors.primary)
    }
}
