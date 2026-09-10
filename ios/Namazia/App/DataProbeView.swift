import SwiftUI

/// Phase 2 smoke test: proves the data layer end to end before any real screen depends
/// on it — API reachable, response decoded, cache written and re-read, settings
/// persisted across launches, times resolved to the right instants in Kabul time.
///
/// Replaced by the real home screen in Phase 3.
struct DataProbeView: View {
    @Environment(\.colors) private var colors
    @StateObject private var model = DataProbeModel()

    /// SwiftUI owns the countdown tick rather than the model owning a long-lived
    /// `Task`: the publisher stops when the view goes away, with nothing to cancel by
    /// hand.
    private let tick = Timer.publish(every: 1, on: .main, in: .common).autoconnect()

    var body: some View {
        VStack(alignment: .leading, spacing: Spacing.md) {
            picker

            switch model.state {
            case .idle, .loading:
                ProgressView()
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.vertical, Spacing.lg)

            case .failed(let error):
                VStack(alignment: .leading, spacing: Spacing.xs) {
                    Text(error.title)
                        .appText(AppType.titleMedium)
                        .foregroundStyle(colors.error)
                    Text(error.message)
                        .appText(AppType.bodySmall)
                        .foregroundStyle(colors.onSurfaceVariant)
                }

            case .loaded(let times):
                loaded(times)
            }

            HStack(spacing: Spacing.sm) {
                Button("بارگیری دوباره") { model.reload() }
                Button("پاک کردن حافظه") { model.clearCache() }
            }
            .appText(AppType.labelLarge)

            Text(model.source)
                .appText(AppType.labelSmall)
                .foregroundStyle(colors.onSurfaceVariant)
        }
        .task { await model.start() }
        .onReceive(tick) { model.update(now: $0) }
    }

    private var picker: some View {
        Picker("شهر", selection: Binding(
            get: { model.cityIndex },
            set: { model.selectCity($0) }
        )) {
            ForEach(AfghanCities.list.indices, id: \.self) { index in
                Text(AfghanCities.list[index].nameDari).tag(index)
            }
        }
        .pickerStyle(.menu)
        .tint(colors.primary)
    }

    @ViewBuilder
    private func loaded(_ times: DayPrayerTimes) -> some View {
        VStack(alignment: .leading, spacing: Spacing.sm) {
            Text(times.hijriDate.persianDigits)
                .appText(AppType.labelLarge)
                .foregroundStyle(colors.tertiary)

            ForEach(times.enabledSchedule(model.settings)) { entry in
                HStack {
                    Text(entry.prayer.dari)
                        .appText(AppType.bodyLarge)
                    Spacer()
                    Text(entry.clock.persianDigits)
                        .appText(AppType.titleMedium)
                        .foregroundStyle(
                            entry.id == model.next?.id ? colors.primary : colors.onSurface
                        )
                }
            }

            if let next = model.next {
                Divider().overlay(colors.outline)
                HStack {
                    Text("تا \(next.prayer.dari)")
                        .appText(AppType.labelLarge)
                        .foregroundStyle(colors.onSurfaceVariant)
                    Spacer()
                    Text(countdownText(seconds: next.secondsAway(from: model.now)))
                        .appText(AppType.titleLarge)
                        .foregroundStyle(colors.primary)
                        .monospacedDigit()
                }
            } else {
                Text("اوقات امروز تمام شد")
                    .appText(AppType.labelLarge)
                    .foregroundStyle(colors.onSurfaceVariant)
            }
        }
    }
}

@MainActor
final class DataProbeModel: ObservableObject {

    enum State {
        case idle
        case loading
        case loaded(DayPrayerTimes)
        case failed(AppError)
    }

    @Published private(set) var state: State = .idle
    @Published private(set) var now = Date()
    @Published private(set) var source = ""

    private let store = SettingsStore()
    private let repository = PrayerTimesRepository()

    var settings: AppSettings { store.settings }
    var cityIndex: Int { store.settings.cityIndex }

    var next: ScheduledPrayer? {
        guard case .loaded(let times) = state else { return nil }
        return times.upcoming(store.settings, now: now)
    }

    func start() async {
        await load()
    }

    func update(now: Date) {
        self.now = now
    }

    func selectCity(_ index: Int) {
        store.setCityIndex(index)
        objectWillChange.send()
        reload()
    }

    func reload() {
        Task { await load() }
    }

    func clearCache() {
        Task {
            await repository.clearCache()
            await load()
        }
    }

    private func load() async {
        state = .loading
        let settings = store.settings
        let started = Date()

        do {
            let times = try await repository.prayerTimes(
                city: settings.city,
                method: settings.calculationMethod,
                school: settings.asrSchool
            )
            state = .loaded(times)
            // A cache hit answers in microseconds; a network round trip does not. The
            // gap is the only visible proof that the cache is actually being used.
            let elapsed = Int(Date().timeIntervalSince(started) * 1000)
            source = "\(settings.city.nameEn) • \(elapsed)ms • "
                + (AppGroup.isAvailable ? "App Group" : "حافظه‌ی داخلی")

            // Detached so the coming week is warmed without holding up anything the
            // user can see.
            Task {
                await repository.prefetchWeek(
                    city: settings.city,
                    method: settings.calculationMethod,
                    school: settings.asrSchool
                )
            }
        } catch {
            state = .failed(AppError.classify(error))
            source = AppError.classify(error).detail ?? ""
        }
    }

}
