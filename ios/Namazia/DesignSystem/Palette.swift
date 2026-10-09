import SwiftUI

/// The same lapis-and-gold palette as the Android app, so the two platforms are
/// visibly one product. Hex values are copied from `ui/theme/Theme.kt`.
enum Palette {

    // MARK: - Brand

    static let lapis = Color(hex: 0x1A3A6B)
    static let lapisLight = Color(hex: 0x2D5FAA)
    static let moonlight = Color(hex: 0xF5F2EB)
    static let gold = Color(hex: 0xBFA15C)
    static let goldSoft = Color(hex: 0xD9C48A)

    /// Gradient used behind the mosque mark on the icon and the home hero card.
    static let gradientStart = Color(hex: 0x2C5CA5)
    static let gradientEnd = Color(hex: 0x091C48)
}

/// Semantic colours. Each resolves against the current colour scheme, which is why
/// they are computed from the environment rather than being plain constants.
struct AppColors {
    let scheme: ColorScheme

    private func pick(light: UInt32, dark: UInt32) -> Color {
        Color(hex: scheme == .dark ? dark : light)
    }

    var primary: Color { pick(light: 0x1A3A6B, dark: 0x9EBEFF) }
    var onPrimary: Color { pick(light: 0xFFFFFF, dark: 0x00305F) }
    var primaryContainer: Color { pick(light: 0xDDE7FA, dark: 0x1B4585) }
    var onPrimaryContainer: Color { pick(light: 0x0A1F45, dark: 0xD9E4FF) }

    var secondary: Color { pick(light: 0x2D5FAA, dark: 0xAFC7F5) }

    var tertiary: Color { pick(light: 0x8A7434, dark: 0xD9C48A) }
    var tertiaryContainer: Color { pick(light: 0xF6EDD6, dark: 0x564519) }
    var onTertiaryContainer: Color { pick(light: 0x3D3312, dark: 0xF6E7BE) }

    var background: Color { pick(light: 0xF5F2EB, dark: 0x11151E) }
    var onBackground: Color { pick(light: 0x1B1B20, dark: 0xE4E2DC) }

    var surface: Color { pick(light: 0xFFFFFF, dark: 0x171D29) }
    var onSurface: Color { pick(light: 0x1B1B20, dark: 0xE4E2DC) }
    var surfaceVariant: Color { pick(light: 0xE8E3D9, dark: 0x262E3D) }
    var onSurfaceVariant: Color { pick(light: 0x4A463E, dark: 0xC6C2B9) }

    var outline: Color { pick(light: 0x7C786F, dark: 0x8C8A83) }
    var outlineVariant: Color { pick(light: 0xD2CCC0, dark: 0x3A4150) }

    var error: Color { pick(light: 0xB3261E, dark: 0xF2B8B5) }
    var errorContainer: Color { pick(light: 0xF9DEDC, dark: 0x8C1D18) }
    var onErrorContainer: Color { pick(light: 0x410E0B, dark: 0xF9DEDC) }
}

private struct AppColorsKey: EnvironmentKey {
    static let defaultValue = AppColors(scheme: .light)
}

extension EnvironmentValues {
    var colors: AppColors {
        get { self[AppColorsKey.self] }
        set { self[AppColorsKey.self] = newValue }
    }
}

extension Color {
    /// `Color(hex: 0x1A3A6B)` — keeps the palette readable next to the Kotlin original.
    init(hex: UInt32) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255,
            green: Double((hex >> 8) & 0xFF) / 255,
            blue: Double(hex & 0xFF) / 255,
            opacity: 1
        )
    }
}
