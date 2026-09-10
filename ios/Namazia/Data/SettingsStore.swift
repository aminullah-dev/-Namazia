import Foundation
import Combine
import WidgetKit

/// User settings, in the App Group's `UserDefaults` so the widget reads the same values.
/// The Android counterpart is `data/SettingsDataStore.kt`.
///
/// Values are stored one key per setting rather than as a single encoded blob. With a
/// blob, adding a field later makes the whole thing fail to decode and silently resets
/// every setting the user had chosen; per-key storage degrades to "the new field takes
/// its default", which is what anyone would expect. The keys and the reading half live
/// in `SettingsKeys` / `AppSettings.load(from:)` so the widget can use them without
/// this object.
@MainActor
final class SettingsStore: ObservableObject {

    @Published private(set) var settings: AppSettings
    @Published private(set) var tasbihCount: Int

    private let defaults: UserDefaults

    init(defaults: UserDefaults = AppGroup.defaults) {
        self.defaults = defaults
        self.settings = AppSettings.load(from: defaults)
        self.tasbihCount = defaults.integer(forKey: SettingsKeys.tasbihCount)
    }

    // MARK: - Writing

    func setCityIndex(_ index: Int) {
        guard AfghanCities.list.indices.contains(index) else { return }
        write(index, forKey: SettingsKeys.cityIndex) { $0.cityIndex = index }
    }

    /// Changing this invalidates every cached time — go through
    /// `AppServices.setCalculationMethod`, which clears the cache in the same call.
    func setCalculationMethod(_ method: Int) {
        write(method, forKey: SettingsKeys.calcMethod) { $0.calculationMethod = method }
    }

    /// Same: Asr moves by about an hour between the schools.
    func setAsrSchool(_ school: Int) {
        write(school, forKey: SettingsKeys.asrSchool) { $0.asrSchool = school }
    }

    func setPrayerEnabled(_ prayer: PrayerName, _ enabled: Bool) {
        write(enabled, forKey: SettingsKeys.enabledKey(for: prayer)) {
            $0.setEnabled(prayer, enabled)
        }
    }

    func setReminderMinutes(_ minutes: Int) {
        write(minutes, forKey: SettingsKeys.reminderMinutes) { $0.reminderMinutes = minutes }
    }

    func setVibration(_ enabled: Bool) {
        write(enabled, forKey: SettingsKeys.vibrationEnabled) { $0.vibrationEnabled = enabled }
    }

    func setDarkMode(_ enabled: Bool) {
        write(enabled, forKey: SettingsKeys.darkMode) { $0.darkMode = enabled }
    }

    func setTasbihCount(_ count: Int) {
        let clamped = max(0, count)
        defaults.set(clamped, forKey: SettingsKeys.tasbihCount)
        tasbihCount = clamped
    }

    /// Every settings write does the same three things, and forgetting the third is how
    /// a widget ends up showing another city's times for hours.
    private func write(_ value: Any, forKey key: String, update: (inout AppSettings) -> Void) {
        defaults.set(value, forKey: key)
        update(&settings)
        WidgetCenter.shared.reloadAllTimelines()
    }
}
