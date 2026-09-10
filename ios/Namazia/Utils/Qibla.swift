import Foundation

/// Direction to the Kaaba, ported from `utils/QiblaUtil.kt`.
enum Qibla {
    static let kaabaLatitude = 21.4225
    static let kaabaLongitude = 39.8262

    /// Great-circle initial bearing to the Kaaba, in degrees clockwise from true north.
    ///
    /// Not a straight line on a flat map: over this distance the shortest path across a
    /// sphere leaves at a noticeably different angle from what a Mercator projection
    /// would suggest, which is why every serious qibla calculation uses this formula.
    static func bearing(latitude: Double, longitude: Double) -> Double {
        let phi1 = latitude * .pi / 180
        let phi2 = kaabaLatitude * .pi / 180
        let deltaLon = (kaabaLongitude - longitude) * .pi / 180

        let y = sin(deltaLon) * cos(phi2)
        let x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLon)

        let degrees = atan2(y, x) * 180 / .pi
        return (degrees + 360).truncatingRemainder(dividingBy: 360)
    }

    /// A compass bearing as a Dari direction name, for the case where the phone has no
    /// usable compass and the only thing left is to tell the user which way to face.
    static func directionName(_ bearing: Double) -> String {
        let names = ["شمال", "شمال‌شرق", "شرق", "جنوب‌شرق", "جنوب", "جنوب‌غرب", "غرب", "شمال‌غرب"]
        let index = Int((bearing / 45).rounded()) % 8
        return names[index]
    }
}
