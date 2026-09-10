import Foundation

// MARK: - aladhan API payloads
//
// Only the fields the app actually uses are declared. `JSONDecoder` ignores unknown
// keys, so the API adding fields (it does, between minor versions) cannot break
// decoding — but a field declared here and later removed upstream *would*, which is
// why nothing speculative is listed.

struct TimingsResponse: Decodable {
    let code: Int
    let status: String
    let data: PrayerData
}

struct CalendarResponse: Decodable {
    let code: Int
    let status: String
    let data: [PrayerData]
}

struct PrayerData: Decodable {
    let timings: Timings
    let date: DateInfo
}

/// The API's own key casing is capitalised (`"Fajr"`), so the CodingKeys are explicit
/// rather than relying on a conversion strategy.
struct Timings: Decodable {
    let fajr: String
    let sunrise: String
    let dhuhr: String
    let asr: String
    let maghrib: String
    let isha: String

    private enum CodingKeys: String, CodingKey {
        case fajr = "Fajr"
        case sunrise = "Sunrise"
        case dhuhr = "Dhuhr"
        case asr = "Asr"
        case maghrib = "Maghrib"
        case isha = "Isha"
    }
}

struct DateInfo: Decodable {
    let readable: String
    let hijri: HijriDate
    let gregorian: GregorianDate
}

struct HijriDate: Decodable {
    let date: String        // dd-MM-yyyy
    let day: String
    let month: HijriMonth
    let year: String
}

struct HijriMonth: Decodable {
    let number: Int
    let en: String
    let ar: String
}

struct GregorianDate: Decodable {
    let date: String        // dd-MM-yyyy
    let day: String
    let month: GregorianMonth
    let year: String
}

struct GregorianMonth: Decodable {
    let number: Int
    let en: String
}

// MARK: - What the app stores and renders

/// One day of prayer times for one city — the equivalent of the Android Room entity.
///
/// Times are kept as the API's `"HH:mm"` strings rather than `Date`s on purpose: they
/// are wall-clock times in Kabul, and storing them as absolute instants would make the
/// cache wrong for anyone whose device timezone changes. `absoluteTime(for:)` resolves
/// them to a real instant when one is needed.
struct DayPrayerTimes: Codable, Equatable, Identifiable {
    /// `yyyy-MM-dd` in Kabul time.
    let day: String
    /// `AfghanCity.nameEn` — cities are keyed by their stable English name, not by
    /// their index in the list, so inserting a city later cannot silently repoint the
    /// cache at another city's times.
    let city: String

    let fajr: String
    let sunrise: String
    let dhuhr: String
    let asr: String
    let maghrib: String
    let isha: String

    /// Pre-rendered Hijri date, e.g. "۱۲ رمضان ۱۴۴۷" once digits are converted.
    let hijriDate: String

    let cachedAt: Date

    var id: String { CacheKey.make(day: day, city: city) }

    func time(for prayer: PrayerName) -> String {
        switch prayer {
        case .fajr: return fajr
        case .sunrise: return sunrise
        case .dhuhr: return dhuhr
        case .asr: return asr
        case .maghrib: return maghrib
        case .isha: return isha
        }
    }
}

enum CacheKey {
    static func make(day: String, city: String) -> String { "\(city)|\(day)" }
}

enum PrayerName: String, Codable, CaseIterable, Identifiable {
    case fajr, sunrise, dhuhr, asr, maghrib, isha

    var id: String { rawValue }

    var dari: String {
        switch self {
        case .fajr: return "فجر"
        case .sunrise: return "طلوع آفتاب"
        case .dhuhr: return "ظهر"
        case .asr: return "عصر"
        case .maghrib: return "مغرب"
        case .isha: return "عشا"
        }
    }

    var english: String {
        switch self {
        case .fajr: return "Fajr"
        case .sunrise: return "Sunrise"
        case .dhuhr: return "Dhuhr"
        case .asr: return "Asr"
        case .maghrib: return "Maghrib"
        case .isha: return "Isha"
        }
    }

    /// Sunrise is a time marker, not a prayer — nothing calls the azan for it.
    var callsAzan: Bool { self != .sunrise }
}

// MARK: - Cities

struct AfghanCity: Codable, Equatable, Identifiable, Hashable {
    let nameDari: String
    let nameEn: String
    let latitude: Double
    let longitude: Double

    var id: String { nameEn }
}

