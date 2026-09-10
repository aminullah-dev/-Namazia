import SwiftUI
import UIKit
import CoreLocation

/// Live compass heading.
///
/// The qibla *bearing* comes from the selected city's coordinates and needs no
/// permission at all — only the phone's own orientation does. That split matters: if
/// location is refused, the screen still tells the user which way to face, it just
/// cannot rotate the dial for them.
@MainActor
final class HeadingProvider: NSObject, ObservableObject {

    /// Degrees clockwise from true north, or `nil` before the first reading.
    @Published private(set) var heading: Double?
    @Published private(set) var authorization: CLAuthorizationStatus
    @Published private(set) var accuracy: CLLocationDirection = -1

    /// Simulators have no magnetometer, and neither do some devices.
    let hasCompass = CLLocationManager.headingAvailable()

    private let manager = CLLocationManager()

    override init() {
        authorization = manager.authorizationStatus
        super.init()
        manager.delegate = self
        manager.headingFilter = 1          // degrees; anything finer just jitters
        manager.headingOrientation = .portrait
    }

    func start() {
        guard hasCompass else { return }
        if authorization == .notDetermined {
            manager.requestWhenInUseAuthorization()
        }
        manager.startUpdatingHeading()
    }

    func stop() {
        manager.stopUpdatingHeading()
    }

    var isDenied: Bool {
        authorization == .denied || authorization == .restricted
    }

    /// True north needs a location fix; magnetic north does not. Falling back to
    /// magnetic keeps the compass working, off by the local declination — about two
    /// degrees across Afghanistan, which is well inside the width of the pointer.
    fileprivate func apply(_ newHeading: CLHeading) {
        heading = newHeading.trueHeading >= 0 ? newHeading.trueHeading : newHeading.magneticHeading
        accuracy = newHeading.headingAccuracy
    }

    fileprivate func apply(authorization status: CLAuthorizationStatus) {
        authorization = status
        if status == .authorizedWhenInUse || status == .authorizedAlways {
            manager.startUpdatingHeading()
        }
    }
}

extension HeadingProvider: CLLocationManagerDelegate {
    nonisolated func locationManager(_ manager: CLLocationManager, didUpdateHeading newHeading: CLHeading) {
        Task { @MainActor in self.apply(newHeading) }
    }

    nonisolated func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        let status = manager.authorizationStatus
        Task { @MainActor in self.apply(authorization: status) }
    }
}

// MARK: - Screen

struct QiblaView: View {
    @Environment(\.colors) private var colors
    @EnvironmentObject private var settings: SettingsStore
    @StateObject private var provider = HeadingProvider()

    private var city: AfghanCity { settings.settings.city }

    private var bearing: Double {
        Qibla.bearing(latitude: city.latitude, longitude: city.longitude)
    }

    /// Where the pointer sits on screen: the qibla's bearing, minus wherever the phone
    /// is currently pointing.
    private var rotation: Double {
        bearing - (provider.heading ?? 0)
    }

    /// The pointer is at the top when `rotation` is zero. Five degrees either side is
    /// as close as a phone magnetometer can honestly claim to be.
    private var isAligned: Bool {
        guard provider.heading != nil else { return false }
        let normalized = (rotation.truncatingRemainder(dividingBy: 360) + 360)
            .truncatingRemainder(dividingBy: 360)
        return min(normalized, 360 - normalized) <= 5
    }

    var body: some View {
        ScrollView {
            VStack(spacing: Spacing.xl) {
                Text(city.displayName)
                    .appText(AppType.titleLarge)
                    .foregroundStyle(colors.onSurface)

                compass

                readout

                if !provider.hasCompass {
                    MessageState(
                        title: "qibla.noCompass.title".localized,
                        message: "qibla.noCompass.body".localized,
                        systemImage: "location.slash"
                    )
                } else if provider.isDenied {
                    MessageState(
                        title: "qibla.noPermission.title".localized,
                        message: "qibla.noPermission.body".localized,
                        systemImage: "location.slash",
                        actionLabel: "action.openSettings".localized,
                        action: {
                            guard let url = URL(string: UIApplication.openSettingsURLString) else { return }
                            UIApplication.shared.open(url)
                        }
                    )
                }
            }
            .padding(Spacing.lg)
            .frame(maxWidth: .infinity)
        }
        .background(colors.background)
        .onAppear { provider.start() }
        .onDisappear { provider.stop() }
    }

    private var compass: some View {
        ZStack {
            Circle()
                .fill(colors.surface)
                .overlay(Circle().stroke(colors.outlineVariant, lineWidth: 1))

            // Cardinal ticks, drawn against the dial so they turn with the phone.
            ForEach(0..<24, id: \.self) { index in
                Rectangle()
                    .fill(index % 6 == 0 ? colors.primary : colors.outlineVariant)
                    .frame(width: index % 6 == 0 ? 2.5 : 1, height: index % 6 == 0 ? 18 : 10)
                    .offset(y: -122)
                    .rotationEffect(.degrees(Double(index) * 15 - (provider.heading ?? 0)))
            }

            Text("compass.north".localized)
                .appText(AppType.labelLarge)
                .foregroundStyle(colors.onSurfaceVariant)
                .offset(y: -96)
                .rotationEffect(.degrees(-(provider.heading ?? 0)), anchor: .center)

            pointer
                .rotationEffect(.degrees(rotation))
                .animation(.easeOut(duration: 0.25), value: rotation)
        }
        .frame(width: 280, height: 280)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("qibla.a11y".localized(Int(bearing).persianDigits))
    }

    private var pointer: some View {
        VStack(spacing: Spacing.xs) {
            Image(systemName: "location.north.fill")
                .font(.system(size: 34))
                .foregroundStyle(isAligned ? colors.tertiary : colors.primary)

            Text("qibla.title".localized)
                .appText(AppType.labelMedium)
                .foregroundStyle(colors.onSurfaceVariant)

            Spacer()
        }
        .frame(height: 240)
    }

    private var readout: some View {
        VStack(spacing: Spacing.sm) {
            Text("qibla.readout".localized(Int(bearing.rounded()).persianDigits, Qibla.directionName(bearing)))
                .appText(AppType.titleMedium)
                .foregroundStyle(colors.onSurface)

            if provider.heading != nil {
                Text((isAligned ? "qibla.aligned" : "qibla.turn").localized)
                    .appText(AppType.bodySmall)
                    .foregroundStyle(isAligned ? colors.tertiary : colors.onSurfaceVariant)
            }

            // A magnetometer near metal or a speaker reads badly, and says so.
            if provider.accuracy > 15 {
                Text("qibla.lowAccuracy".localized)
                    .appText(AppType.bodySmall)
                    .foregroundStyle(colors.error)
            }
        }
        .multilineTextAlignment(.center)
        .cardSurface(colors)
    }
}
