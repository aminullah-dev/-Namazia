import Foundation

/// Thin client over the aladhan API — the same two endpoints the Android app uses, so
/// both platforms show identical times for the same city and settings.
struct PrayerTimesAPI {

    static let baseURL = URL(string: "https://api.aladhan.com")!

    private let session: URLSession

    init(session: URLSession? = nil) {
        self.session = session ?? PrayerTimesAPI.makeSession()
    }

    /// Parses `yyyy-MM-dd` as midnight UTC — deliberately *not* `AppTime.dayFormatter`,
    /// which parses in Kabul time and would give a timestamp seven and a half hours off.
    private static let utcDayFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.calendar = Calendar(identifier: .gregorian)
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.timeZone = TimeZone(secondsFromGMT: 0)
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter
    }()

    private static func makeSession() -> URLSession {
        let configuration = URLSessionConfiguration.default
        // A prayer-times request is small and the app has a cache to fall back on, so a
        // long spinner is worse than a quick failure.
        configuration.timeoutIntervalForRequest = 15
        configuration.timeoutIntervalForResource = 30
        // `waitsForConnectivity` would hold the request open until the phone comes back
        // online; here we want the offline path to be reached immediately so cached
        // times (or the offline message) appear at once.
        configuration.waitsForConnectivity = false
        configuration.requestCachePolicy = .reloadIgnoringLocalCacheData
        return URLSession(configuration: configuration)
    }

    /// One day, by coordinates.
    func timings(
        day: String,
        city: AfghanCity,
        method: Int,
        school: Int
    ) async throws -> PrayerData {
        // The API derives the date from this timestamp; Android builds it the same way
        // (midnight *UTC* of the requested day) so both platforms ask for the same day.
        guard let midnightUTC = PrayerTimesAPI.utcDayFormatter.date(from: day) else {
            throw AppError.unknown(detail: "bad day key \(day)")
        }
        let timestamp = Int(midnightUTC.timeIntervalSince1970)

        let response: TimingsResponse = try await get(
            path: "/v1/timings/\(timestamp)",
            query: locationQuery(city: city, method: method, school: school)
        )
        return response.data
    }

    /// A whole month, used by the calendar screen and to warm the cache cheaply.
    func monthlyCalendar(
        year: Int,
        month: Int,
        city: AfghanCity,
        method: Int,
        school: Int
    ) async throws -> [PrayerData] {
        let response: CalendarResponse = try await get(
            path: "/v1/calendar/\(year)/\(month)",
            query: locationQuery(city: city, method: method, school: school)
        )
        return response.data
    }

    // MARK: - Plumbing

    private func locationQuery(city: AfghanCity, method: Int, school: Int) -> [URLQueryItem] {
        [
            URLQueryItem(name: "latitude", value: String(city.latitude)),
            URLQueryItem(name: "longitude", value: String(city.longitude)),
            URLQueryItem(name: "method", value: String(method)),
            // 0 = Shafi/Maliki/Hanbali, 1 = Hanafi. Wrong value here means Asr is off
            // by about an hour, which is the kind of bug users notice immediately.
            URLQueryItem(name: "school", value: String(school))
        ]
    }

    private func get<T: Decodable>(path: String, query: [URLQueryItem]) async throws -> T {
        // The path is set on the components rather than appended to the URL: appending
        // percent-encodes the slashes in some Foundation versions, which turns a valid
        // endpoint into a 404 that looks like a server outage.
        var components = URLComponents(url: PrayerTimesAPI.baseURL, resolvingAgainstBaseURL: false)
        components?.path = path
        components?.queryItems = query

        guard let url = components?.url else {
            throw AppError.unknown(detail: "could not build URL for \(path)")
        }

        let data: Data
        let response: URLResponse
        do {
            (data, response) = try await session.data(from: url)
        } catch {
            // Transport failures are all "no network" from the user's point of view;
            // whether the cache can rescue this is decided one layer up.
            throw AppError.classify(error)
        }

        guard let http = response as? HTTPURLResponse else {
            throw AppError.badResponse(detail: "non-HTTP response")
        }
        guard (200..<300).contains(http.statusCode) else {
            throw AppError.server(detail: "HTTP \(http.statusCode)")
        }

        do {
            return try JSONDecoder().decode(T.self, from: data)
        } catch {
            throw AppError.badResponse(detail: "\(error)")
        }
    }
}
