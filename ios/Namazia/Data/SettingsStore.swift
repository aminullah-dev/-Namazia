import Foundation
import Combine

/// User settings, in the App Group's `UserDefaults` so the widget reads the same values.
/// The Android counterpart is `data/SettingsDataStore.kt`.
///
/// Values are stored one key per setting rather than as a single encoded blob. With a
/// blob, adding a field later makes the whole thing fail to decode and silently resets
/// every setting the user had chosen; per-key storage degrades to "the new field takes
/// its default", which is what anyone would expect.
@MainActor
final class SettingsStore: ObservableObject {

    @Published private(set) var settings: AppSettings
    @Published private(set) var tasbihCount: Int

    private let defaults: UserDefaults

    private enum Keys {
        static let cityIndex = "city_index"
        static let calcMethod = "calc_method"
        static let asrSchool = "asr_school"
        static let fajrEnabled = "fajr_enabled"
        static let dhuhrEnabled = "dhuhr_enabled"
        static let asrEnabled = "asr_enabled"
        static let maghribEnabled = "maghrib_enabled"
        static let ishaEnabled = "isha_enabled"
        static let sunriseEnabled = "sunrise_enabled"
        static let reminderMinutes = "reminder_minutes"
        static let vibrationEnabled = "vibration_enabled"
        static let darkMode = "dark_mode"
        static let tasbihCount = "tasbih_count"
    }

    init(defaults: UserDefaults = AppGroup.defaults) {
        self.defaults = defaults
        self.settings = SettingsStore.read(from: defaults)
        self.tasbihCount = defaults.integer(forKey: Keys.tasbihCount)
    }

    // MARK: - Reading

    private static func read(from defaults: UserDefaults) -> AppSettings {
        var settings = AppSettings()
        // `integer(forKey:)` and `bool(forKey:)` return 0 / false for a key that was
        // never written, which is indistinguishable from a stored 0 / false — hence the
        // explicit presence check before overriding each default.
        func int(_ key: String, _ current: Int) -> Int {
            defaults.object(forKey: key) == nil ? current : defaults.integer(forKey: key)
        }
        func bool(_ key: String, _ current: Bool) -> Bool {
            defaults.object(forKey: key) == nil ? current : defaults.bool(forKey: key)
        }

        settings.cityIndex = int(Keys.cityIndex, settings.cityIndex)
        settings.calculationMethod = int(Keys.calcMethod, settings.calculationMethod)
        settings.asrSchool = int(Keys.asrSchool, settings.asrSchool)
        settings.reminderMinutes = int(Keys.reminderMinutes, settings.reminderMinutes)
        settings.fajrEnabled = bool(Keys.fajrEnabled, settings.fajrEnabled)
        settings.dhuhrEnabled = bool(Keys.dhuhrEnabled, settings.dhuhrEnabled)
        settings.asrEnabled = bool(Keys.asrEnabled, settings.asrEnabled)
        settings.maghribEnabled = bool(Keys.maghribEnabled, settings.maghribEnabled)
        settings.ishaEnabled = bool(Keys.ishaEnabled, settings.ishaEnabled)
        settings.sunriseEnabled = bool(Keys.sunriseEnabled, settings.sunriseEnabled)
        settings.vibrationEnabled = bool(Keys.vibrationEnabled, settings.vibrationEnabled)
        settings.darkMode = bool(Keys.darkMode, settings.darkMode)
        return settings
    }

    // MARK: - Writing

    func setCityIndex(_ index: Int) {
        guard AfghanCities.list.indices.contains(index) else { return }
        defaults.set(index, forKey: Keys.cityIndex)
        settings.cityIndex = index
    }

    /// Changing this invalidates every cached time — the caller must clear the cache.
    func setCalculationMethod(_ method: Int) {
        defaults.set(method, forKey: Keys.calcMethod)
        settings.calculationMethod = method
    }

    /// Same: Asr moves by about an hour between the schools.
    func setAsrSchool(_ school: Int) {
        defaults.set(school, forKey: Keys.asrSchool)
        settings.asrSchool = school
    }

    func setPrayerEnabled(_ prayer: PrayerName, _ enabled: Bool) {
        let key: String
        switch prayer {
        case .fajr: key = Keys.fajrEnabled
        case .sunrise: key = Keys.sunriseEnabled
        case .dhuhr: key = Keys.dhuhrEnabled
        case .asr: key = Keys.asrEnabled
        case .maghrib: key = Keys.maghribEnabled
        case .isha: key = Keys.ishaEnabled
        }
        defaults.set(enabled, forKey: key)
        settings.setEnabled(prayer, enabled)
    }

    func setReminderMinutes(_ minutes: Int) {
        defaults.set(minutes, forKey: Keys.reminderMinutes)
        settings.reminderMinutes = minutes
    }

    func setVibration(_ enabled: Bool) {
        defaults.set(enabled, forKey: Keys.vibrationEnabled)
        settings.vibrationEnabled = enabled
    }

    func setDarkMode(_ enabled: Bool) {
        defaults.set(enabled, forKey: Keys.darkMode)
        settings.darkMode = enabled
    }

    func setTasbihCount(_ count: Int) {
        let clamped = max(0, count)
        defaults.set(clamped, forKey: Keys.tasbihCount)
        tasbihCount = clamped
    }
}
