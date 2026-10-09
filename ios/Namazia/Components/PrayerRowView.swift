import SwiftUI

/// One line of the prayer list.
struct PrayerRowView: View {
    @Environment(\.colors) private var colors

    let row: PrayerRow

    private var background: Color {
        if row.isNext { return colors.primaryContainer }
        if row.isPast { return colors.surfaceVariant.opacity(0.45) }
        return colors.surface
    }

    private var foreground: Color {
        if row.isNext { return colors.onPrimaryContainer }
        if row.isPast { return colors.onSurfaceVariant }
        return colors.onSurface
    }

    var body: some View {
        HStack(spacing: Spacing.md) {
            statusDot

            VStack(alignment: .leading, spacing: 0) {
                Text(row.prayer.localizedName)
                    .appText(row.isNext ? AppType.titleLarge : AppType.titleMedium)
                    .foregroundStyle(foreground)
                Text(row.prayer.english)
                    .appText(AppType.labelSmall)
                    .foregroundStyle(foreground.opacity(0.6))
            }

            Spacer(minLength: Spacing.sm)

            // The azan for this prayer is switched off — said with an icon, not only
            // with colour. Sunrise is exempt: it is a time marker, and no azan is
            // called for it, so a crossed-out bell there would be misleading.
            if !row.isEnabled, row.prayer.callsAzan {
                Image(systemName: "bell.slash")
                    .font(.system(size: 15))
                    .foregroundStyle(foreground.opacity(0.5))
            }

            Text(row.clock.persianDigits)
                .appText(AppType.headlineSmall)
                .foregroundStyle(row.isNext ? colors.primary : foreground)
                .monospacedDigit()
        }
        .padding(.horizontal, Spacing.lg)
        .padding(.vertical, Spacing.md)
        .frame(minHeight: 64)
        .background(
            background,
            in: RoundedRectangle(cornerRadius: Radii.md, style: .continuous)
        )
        .animation(.easeInOut(duration: 0.4), value: row.isNext)
        .accessibilityElement(children: .combine)
        .accessibilityLabel(accessibilityText)
    }

    /// Status is carried by a shape as well as a colour, so it survives greyscale and
    /// colour-blindness.
    private var statusDot: some View {
        Group {
            if row.isNext {
                dot(colors.primary) {
                    Image(systemName: "bell.badge.fill")
                        .foregroundStyle(colors.onPrimary)
                }
            } else if row.isPast {
                dot(colors.surfaceVariant) {
                    Image(systemName: "checkmark")
                        .foregroundStyle(colors.onSurfaceVariant)
                }
            } else {
                dot(colors.surfaceVariant.opacity(0.6)) {
                    Image(systemName: "sun.max")
                        .foregroundStyle(colors.onSurfaceVariant.opacity(0.7))
                }
            }
        }
    }

    private func dot<Content: View>(
        _ fill: Color,
        @ViewBuilder content: () -> Content
    ) -> some View {
        content()
            .font(.system(size: 14, weight: .semibold))
            .frame(width: 32, height: 32)
            .background(fill, in: Circle())
    }

    private var accessibilityText: String {
        var parts = ["\(row.prayer.localizedName) \(row.clock.persianDigits)"]
        if row.isNext { parts.append("a11y.nextPrayer".localized) }
        else if row.isPast { parts.append("a11y.past".localized) }
        if !row.isEnabled, row.prayer.callsAzan { parts.append("a11y.azanOff".localized) }
        return parts.joined(separator: "، ")
    }
}
