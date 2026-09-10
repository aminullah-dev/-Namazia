import Foundation

/// On-disk cache of fetched days, standing in for Android's Room table.
///
/// A single JSON file rather than a database: the whole cache is a few hundred rows of
/// six short strings, one write per fetch, and it lives in the App Group container so
/// the widget can render prayer times without a network call of its own.
///
/// It is an `actor` because the app, and later the widget's timeline provider, both
/// touch it — serialising access is what keeps a half-written file from ever existing.
actor PrayerTimesCache {

    private let fileURL: URL
    private var entries: [String: DayPrayerTimes]?

    /// Days kept either side of today. Wide enough for the monthly calendar to stay
    /// browsable offline, narrow enough that the file stays small.
    private let retentionDays = 45

    init(fileURL: URL = AppGroup.containerURL.appendingPathComponent("prayer-cache.json")) {
        self.fileURL = fileURL
    }

    func get(day: String, city: String) -> DayPrayerTimes? {
        loaded()[CacheKey.make(day: day, city: city)]
    }

    func put(_ times: DayPrayerTimes) {
        var current = loaded()
        current[times.id] = times
        entries = current
        persist(current)
    }

    func put(_ batch: [DayPrayerTimes]) {
        guard !batch.isEmpty else { return }
        var current = loaded()
        for times in batch { current[times.id] = times }
        entries = current
        persist(current)
    }

    /// Every cached day for one city, oldest first — what the calendar screen reads.
    func days(for city: String) -> [DayPrayerTimes] {
        loaded().values
            .filter { $0.city == city }
            .sorted { $0.day < $1.day }
    }

    /// Wipes everything. Called when the calculation method or fiqh school changes,
    /// because every cached time was computed with the old ones.
    func clear() {
        entries = [:]
        try? FileManager.default.removeItem(at: fileURL)
    }

    /// Drops days far enough from today that nothing on screen can reach them.
    func prune(now: Date = Date()) {
        let oldest = AppTime.dayKey(byAddingDays: -retentionDays, to: now)
        let newest = AppTime.dayKey(byAddingDays: retentionDays, to: now)

        let current = loaded()
        let kept = current.filter { $0.value.day >= oldest && $0.value.day <= newest }
        guard kept.count != current.count else { return }

        entries = kept
        persist(kept)
    }

    // MARK: - Disk

    private func loaded() -> [String: DayPrayerTimes] {
        if let entries { return entries }

        guard let data = try? Data(contentsOf: fileURL) else {
            entries = [:]
            return [:]
        }

        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601

        // A cache that fails to decode is a cache whose shape changed. Starting empty
        // costs one network request; refusing to start would break the app.
        let decoded = (try? decoder.decode([String: DayPrayerTimes].self, from: data)) ?? [:]
        entries = decoded
        return decoded
    }

    private func persist(_ value: [String: DayPrayerTimes]) {
        let encoder = JSONEncoder()
        encoder.dateEncodingStrategy = .iso8601

        guard let data = try? encoder.encode(value) else { return }
        // `.atomic` writes to a temporary file and renames, so a crash mid-write leaves
        // the previous cache intact rather than a truncated file.
        try? data.write(to: fileURL, options: .atomic)
    }
}
