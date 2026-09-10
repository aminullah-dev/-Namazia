import Foundation

/// The app's shared objects, in one place.
///
/// A single instance rather than passing a container down through initialisers: the
/// cache is a file and the settings store is a `UserDefaults` suite, so a second copy
/// of either would mean two views of the same bytes, each unaware of the other's
/// writes. Screens reach for `AppServices.shared`; only tests build their own.
@MainActor
final class AppServices {
    static let shared = AppServices()

    let repository: PrayerTimesRepository
    let settings: SettingsStore

    /// The parameters take `nil` rather than a constructed default.
    ///
    /// A default *argument* is evaluated in a nonisolated context, so
    /// `settings: SettingsStore = SettingsStore()` would be calling a main-actor
    /// isolated initialiser from outside the actor — which does not compile. Building
    /// the real objects inside the body, which is main-actor isolated like the rest of
    /// the class, is the way to keep both the convenience and the injection point.
    init(repository: PrayerTimesRepository? = nil, settings: SettingsStore? = nil) {
        self.repository = repository ?? PrayerTimesRepository()
        self.settings = settings ?? SettingsStore()
    }

    /// Changing the calculation method or the fiqh school changes every prayer time,
    /// so the cache — full of days computed the old way — has to go with it.
    ///
    /// These two live here rather than on `SettingsStore` so that the write and the
    /// invalidation cannot be done separately, which is exactly the mistake that leaves
    /// an app showing the old Asr for a week.
    func setCalculationMethod(_ method: Int) async {
        guard method != settings.settings.calculationMethod else { return }
        settings.setCalculationMethod(method)
        await repository.clearCache()
    }

    func setAsrSchool(_ school: Int) async {
        guard school != settings.settings.asrSchool else { return }
        settings.setAsrSchool(school)
        await repository.clearCache()
    }
}
