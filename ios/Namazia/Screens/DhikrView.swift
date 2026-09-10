import SwiftUI
import UIKit

/// Adhkar to read, and a tasbih to count with.
struct DhikrView: View {
    @Environment(\.colors) private var colors
    @EnvironmentObject private var settings: SettingsStore

    @State private var tab = Tab.adhkar
    @State private var categoryIndex = 0

    private enum Tab: String, CaseIterable, Identifiable {
        case adhkar, tasbih
        var id: String { rawValue }
        var title: String { self == .adhkar ? "اذکار" : "تسبیح" }
    }

    var body: some View {
        VStack(spacing: 0) {
            Picker("", selection: $tab) {
                ForEach(Tab.allCases) { tab in
                    Text(tab.title).tag(tab)
                }
            }
            .pickerStyle(.segmented)
            .padding(Spacing.lg)

            switch tab {
            case .adhkar: adhkar
            case .tasbih: tasbih
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .background(colors.background)
    }

    // MARK: - Adhkar

    private var adhkar: some View {
        VStack(spacing: 0) {
            categoryChips

            ScrollView {
                LazyVStack(spacing: Spacing.md) {
                    ForEach(DhikrData.categories[categoryIndex].dhikr) { dhikr in
                        card(dhikr)
                    }
                }
                .padding(.horizontal, Spacing.lg)
                .padding(.bottom, Spacing.xl)
            }
        }
    }

    private var categoryChips: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: Spacing.sm) {
                ForEach(Array(DhikrData.categories.indices), id: \.self) { index in
                    let selected = index == categoryIndex

                    Button {
                        categoryIndex = index
                    } label: {
                        Text(DhikrData.categories[index].title)
                            .appText(AppType.labelLarge)
                            .foregroundStyle(selected ? colors.onPrimary : colors.onSurfaceVariant)
                            .padding(.horizontal, Spacing.md)
                            .frame(minHeight: 36)
                            .background(
                                selected ? colors.primary : colors.surfaceVariant,
                                in: Capsule()
                            )
                    }
                }
            }
            .padding(.horizontal, Spacing.lg)
            .padding(.bottom, Spacing.md)
        }
    }

    private func card(_ dhikr: Dhikr) -> some View {
        VStack(alignment: .leading, spacing: Spacing.md) {
            Text(dhikr.arabic)
                .appText(AppType.arabicVerse)
                .foregroundStyle(colors.onSurface)
                .frame(maxWidth: .infinity, alignment: .trailing)

            Text(dhikr.dari)
                .appText(AppType.bodyMedium)
                .foregroundStyle(colors.onSurfaceVariant)
                .frame(maxWidth: .infinity, alignment: .leading)

            if dhikr.count > 1 {
                Text("\(dhikr.count.persianDigits) بار")
                    .appText(AppType.labelMedium)
                    .foregroundStyle(colors.onTertiaryContainer)
                    .padding(.horizontal, Spacing.md)
                    .padding(.vertical, Spacing.xs)
                    .background(colors.tertiaryContainer, in: Capsule())
            }
        }
        .cardSurface(colors, cornerRadius: Radii.lg)
    }

    // MARK: - Tasbih

    /// The whole screen is the button. A counter you have to aim at is a counter you
    /// lose your place on — the thumb should be able to stay where it is.
    private var tasbih: some View {
        VStack(spacing: Spacing.xl) {
            Spacer()

            Text(settings.tasbihCount.persianDigits)
                .appText(AppType.displayLarge)
                .foregroundStyle(colors.primary)
                .monospacedDigit()
                .contentTransition(.numericText())

            Text("برای شمردن، هرجای صفحه را لمس کنید")
                .appText(AppType.bodySmall)
                .foregroundStyle(colors.onSurfaceVariant)

            Spacer()

            Button("صفر کردن") {
                settings.setTasbihCount(0)
            }
            .appText(AppType.labelLarge)
            .tint(colors.error)
            .padding(.bottom, Spacing.xxl)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .contentShape(Rectangle())
        .onTapGesture {
            settings.setTasbihCount(settings.tasbihCount + 1)
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
        }
    }
}
