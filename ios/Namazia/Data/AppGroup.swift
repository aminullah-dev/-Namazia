import Foundation

/// The shared container the app and (later) the widget both read.
///
/// Both accessors fall back to app-private storage when the group is unavailable. That
/// happens more often than you would expect: the App Group identifier has to exist on
/// the developer portal and be in the provisioning profile, and until it is,
/// `containerURL(forSecurityApplicationGroupIdentifier:)` simply returns `nil`. Falling
/// back means the app keeps working with its own copy of the data and only the widget
/// goes stale — far better than crashing on a force-unwrap at launch.
enum AppGroup {
    static let identifier = "group.af.namazia.app"

    static let isAvailable: Bool =
        FileManager.default.containerURL(forSecurityApplicationGroupIdentifier: identifier) != nil

    static var defaults: UserDefaults {
        UserDefaults(suiteName: identifier) ?? .standard
    }

    /// Directory for the app's own files, created on first use.
    static var containerURL: URL {
        let base = FileManager.default
            .containerURL(forSecurityApplicationGroupIdentifier: identifier)
            ?? FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask)[0]

        let directory = base.appendingPathComponent("Namazia", isDirectory: true)
        if !FileManager.default.fileExists(atPath: directory.path) {
            try? FileManager.default.createDirectory(
                at: directory,
                withIntermediateDirectories: true
            )
        }
        return directory
    }
}
