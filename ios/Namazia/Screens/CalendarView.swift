import SwiftUI

@MainActor
final class CalendarViewModel: ObservableObject {
    @Published private(set) var days: [DayPrayerTimes] = []
    @Published private(set) var isLoading = false
    @Published private(set) var error: AppError?
    @Published private(set) var year: Int
    @Published private(set) var month: Int

    private let services: AppServices

    init(services: AppServices? = nil) {
        self.services = services ?? .shared
        let components = AppTime.calendar.dateComponents([.year, .month], from: Date())
        year = components.year ?? 2025
        month = components.month ?? 1
    }

    func load() async {
        guard !isLoading else { return }
        isLoading = true
        error = nil

        let settings = services.settings.settings
        do {
            days = try await services.repository.monthlyCalendar(
                year: year,
                month: month,
                city: settings.city,
                method: settings.calculationMethod,
                school: settings.asrSchool
            )
        } catch {
            self.error = AppError.classify(error)
            days = []
        }

        isLoading = false
    }

    func step(_ months: Int) async {
        var value = month + months
        var newYear = year
        while value > 12 { value -= 12; newYear += 1 }
        while value < 1 { value += 12; newYear -= 1 }

        month = value
        year = newYear
        days = []
        await load()
    }

    /// Gregorian months as they are named in Afghanistan — not the Persian solar month
    /// names, which belong to a different calendar entirely and would be wrong here.
    var monthTitle: String {
        let names = ["جنوری", "فبروری", "مارچ", "اپریل", "می", "جون",
                     "جولای", "اگست", "سپتمبر", "اکتوبر", "نومبر", "دسمبر"]
        let name = names.indices.contains(month - 1) ? names[month - 1] : ""
        return "\(name) \(year.persianDigits)"
    }
}

/// A month of prayer times at a glance — for planning, and for checking a day that has
/// not arrived yet.
struct CalendarView: View {
    @Environment(\.colors) private var colors
    @EnvironmentObject private var settings: SettingsStore
    @StateObject private var model = CalendarViewModel()

    private let columns: [PrayerName] = [.fajr, .dhuhr, .asr, .maghrib, .isha]

    var body: some View {
        VStack(spacing: 0) {
            header

            if model.days.isEmpty, model.isLoading {
                PrayerListSkeleton()
            } else if model.days.isEmpty, let error = model.error {
                ScrollView {
                    MessageState(
                        title: error.title,
                        message: error.message,
                        actionLabel: "تلاش دوباره",
                        action: { Task { await model.load() } }
                    )
                }
            } else {
                list
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .background(colors.background)
        .task { await model.load() }
        .onChange(of: settings.settings.calculationIdentity) { _ in
            Task { await model.load() }
        }
    }

    private var header: some View {
        HStack {
            // In a right-to-left layout "back" points right, and SwiftUI mirrors
            // `chevron.backward` automatically — which is why it is used here instead
            // of `chevron.left`.
            Button { Task { await model.step(-1) } } label: {
                Image(systemName: "chevron.backward")
                    .frame(width: Spacing.touchTarget, height: Spacing.touchTarget)
            }

            Spacer()

            Text(model.monthTitle)
                .appText(AppType.titleLarge)
                .foregroundStyle(colors.onSurface)

            Spacer()

            Button { Task { await model.step(1) } } label: {
                Image(systemName: "chevron.forward")
                    .frame(width: Spacing.touchTarget, height: Spacing.touchTarget)
            }
        }
        .tint(colors.primary)
        .padding(.horizontal, Spacing.md)
        .padding(.vertical, Spacing.sm)
        .background(colors.surface)
        .overlay(alignment: .bottom) {
            Rectangle().fill(colors.outlineVariant).frame(height: 0.5)
        }
    }

    private var list: some View {
        List {
            columnHeadings

            ForEach(model.days) { day in
                row(day)
                    .listRowInsets(EdgeInsets(top: Spacing.xs, leading: Spacing.lg,
                                              bottom: Spacing.xs, trailing: Spacing.lg))
                    .listRowBackground(Color.clear)
                    .listRowSeparator(.hidden)
            }
        }
        .listStyle(.plain)
        .scrollContentBackground(.hidden)
    }

    private var columnHeadings: some View {
        HStack(spacing: Spacing.xs) {
            Text("روز")
                .frame(width: 44, alignment: .leading)

            ForEach(columns) { prayer in
                Text(prayer.dari)
                    .frame(maxWidth: .infinity)
            }
        }
        .appText(AppType.labelSmall)
        .foregroundStyle(colors.onSurfaceVariant)
        .listRowInsets(EdgeInsets(top: Spacing.sm, leading: Spacing.lg,
                                  bottom: Spacing.xs, trailing: Spacing.lg))
        .listRowBackground(Color.clear)
        .listRowSeparator(.hidden)
    }

    private func row(_ day: DayPrayerTimes) -> some View {
        let isToday = day.isToday

        return HStack(spacing: Spacing.xs) {
            VStack(alignment: .leading, spacing: 0) {
                Text(dayNumber(day.day))
                    .appText(AppType.titleMedium)
                Text(hijriDay(day.hijriDate))
                    .appText(AppType.labelSmall)
                    .foregroundStyle(colors.onSurfaceVariant)
            }
            .frame(width: 44, alignment: .leading)

            ForEach(columns) { prayer in
                Text(day.time(for: prayer).persianDigits)
                    .appText(AppType.bodySmall)
                    .monospacedDigit()
                    .frame(maxWidth: .infinity)
            }
        }
        .foregroundStyle(isToday ? colors.onPrimaryContainer : colors.onSurface)
        .padding(.horizontal, Spacing.md)
        .padding(.vertical, Spacing.sm)
        .background(
            isToday ? colors.primaryContainer : colors.surface,
            in: RoundedRectangle(cornerRadius: Radii.md, style: .continuous)
        )
    }

    /// `"2025-09-10"` → `"۱۰"`.
    private func dayNumber(_ key: String) -> String {
        (key.split(separator: "-").last.map(String.init) ?? key).persianDigits
    }

    /// The stored Hijri date is "12 رمضان 1447"; only the day and month fit here.
    private func hijriDay(_ hijri: String) -> String {
        let parts = hijri.split(separator: " ")
        guard parts.count >= 2 else { return "" }
        return "\(parts[0]) \(parts[1])".persianDigits
    }
}
