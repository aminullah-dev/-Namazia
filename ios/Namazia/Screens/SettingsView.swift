import SwiftUI
import UIKit

struct SettingsView: View {
    @Environment(\.colors) private var colors
    @EnvironmentObject private var settings: SettingsStore
    @EnvironmentObject private var notifications: NotificationScheduler

    private let services = AppServices.shared

    /// Observed, not just held: the stop row appears and disappears with playback.
    @ObservedObject private var player = AppServices.shared.player

    /// The offsets worth offering. Anything finer is fiddling, and zero is how the
    /// reminder gets turned off entirely.
    private let reminderOptions = [0, 5, 10, 15, 20, 30]

    var body: some View {
        List {
            languageSection
            locationSection
            calculationSection
            azanSection
            reminderSection
            appearanceSection
            aboutSection
        }
        .listStyle(.insetGrouped)
        .scrollContentBackground(.hidden)
        .background(colors.background)
        .tint(colors.primary)
    }

    // MARK: - Sections

    /// First on the screen on purpose: someone who opened Settings because the app is
    /// in a language they do not read should not have to search for this.
    private var languageSection: some View {
        Section {
            Picker(selection: languageBinding) {
                ForEach(AppLanguage.allCases) { language in
                    Text(language.nativeName).tag(language)
                }
            } label: {
                label("settings.language".localized, systemImage: "globe")
            }
        } header: {
            header("settings.section.language".localized)
        }
    }

    private var locationSection: some View {
        Section {
            Picker(selection: cityBinding) {
                ForEach(AfghanCities.list.indices, id: \.self) { index in
                    Text(AfghanCities.list[index].displayName).tag(index)
                }
            } label: {
                label("settings.city".localized, systemImage: "building.2")
            }
        } header: {
            header("settings.section.location".localized)
        }
    }

    private var calculationSection: some View {
        Section {
            Picker(selection: methodBinding) {
                ForEach(CalcMethods.list) { method in
                    Text(method.name).tag(method.id)
                }
            } label: {
                label("settings.method".localized, systemImage: "function")
            }

            Picker(selection: schoolBinding) {
                ForEach(Madhabs.list) { madhab in
                    Text(madhab.name).tag(madhab.school)
                }
            } label: {
                label("settings.madhab".localized, systemImage: "book")
            }
        } header: {
            header("settings.section.calculation".localized)
        } footer: {
            footer("settings.calculation.footer".localized)
        }
    }

    private var azanSection: some View {
        Section {
            ForEach(PrayerName.allCases) { prayer in
                Toggle(isOn: prayerBinding(prayer)) {
                    label(
                        prayer.callsAzan
                            ? "settings.azanFor".localized(prayer.localizedName)
                            : prayer.localizedName,
                        systemImage: prayer.callsAzan ? "bell" : "sunrise"
                    )
                }
            }
        } header: {
            header("settings.section.azans".localized)
        } footer: {
            footer(
                prayerFooter
            )
        }
    }

    private var prayerFooter: String {
        // Sunrise is in the list because people want to see it, but it is a time
        // marker: no azan is called for it, and the switch only controls whether the
        // row appears.
        "settings.azans.footer".localized
    }

    private var reminderSection: some View {
        Section {
            Picker(selection: reminderBinding) {
                ForEach(reminderOptions, id: \.self) { minutes in
                    Text(minutes == 0 ? "settings.reminder.off".localized : "settings.reminder.minutes".localized(minutes.persianDigits)).tag(minutes)
                }
            } label: {
                label("settings.reminder".localized, systemImage: "clock.badge")
            }

            #if DEBUG
            Button {
                Task { await notifications.scheduleTest() }
            } label: {
                label("settings.testNotification".localized, systemImage: "speaker.wave.2")
            }
            #endif

            Button {
                player.play(for: .maghrib)
            } label: {
                label("settings.playAzan".localized, systemImage: "play.circle")
            }

            if player.playing != nil {
                Button(role: .destructive) {
                    player.stop()
                } label: {
                    label("settings.stopAzan".localized, systemImage: "stop.circle")
                }
            }
        } header: {
            header("settings.section.sound".localized)
        } footer: {
            footer(notificationFooter)
        }
    }

