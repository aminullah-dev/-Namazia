import SwiftUI

/// Phase 1 smoke test.
///
/// This screen exists to prove four things work before any real feature is built on
/// top of them, because each one fails silently rather than loudly:
///   1. the bundled Vazirmatn faces actually load (a missing font falls back to the
///      system face and just looks slightly off, with no error),
///   2. the palette resolves in both light and dark,
///   3. layout is mirrored right-to-left,
///   4. Perso-Arabic digits and joined script render correctly.
///
/// It is replaced by the real home screen in Phase 3.
struct RootView: View {
    @Environment(\.colors) private var colors

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: Spacing.xl) {
                header

                section("وزن‌های فونت") {
                    row("سبک — Light", AppType.displayMedium)
                    row("معمولی — Regular", AppType.bodyLarge)
                    row("متوسط — Medium", AppType.titleMedium)
                    row("ضخیم — Bold", AppType.headlineSmall)
                }

                section("ارقام فارسی") {
                    Text("۰۱۲۳۴۵۶۷۸۹")
                        .appText(AppType.headlineMedium)
                    Text("اذان مغرب: \("18:35".persianDigits)")
                        .appText(AppType.bodyLarge)
                }

                section("متن عربی") {
                    Text("سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ")
                        .appText(AppType.arabicVerse)
                        .frame(maxWidth: .infinity, alignment: .trailing)
                }

                section("پالت رنگ") {
                    swatches
                }

                Text("اگر این صفحه از راست چیده شده، فونت‌ها متفاوت‌اند و ارقام فارسی‌اند — فاز ۱ موفق بوده است.")
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