enum AfghanCities {
    static let list: [AfghanCity] = [
        AfghanCity(nameDari: "کابل", nameEn: "Kabul", latitude: 34.5553, longitude: 69.2075),
        AfghanCity(nameDari: "هرات", nameEn: "Herat", latitude: 34.3529, longitude: 62.2040),
        AfghanCity(nameDari: "مزار شریف", nameEn: "Mazar-i-Sharif", latitude: 36.7069, longitude: 67.1100),
        AfghanCity(nameDari: "قندهار", nameEn: "Kandahar", latitude: 31.6289, longitude: 65.7372),
        AfghanCity(nameDari: "جلال‌آباد", nameEn: "Jalalabad", latitude: 34.4415, longitude: 70.4360),
        AfghanCity(nameDari: "کندز", nameEn: "Kunduz", latitude: 36.7285, longitude: 68.8571),
        AfghanCity(nameDari: "بامیان", nameEn: "Bamyan", latitude: 34.8203, longitude: 67.8294),
        AfghanCity(nameDari: "غزنی", nameEn: "Ghazni", latitude: 33.5450, longitude: 68.4231),
        AfghanCity(nameDari: "لشکرگاه", nameEn: "Lashkar Gah", latitude: 31.5933, longitude: 64.3599),
        AfghanCity(nameDari: "تالقان", nameEn: "Taloqan", latitude: 36.7364, longitude: 69.5391),
        AfghanCity(nameDari: "پل‌خمری", nameEn: "Pul-e-Khumri", latitude: 35.9439, longitude: 68.7152),
        AfghanCity(nameDari: "میمنه", nameEn: "Maimana", latitude: 35.9231, longitude: 64.7686),
        AfghanCity(nameDari: "شبرغان", nameEn: "Sheberghan", latitude: 36.6700, longitude: 65.7500),
        AfghanCity(nameDari: "زرنج", nameEn: "Zaranj", latitude: 30.9587, longitude: 61.8686),
        AfghanCity(nameDari: "فیض‌آباد", nameEn: "Fayzabad", latitude: 37.1194, longitude: 70.5797)
    ]

    static let `default` = list[0]

    static func at(_ index: Int) -> AfghanCity {
        list.indices.contains(index) ? list[index] : `default`
    }
}

// MARK: - Calculation choices

/// Fiqh school for the Asr calculation. The API calls this `school`: 0 = Shafi and the
/// other three schools, 1 = Hanafi. It shifts Asr by roughly an hour, so the default
/// matters — most Afghans are Hanafi.
struct Madhab: Identifiable, Equatable {
    let school: Int
    let nameDari: String

    var id: Int { school }
}

enum Madhabs {
    static let list = [
        Madhab(school: 1, nameDari: "حنفی"),
        Madhab(school: 0, nameDari: "شافعی / مالکی / حنبلی")
    ]

    static func name(of school: Int) -> String {
        list.first { $0.school == school }?.nameDari ?? "حنفی"
    }
}

struct CalcMethod: Identifiable, Equatable {
    let id: Int
    let nameDari: String
}

enum CalcMethods {
    static let list = [
        CalcMethod(id: 1, nameDari: "دانشگاه علوم اسلامی کراچی"),
        CalcMethod(id: 3, nameDari: "اتحادیه جهانی مسلمانان"),
        CalcMethod(id: 4, nameDari: "ام القری مکه"),
        CalcMethod(id: 2, nameDari: "انجمن اسلامی امریکای شمالی"),
        CalcMethod(id: 5, nameDari: "سازمان عمومی مساحی مصر")
    ]

    static func name(of id: Int) -> String {
        list.first { $0.id == id }?.nameDari ?? "پیش‌فرض"
    }
}

// MARK: - Settings

struct AppSettings: Equatable {
    var cityIndex: Int = 0
    var calculationMethod: Int = 3
    var asrSchool: Int = 1
    var fajrEnabled: Bool = true
    var dhuhrEnabled: Bool = true
    var asrEnabled: Bool = true
    var maghribEnabled: Bool = true
    var ishaEnabled: Bool = true
    var sunriseEnabled: Bool = false
    var reminderMinutes: Int = 15
    var vibrationEnabled: Bool = true
    var darkMode: Bool = false

    var city: AfghanCity { AfghanCities.at(cityIndex) }

    /// The three settings that decide *which* times get fetched. Everything else —
    /// which azans are on, vibration, theme — changes how they are presented, not what
    /// they are, so only a change to this identity is worth a refetch.
    var calculationIdentity: String { "\(cityIndex)-\(calculationMethod)-\(asrSchool)" }

    func isEnabled(_ prayer: PrayerName) -> Bool {
        switch prayer {
        case .fajr: return fajrEnabled
        case .sunrise: return sunriseEnabled
        case .dhuhr: return dhuhrEnabled
        case .asr: return asrEnabled
        case .maghrib: return maghribEnabled
        case .isha: return ishaEnabled
        }
    }

    mutating func setEnabled(_ prayer: PrayerName, _ enabled: Bool) {
        switch prayer {
        case .fajr: fajrEnabled = enabled
        case .sunrise: sunriseEnabled = enabled
        case .dhuhr: dhuhrEnabled = enabled
        case .asr: asrEnabled = enabled
        case .maghrib: maghribEnabled = enabled
        case .isha: ishaEnabled = enabled
        }
    }
}