    /// Says plainly what iOS does and does not allow, rather than leaving the user to
    /// wonder why the azan is short or why there is no vibration switch.
    private var notificationFooter: String {
        var lines = [
            "settings.sound.footer.cap".localized,
            "settings.sound.footer.vibration".localized
        ]
        if notifications.authorization == .denied {
            lines.insert("settings.sound.footer.denied".localized, at: 0)
        }
        if !notifications.hasAzanSound {
            lines.append("settings.sound.footer.missing".localized)
        }
        return lines.joined(separator: "\n\n")
    }

    private var appearanceSection: some View {
        Section {
            Toggle(isOn: darkModeBinding) {
                label("settings.darkMode".localized, systemImage: "moon")
            }
        } header: {
            header("settings.section.appearance".localized)
        } footer: {
            footer("settings.darkMode.footer".localized)
        }
    }

    private var aboutSection: some View {
        Section {
            HStack {
                label("settings.version".localized, systemImage: "info.circle")
                Spacer()
                Text(appVersion.persianDigits)
                    .appText(AppType.bodyMedium)
                    .foregroundStyle(colors.onSurfaceVariant)
            }

            Link(destination: URL(string: "mailto:aminhashemi979@gmail.com")!) {
                label("settings.support".localized, systemImage: "envelope")
            }

            Link(destination: URL(string: "https://aminullah-dev.github.io/-Namazia/privacy-policy.html")!) {
                label("settings.privacy".localized, systemImage: "hand.raised")
            }
        } header: {
            header("settings.section.about".localized)
        } footer: {
            footer("settings.about.footer".localized)
        }
    }

    private var appVersion: String {
        let info = Bundle.main.infoDictionary
        let short = info?["CFBundleShortVersionString"] as? String ?? "1.0"
        let build = info?["CFBundleVersion"] as? String ?? "1"
        return "\(short) (\(build))"
    }

    // MARK: - Bindings
    //
    // Written out rather than bound straight to the store because each write has a
    // consequence beyond the value itself: the method and school also invalidate the
    // cache, and every one of them re-arms the notification queue through the home
    // screen's observation of the settings.

    private var languageBinding: Binding<AppLanguage> {
        Binding(
            get: { settings.settings.language },
            set: { settings.setLanguage($0) }
        )
    }

    private var cityBinding: Binding<Int> {
        Binding(get: { settings.settings.cityIndex }, set: { settings.setCityIndex($0) })
    }

    private var methodBinding: Binding<Int> {
        Binding(
            get: { settings.settings.calculationMethod },
            set: { value in Task { await services.setCalculationMethod(value) } }
        )
    }

    private var schoolBinding: Binding<Int> {
        Binding(
            get: { settings.settings.asrSchool },
            set: { value in Task { await services.setAsrSchool(value) } }
        )
    }

    private func prayerBinding(_ prayer: PrayerName) -> Binding<Bool> {
        Binding(
            get: { settings.settings.isEnabled(prayer) },
            set: { settings.setPrayerEnabled(prayer, $0) }
        )
    }

    private var reminderBinding: Binding<Int> {
        Binding(
            get: { settings.settings.reminderMinutes },
            set: { settings.setReminderMinutes($0) }
        )
    }

    private var darkModeBinding: Binding<Bool> {
        Binding(get: { settings.settings.darkMode }, set: { settings.setDarkMode($0) })
    }

    // MARK: - Pieces

    private func label(_ title: String, systemImage: String) -> some View {
        HStack(spacing: Spacing.md) {
            Image(systemName: systemImage)
                .font(.system(size: 15))
                .foregroundStyle(colors.primary)
                .frame(width: 24)

            Text(title)
                .appText(AppType.bodyLarge)
                .foregroundStyle(colors.onSurface)
        }
    }

    private func header(_ text: String) -> some View {
        Text(text)
            .appText(AppType.labelMedium)
            .foregroundStyle(colors.onSurfaceVariant)
    }

    private func footer(_ text: String) -> some View {
        Text(text)
            .appText(AppType.bodySmall)
            .foregroundStyle(colors.onSurfaceVariant)
    }
}
