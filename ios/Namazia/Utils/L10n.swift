import Foundation

/// The two languages the app ships in.
///
/// Both are Afghan national languages and both are right-to-left, so switching between
/// them changes wording only — never layout.
enum AppLanguage: String, CaseIterable, Identifiable {
    case dari = "fa"
    case pashto = "ps"

    var id: String { rawValue }

    /// Each language's name written in itself, which is how a language picker should
    /// always read: someone looking for Pashto is looking for the word «پښتو».
    var nativeName: String {
        switch self {
        case .dari: return "دری"
        case .pashto: return "پښتو"
        }
    }
}

/// Resolves strings against the language the user picked, rather than the phone's.
///
/// This file sits in `Utils/` rather than next to the `.lproj` folders on purpose: a
/// directory listed as a target's source is treated as a resource group once it
/// contains `.lproj` subfolders, and a Swift file inside it is then never compiled into
/// that target — which is exactly how the widget ended up without this lookup.
///
/// iOS normally decides this from the device language list, which is the wrong owner
/// here: an Afghan phone is often set to English, and someone who wants the app in
/// Pashto should not have to change their whole device to get it. So the app carries
/// its own choice and looks strings up in that language's bundle directly.
///
/// The bundles are still real `.lproj` resources, so nothing else is given up: the
/// system can still pick a sensible default on first launch, and the app appears with
/// its languages in iOS Settings.
enum L10n {
    private(set) static var bundle: Bundle = .main

    /// Called once at launch and again whenever the language changes. Views re-read
    /// their strings on the next render, which the settings store triggers by
    /// publishing — so the whole UI switches language without a relaunch.
    static func use(_ language: AppLanguage) {
        guard
            let path = Bundle.main.path(forResource: language.rawValue, ofType: "lproj"),
            let localized = Bundle(path: path)
        else {
            // Missing .lproj means a packaging mistake, not a user-facing situation.
            // Falling back to the main bundle keeps the app readable rather than
            // filling every label with raw keys.
            bundle = .main
            return
        }
        bundle = localized
    }

    static func string(_ key: String) -> String {
        bundle.localizedString(forKey: key, value: nil, table: nil)
    }
}

extension String {
    /// `"home.title".localized` — the key itself is returned when a translation is
    /// missing, which makes the gap obvious on screen instead of silently blank.
    var localized: String {
        L10n.string(self)
    }

    /// For the handful of strings that take a value, e.g. "%@ دقیقه تا %@".
    func localized(_ arguments: CVarArg...) -> String {
        String(format: L10n.string(self), arguments: arguments)
    }
}
