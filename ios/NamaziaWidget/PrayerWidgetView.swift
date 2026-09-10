import WidgetKit
import SwiftUI

struct PrayerWidgetView: View {
    @Environment(\.widgetFamily) private var family

    let entry: PrayerEntry

    var body: some View {
        content
            // Two environment values do the heavy lifting here. The layout direction
            // mirrors the widget like the app. The Persian locale is what makes the
            // live countdown below render as ۰۱:۲۳:۴۵ — `Text(timerInterval:)` is
            // formatted by the system, so it is the locale, not our own digit
            // conversion, that decides the numerals.
            .environment(\.layoutDirection, .rightToLeft)
            .environment(\.locale, Locale(identifier: "fa_AF"))
            .widgetBackground(background)
    }

    @ViewBuilder
    private var content: some View {
        if !entry.hasData {
            emptyState
        } else {
            switch family {
            case .accessoryRectangular: rectangular
            case .accessoryCircular: circular
            case .systemMedium: medium
            default: small
            }
        }
    }

    // MARK: - Home screen

    private var small: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(entry.cityName)
                .appText(AppType.labelSmall)
                .foregroundStyle(Color.white.opacity(0.75))

            Spacer(minLength: 0)

            if let next = entry.next {
                Text(next.prayer.dari)
                    .appText(AppType.titleLarge)
                    .foregroundStyle(.white)

                Text(next.clock.persianDigits)
                    .appText(AppType.headlineMedium)
                    .foregroundStyle(.white)

                countdown(to: next.date)
                    .appText(AppType.labelMedium)
                    .foregroundStyle(Color.white.opacity(0.85))
            } else {
                Text("اوقات امروز تمام شد")
                    .appText(AppType.titleMedium)
                    .foregroundStyle(.white)
            }

            Spacer(minLength: 0)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private var medium: some View {
        HStack(spacing: Spacing.md) {
            VStack(alignment: .leading, spacing: 2) {
                Text(entry.cityName)
                    .appText(AppType.labelSmall)
                    .foregroundStyle(Color.white.opacity(0.75))

                if let next = entry.next {
                    Text(next.prayer.dari)
                        .appText(AppType.titleLarge)
                        .foregroundStyle(.white)
                    Text(next.clock.persianDigits)
                        .appText(AppType.headlineSmall)
                        .foregroundStyle(.white)
                    countdown(to: next.date)
                        .appText(AppType.labelMedium)
                        .foregroundStyle(Color.white.opacity(0.85))
                }

                Spacer(minLength: 0)

                Text(entry.hijriDate)
                    .appText(AppType.labelSmall)
                    .foregroundStyle(Color.white.opacity(0.6))
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            VStack(spacing: 3) {
                ForEach(entry.rows.filter { $0.prayer.callsAzan }) { row in
                    HStack {
                        Text(row.prayer.dari)
                            .appText(AppType.labelMedium)
                        Spacer(minLength: Spacing.sm)
                        Text(row.clock.persianDigits)
                            .appText(AppType.labelMedium)
                            .monospacedDigit()
                    }
                    // The prayer in hand stays white; the ones already gone recede.
                    .foregroundStyle(Color.white.opacity(row.isPast ? 0.5 : 1))
                }
            }
            .frame(maxWidth: .infinity)
        }
    }

    // MARK: - Lock screen
    //
    // These render into a monochrome, translucent layer, so colour is ignored by the
    // system and only shape and weight carry meaning.

    private var rectangular: some View {
        VStack(alignment: .leading, spacing: 1) {
            if let next = entry.next {
                Text("\(next.prayer.dari) \(next.clock.persianDigits)")
                    .appText(AppType.titleMedium)
                countdown(to: next.date)
                    .appText(AppType.labelMedium)
            } else {
                Text("اوقات نماز")
                    .appText(AppType.titleMedium)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private var circular: some View {
        VStack(spacing: 0) {
            if let next = entry.next {
                Text(next.prayer.dari)
                    .appText(AppType.labelSmall)
                Text(next.clock.persianDigits)
                    .appText(AppType.labelLarge)
                    .monospacedDigit()
            } else {
                Image(systemName: "moon.stars")
            }
        }
    }

    // MARK: - Pieces

    /// A live countdown without a single extra timeline entry: WidgetKit redraws this
    /// text itself, once a second, for free.
    private func countdown(to date: Date) -> Text {
        Text(timerInterval: Date()...max(date, Date().addingTimeInterval(1)), countsDown: true)
    }

    private var emptyState: some View {
        VStack(spacing: Spacing.xs) {
            Image(systemName: "arrow.down.app")
                .foregroundStyle(Color.white.opacity(0.9))
            Text("برنامه را باز کنید")
                .appText(AppType.labelMedium)
                .foregroundStyle(.white)
                .multilineTextAlignment(.center)
        }
    }

    /// The lock-screen families are drawn monochrome by the system, so they get no
    /// background of their own; the home-screen ones get the brand gradient.
    @ViewBuilder
    private var background: some View {
        switch family {
        case .accessoryRectangular, .accessoryCircular:
            Color.clear
        default:
            LinearGradient(
                colors: [Palette.gradientStart, Palette.gradientEnd],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        }
    }
}

extension View {
    /// `containerBackground` is required from iOS 17 — a widget without it is drawn
    /// with a blank background — but does not exist before it, hence the split.
    @ViewBuilder
    func widgetBackground<Background: View>(_ background: Background) -> some View {
        if #available(iOS 17.0, *) {
            containerBackground(for: .widget) { background }
        } else {
            self.background(background)
        }
    }
}
