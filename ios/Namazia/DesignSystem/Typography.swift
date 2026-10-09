import SwiftUI

/// A text style: a font plus the line spacing that goes with it.
///
/// Perso-Arabic sits taller than Latin and carries descenders that the stock metrics
/// clip, so every style here is given deliberately generous leading — the same reason
/// the Android side sets a ~1.6x `lineHeight` on each style.
///
/// The catch when porting those numbers: Android's `lineHeight` is the *total* height
/// of a line, but SwiftUI's `lineSpacing` is the *extra* gap added between lines. So
/// the Kotlin values cannot be copied across directly; `lineSpacing` below is derived
/// as `targetLineHeight - naturalLineHeight`, with the natural height approximated at
/// 1.25x the point size.
struct AppTextStyle {
    let font: Font
    let lineSpacing: CGFloat

    init(
        weight: VazirmatnWeight,
        size: CGFloat,
        lineHeight: CGFloat,
        relativeTo textStyle: Font.TextStyle
    ) {
        // `relativeTo:` opts the custom font into Dynamic Type, so the app still
        // responds to the reader's chosen text size instead of being locked at 100%.
        self.font = .custom(weight.postScriptName, size: size, relativeTo: textStyle)
        self.lineSpacing = max(0, lineHeight - size * 1.25)
    }
}

/// The four bundled weights. The raw values are the fonts' PostScript names, which is
/// what `Font.custom` matches on — not the filename and not the family name.
enum VazirmatnWeight {
    case light, regular, medium, bold

    var postScriptName: String {
        switch self {
        case .light: return "Vazirmatn-Light"
        case .regular: return "Vazirmatn-Regular"
        case .medium: return "Vazirmatn-Medium"
        case .bold: return "Vazirmatn-Bold"
        }
    }
}

enum AppType {

    // Display — the countdown clock and the tasbih counter
    static let displayLarge = AppTextStyle(weight: .light, size: 52, lineHeight: 64, relativeTo: .largeTitle)
    static let displayMedium = AppTextStyle(weight: .light, size: 42, lineHeight: 54, relativeTo: .largeTitle)

    // Headline
    static let headlineLarge = AppTextStyle(weight: .bold, size: 28, lineHeight: 40, relativeTo: .title)
    static let headlineMedium = AppTextStyle(weight: .bold, size: 24, lineHeight: 36, relativeTo: .title2)
    static let headlineSmall = AppTextStyle(weight: .medium, size: 20, lineHeight: 30, relativeTo: .title3)

    // Title
    static let titleLarge = AppTextStyle(weight: .bold, size: 19, lineHeight: 30, relativeTo: .headline)
    static let titleMedium = AppTextStyle(weight: .medium, size: 17, lineHeight: 27, relativeTo: .headline)
    static let titleSmall = AppTextStyle(weight: .medium, size: 15, lineHeight: 24, relativeTo: .subheadline)

    // Body
    static let bodyLarge = AppTextStyle(weight: .regular, size: 16, lineHeight: 27, relativeTo: .body)
    static let bodyMedium = AppTextStyle(weight: .regular, size: 14, lineHeight: 24, relativeTo: .body)
    static let bodySmall = AppTextStyle(weight: .regular, size: 12, lineHeight: 20, relativeTo: .caption)

    // Label
    static let labelLarge = AppTextStyle(weight: .medium, size: 14, lineHeight: 22, relativeTo: .callout)
    static let labelMedium = AppTextStyle(weight: .medium, size: 12, lineHeight: 18, relativeTo: .caption)
    static let labelSmall = AppTextStyle(weight: .regular, size: 11, lineHeight: 16, relativeTo: .caption2)

    /// Qur'anic and du'a Arabic needs more room again, and reads better a size up.
    static let arabicVerse = AppTextStyle(weight: .medium, size: 19, lineHeight: 38, relativeTo: .body)
}

extension View {
    /// Applies a style's font and its leading together — using `.font()` alone would
    /// silently drop the line spacing that makes the script legible.
    func appText(_ style: AppTextStyle) -> some View {
        self
            .font(style.font)
            .lineSpacing(style.lineSpacing)
    }
}
