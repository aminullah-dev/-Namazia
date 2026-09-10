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

    private var locationSection: some View {
        Section {
            Picker(selection: cityBinding) {
                ForEach(AfghanCities.list.indices, id: \.self) { index in
                    Text(AfghanCities.list[index].nameDari).tag(index)
                }
            } label: {
                label("شهر", systemImage: "building.2")
            }
        } header: {
            header("موقعیت")
        }
    }

    private var calculationSection: some View {
        Section {
            Picker(selection: methodBinding) {
                ForEach(CalcMethods.list) { method in
                    Text(method.nameDari).tag(method.id)
                }
            } label: {
                label("روش محاسبه", systemImage: "function")
            }

            Picker(selection: schoolBinding) {
                ForEach(Madhabs.list) { madhab in
                    Text(madhab.nameDari).tag(madhab.school)
                }
            } label: {
                label("مذهب (وقت عصر)", systemImage: "book")
            }
        } header: {
            header("محاسبه")
        } footer: {
            footer("تغییر این دو، اوقات ذخیره‌شده را پاک می‌کند و همه دوباره محاسبه می‌شوند.")
        }
    }

    private var azanSection: some View {
        Section {
            ForEach(PrayerName.allCases) { prayer in
                Toggle(isOn: prayerBinding(prayer)) {
                    label(
                        prayer.callsAzan ? "اذان \(prayer.dari)" : prayer.dari,
                        systemImage: prayer.callsAzan ? "bell" : "sunrise"
                    )
                }
            }
        } header: {
            header("اذان‌ها")
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
        "برای طلوع آفتاب اذانی گفته نمی‌شود؛ این کلید فقط نمایش آن را در لیست تعیین می‌کند."
    }

    private var reminderSection: some View {
        Section {
            Picker(selection: reminderBinding) {
                ForEach(reminderOptions, id: \.self) { minutes in
                    Text(minutes == 0 ? "بدون یادآوری" : "\(minutes.persianDigits) دقیقه").tag(minutes)
                }
            } label: {
                label("یادآوری پیش از اذان", systemImage: "clock.badge")
            }

            #if DEBUG
            Button {
                Task { await notifications.scheduleTest() }
            } label: {
                label("تست نوتیفیکیشن اذان", systemImage: "speaker.wave.2")
            }
            #endif

            Button {
                player.play(for: .maghrib)
            } label: {
                label("پخش اذان (آزمایش صدا)", systemImage: "play.circle")
            }

            if player.playing != nil {
                Button(role: .destructive) {
                    player.stop()
                } label: {
                    label("توقف اذان", systemImage: "stop.circle")
                }
            }
        } header: {
            header("یادآوری و صدا")
        } footer: {
            footer(notificationFooter)
        }
    }

    /// Says plainly what iOS does and does not allow, rather than leaving the user to
    /// wonder why the azan is short or why there is no vibration switch.
    private var notificationFooter: String {
        var lines = [
            "در iOS صدای نوتیفیکیشن حداکثر ۳۰ ثانیه است؛ اذان کامل تنها وقتی پخش می‌شود که برنامه باز باشد.",
            "لرزش اعلان‌ها را iOS کنترل می‌کند و از داخل برنامه قابل تغییر نیست."
        ]
        if notifications.authorization == .denied {
            lines.insert("اعلان‌ها خاموش است — بدون آن اذان پخش نمی‌شود.", at: 0)
        }
        if !notifications.hasAzanSound {
            lines.append("فایل صوتی کوتاه اذان در این نسخه موجود نیست؛ صدای پیش‌فرض پخش می‌شود.")
        }
        return lines.joined(separator: "\n\n")
    }

    private var appearanceSection: some View {
        Section {
            Toggle(isOn: darkModeBinding) {
                label("حالت تاریک", systemImage: "moon")
            }
        } header: {
            header("ظاهر")
        } footer: {
            footer("خاموش یعنی همان حالتی که در تنظیمات گوشی انتخاب کرده‌اید.")
        }
    }

    private var aboutSection: some View {
        Section {
            HStack {
                label("نسخه", systemImage: "info.circle")
                Spacer()
                Text(appVersion.persianDigits)
                    .appText(AppType.bodyMedium)
                    .foregroundStyle(colors.onSurfaceVariant)
            }

            Link(destination: URL(string: "mailto:aminhashemi979@gmail.com")!) {
                label("پشتیبانی", systemImage: "envelope")
            }

            Link(destination: URL(string: "https://aminullah-dev.github.io/-Namazia/privacy-policy.html")!) {
                label("سیاست حریم خصوصی", systemImage: "hand.raised")
            }
        } header: {
            header("درباره")
        } footer: {
            footer("اوقات نماز از سرویس Aladhan گرفته می‌شود.")
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
