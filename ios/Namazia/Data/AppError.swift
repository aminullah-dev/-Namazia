import Foundation

/// Machine-readable failure codes, mirroring `data/AppError.kt` on Android.
///
/// State carries one of these, never a rendered sentence, and never the underlying
/// system error's text — `URLError`'s description is English and technical, which is
/// exactly what a Dari-speaking user should not be shown. The `detail` is for logging.
enum AppError: Error, Equatable {
    /// No usable network and nothing cached for the requested day.
    case offlineNoCache(detail: String?)

    /// Reached the API but it answered with a non-success status.
    case server(detail: String?)

    /// Reached the API but the payload was not what we expect.
    case badResponse(detail: String?)

    /// Anything we could not classify.
    case unknown(detail: String?)

    /// Reduces any thrown error to one of the four codes.
    ///
    /// The `URLError` list is the set of codes that all mean the same thing to a user —
    /// "you are not online" — even though they arise from different layers.
    static func classify(_ error: Error) -> AppError {
        if let appError = error as? AppError { return appError }

        if error is DecodingError {
            return .badResponse(detail: "\(error)")
        }

        if let urlError = error as? URLError {
            switch urlError.code {
            case .notConnectedToInternet,
                 .networkConnectionLost,
                 .timedOut,
                 .cannotFindHost,
                 .cannotConnectToHost,
                 .dnsLookupFailed,
                 .dataNotAllowed,
                 .internationalRoamingOff,
                 .secureConnectionFailed:
                return .offlineNoCache(detail: urlError.code.rawValue.description)
            default:
                return .unknown(detail: urlError.code.rawValue.description)
            }
        }

        return .unknown(detail: error.localizedDescription)
    }
}

/// Resolved at the edge, in the user's language. The call sites still pass a code and
/// never a sentence, which is the point of the enum.
extension AppError {
    var title: String {
        switch self {
        case .offlineNoCache: return "error.offline.title".localized
        case .server: return "error.server.title".localized
        case .badResponse: return "error.badResponse.title".localized
        case .unknown: return "error.unknown.title".localized
        }
    }

    var message: String {
        switch self {
        case .offlineNoCache:
            return "error.offline.body".localized
        case .server:
            return "error.server.body".localized
        case .badResponse:
            return "error.badResponse.body".localized
        case .unknown:
            return "error.unknown.body".localized
        }
    }

    /// Technical text for the console only — never rendered.
    var detail: String? {
        switch self {
        case .offlineNoCache(let detail),
             .server(let detail),
             .badResponse(let detail),
             .unknown(let detail):
            return detail
        }
    }
}
