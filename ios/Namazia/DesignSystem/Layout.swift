import SwiftUI

/// One spacing and shape scale for the whole app, mirroring `ui/theme/Dimens.kt`.
/// Screens use these instead of inventing their own numbers, which is what keeps the
/// rhythm consistent across tabs.
enum Spacing {
    static let xs: CGFloat = 4
    static let sm: CGFloat = 8
    static let md: CGFloat = 12
    static let lg: CGFloat = 16
    static let xl: CGFloat = 24
    static let xxl: CGFloat = 32

    /// Apple's minimum comfortable hit target. Anything tappable should reach it.
    static let touchTarget: CGFloat = 44
}

enum Radii {
    static let sm: CGFloat = 10
    static let md: CGFloat = 14
    static let lg: CGFloat = 20
    static let xl: CGFloat = 28
}

extension View {
    /// Paints `color` behind the status bar. A screen that scrolls edge to edge with no
    /// header of its own otherwise runs its rows straight through the clock. The inset
    /// is zero high, so the layout does not move; only its background reaches up into
    /// the safe area.
    func statusBarBackdrop(_ color: Color) -> some View {
        safeAreaInset(edge: .top, spacing: 0) {
            Color.clear.frame(height: 0).background(color)
        }
    }
}

/// Converts Latin digits to Perso-Arabic ones, matching `utils/Format.kt`.
///
/// Prayer times arrive from the API as "05:14"; showing them as ۰۵:۱۴ is what makes
/// the screen read as Dari rather than as a translated English app.
extension String {
    var persianDigits: String {
        let table: [Character: Character] = [
            "0": "۰", "1": "۱", "2": "۲", "3": "۳", "4": "۴",
            "5": "۵", "6": "۶", "7": "۷", "8": "۸", "9": "۹"
        ]
        return String(map { table[$0] ?? $0 })
    }
}

extension Int {
    var persianDigits: String { String(self).persianDigits }
}
