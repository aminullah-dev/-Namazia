import SwiftUI

/// A shimmering placeholder block, ported from `ui/components/StateViews.kt`.
struct SkeletonBlock: View {
    @Environment(\.colors) private var colors

    var height: CGFloat = 20
    var cornerRadius: CGFloat = Radii.sm

    @State private var bright = false

    var body: some View {
        RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
            .fill(colors.surfaceVariant)
            .frame(maxWidth: .infinity)
            .frame(height: height)
            .opacity(bright ? 0.8 : 0.35)
            .animation(
                .easeInOut(duration: 0.9).repeatForever(autoreverses: true),
                value: bright
            )
            .onAppear { bright = true }
    }
}

/// Stand-in for the prayer list while the first load runs.
///
/// A shaped placeholder rather than a spinner: the layout does not jump when the real
/// data lands, so the first frame already looks like the screen it is becoming.
struct PrayerListSkeleton: View {
    var body: some View {
        VStack(spacing: Spacing.md) {
            SkeletonBlock(height: 260, cornerRadius: Radii.xl)
            ForEach(0..<5, id: \.self) { _ in
                SkeletonBlock(height: 68, cornerRadius: Radii.md)
            }
            Spacer(minLength: 0)
        }
        .padding(Spacing.lg)
    }
}

/// Centred message with an icon and an optional action — errors and empty states.
struct MessageState: View {
    @Environment(\.colors) private var colors

    let title: String
    // Named `message`, not `body`: a stored property called `body` would collide with
    // the `View` requirement below.
    var message: String?
    var systemImage: String = "wifi.slash"
    var actionLabel: String?
    var action: (() -> Void)?

    var body: some View {
        VStack(spacing: Spacing.lg) {
            Image(systemName: systemImage)
                .font(.system(size: 30, weight: .regular))
                .foregroundStyle(colors.onSurfaceVariant)
                .frame(width: 72, height: 72)
                .background(colors.surfaceVariant, in: Circle())

            VStack(spacing: Spacing.sm) {
                Text(title)
                    .appText(AppType.titleMedium)
                    .foregroundStyle(colors.onSurface)

                if let message {
                    Text(message)
                        .appText(AppType.bodyMedium)
                        .foregroundStyle(colors.onSurfaceVariant)
                }
            }
            .multilineTextAlignment(.center)

            if let actionLabel, let action {
                Button(action: action) {
                    Label(actionLabel, systemImage: "arrow.clockwise")
                        .appText(AppType.labelLarge)
                        .padding(.horizontal, Spacing.lg)
                        .frame(minHeight: Spacing.touchTarget)
                }
                .buttonStyle(.borderedProminent)
                .tint(colors.primary)
                .clipShape(Capsule())
            }
        }
        .padding(Spacing.xxl)
        .frame(maxWidth: .infinity)
    }
}

/// Cached times are still shown when a refresh fails — but they are labelled rather
/// than passed off as live.
struct StaleDataNotice: View {
    @Environment(\.colors) private var colors

    let title: String

    var body: some View {
        Text("state.staleData".localized(title))
            .appText(AppType.bodySmall)
            .foregroundStyle(colors.onErrorContainer)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(Spacing.md)
            .background(
                colors.errorContainer.opacity(0.6),
                in: RoundedRectangle(cornerRadius: Radii.md, style: .continuous)
            )
    }
}

extension View {
    /// The app's standard card: surface colour, rounded, comfortable padding.
    func cardSurface(
        _ colors: AppColors,
        cornerRadius: CGFloat = Radii.md,
        padding: CGFloat = Spacing.lg
    ) -> some View {
        self
            .padding(padding)
            .background(
                colors.surface,
                in: RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
            )
    }
}
