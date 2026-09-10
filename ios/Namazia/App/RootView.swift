import SwiftUI

/// Development scaffold, replaced by the real home screen in Phase 3.
///
/// It exists to prove the things that fail *silently* rather than loudly:
///   1. the bundled Vazirmatn faces load (a missing font falls back to the system face
///      and just looks slightly off, with no error),
///   2. the palette resolves in both light and dark,
///   3. layout is mirrored right-to-left, and Perso-Arabic renders,
///   4. the data layer works end to end — network, decode, cache, settings, timing.
struct RootView: View {
    @Environment(\.colors) private var colors

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: Spacing.xl) {
                header

                section("اوقات امروز — از سرور") {
                    DataProbeView()
                }

                section("فونت، ارقام و رنگ") {
                    row("سبک — Light", AppType.displayMedium)
                    row("معمولی — Regular", AppType.bodyLarge)
                    row("ضخیم — Bold", AppType.headlineSmall)
                    Text("سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ")
                        .appText(AppType.arabicVerse)
                        .frame(maxWidth: .infinity, alignment: .trailing)
                    swatches
                }

                Text("اگر اوقات نماز بالا آمد، کش کار می‌کند و شمارش معکوس پیش می‌رود — فاز ۲ موفق بوده است.")
                    .appText(AppType.bodySmall)
                    .foregroundStyle(colors.onSurfaceVariant)
            }
            .padding(Spacing.lg)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .background(colors.background)
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: Spacing.xs) {
            Text("اوقات نماز")
                .appText(AppType.headlineLarge)
                .foregroundStyle(colors.primary)
            Text("Namazia — iOS")
                .appText(AppType.labelMedium)
                .foregroundStyle(colors.onSurfaceVariant)
        }
    }

    private func section<Content: View>(
        _ title: String,
        @ViewBuilder content: () -> Content
    ) -> some View {
        VStack(alignment: .leading, spacing: Spacing.sm) {
            Text(title)
                .appText(AppType.labelLarge)
                .foregroundStyle(colors.primary)

            VStack(alignment: .leading, spacing: Spacing.sm) {
                content()
            }
            .padding(Spacing.lg)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(colors.surface)
            .clipShape(RoundedRectangle(cornerRadius: Radii.lg, style: .continuous))
        }
    }

    private func row(_ text: String, _ style: AppTextStyle) -> some View {
        Text(text)
            .appText(style)
            .foregroundStyle(colors.onSurface)
    }

    private var swatches: some View {
        HStack(spacing: Spacing.sm) {
            swatch(colors.primary, "اصلی")
            swatch(colors.tertiary, "طلایی")
            swatch(colors.surfaceVariant, "سطح")
            swatch(colors.error, "خطا")
        }
    }

    private func swatch(_ color: Color, _ label: String) -> some View {
        VStack(spacing: Spacing.xs) {
            RoundedRectangle(cornerRadius: Radii.sm, style: .continuous)
                .fill(color)
                .frame(height: 48)
            Text(label)
                .appText(AppType.labelSmall)
                .foregroundStyle(colors.onSurfaceVariant)
        }
        .frame(maxWidth: .infinity)
    }
}

#Preview("Light") {
    RootView().themed()
}

#Preview("Dark") {
    RootView().themed().preferredColorScheme(.dark)
}
