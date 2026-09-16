import SwiftUI

/// The hero of the home screen: a ring that closes as the gap between the last prayer
/// and the next one elapses, so how much time is left reads at a glance before any
/// digits are parsed.
struct CountdownRing: View {
    let next: PrayerRow
    /// The prayer the ring is counting *from*, which sets the size of the interval.
    let previous: PrayerRow?
    let now: Date

    private let diameter: CGFloat = 232
    private let stroke: CGFloat = 10

    /// How much of the current interval has elapsed, 0...1.
    ///
    /// The span is the real gap between the previous prayer and this one, so the ring
    /// tracks something true rather than an arbitrary window. Before Fajr there is no
    /// earlier prayer today, so a six-hour fallback stands in.
    private var progress: Double {
        let span = previous.map { next.date.timeIntervalSince($0.date) } ?? 0
        let interval = span > 0 ? span : 6 * 3600
        let remaining = max(0, next.date.timeIntervalSince(now))
        return min(1, max(0, (interval - remaining) / interval))
    }

    private var secondsLeft: Int {
        max(0, Int(next.date.timeIntervalSince(now)))
    }

    var body: some View {
        ZStack {
            Circle()
                .stroke(Color.white.opacity(0.18), style: StrokeStyle(lineWidth: stroke, lineCap: .round))

            Circle()
                .trim(from: 0, to: progress)
                .stroke(Color.white, style: StrokeStyle(lineWidth: stroke, lineCap: .round))
                // Trimmed shapes start at 3 o'clock; -90° moves the start to the top.
                .rotationEffect(.degrees(-90))
                .animation(.easeInOut(duration: 0.6), value: progress)

            VStack(spacing: Spacing.xs) {
                Text("home.nextPrayer".localized)
                    .appText(AppType.labelMedium)
                    .foregroundStyle(Color.white.opacity(0.85))

                Text(next.prayer.localizedName)
                    .appText(AppType.headlineMedium)
                    .foregroundStyle(.white)

                Text(next.clock.persianDigits)
                    .appText(AppType.displayMedium)
                    .foregroundStyle(.white)

                Text("home.untilAzan".localized(countdownText(seconds: secondsLeft)))
                    .appText(AppType.labelLarge)
                    .foregroundStyle(.white)
                    .monospacedDigit()
                    .padding(.horizontal, Spacing.md)
                    .padding(.vertical, Spacing.xs)
                    .background(Color.white.opacity(0.18), in: Capsule())
                    .padding(.top, Spacing.xs)
            }
            .padding(Spacing.lg)
        }
        .frame(width: diameter, height: diameter)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(
            "a11y.ringSummary".localized(
                next.prayer.localizedName,
                next.clock.persianDigits,
                countdownText(seconds: secondsLeft)
            )
        )
    }
}
