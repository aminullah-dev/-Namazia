import SwiftUI

/// The screen the app exists for: what time the next prayer is, and how long is left.
struct HomeView: View {
    @Environment(\.colors) private var colors
    @Environment(\.scenePhase) private var scenePhase
    @EnvironmentObject private var settings: SettingsStore
    @StateObject private var model = HomeViewModel()

    /// One-second tick for the countdown. Owned by the view so it stops with the view,
    /// with nothing to cancel by hand.
    private let tick = Timer.publish(every: 1, on: .main, in: .common).autoconnect()

    var body: some View {
        VStack(spacing: 0) {
            header
            content
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .background(colors.background)
        .task { await model.load() }
        .onReceive(tick) { model.tick($0) }
        // The timer does not run in the background, so a phone reopened the next
        // morning would otherwise still be showing yesterday.
        .onChange(of: scenePhase) { phase in
            guard phase == .active else { return }
            model.tick(Date())
            Task { await model.load() }
        }
        // The city, method and school decide what was fetched, so a change to any of
        // them invalidates what is on screen. Toggling an azan on or off does not —
        // the rows re-derive from the settings on the next render.
        .onChange(of: settings.settings.calculationIdentity) { _ in
            Task { await model.settingsChanged() }
        }
    }

    // MARK: - Header

    private var header: some View {
        HStack(alignment: .center) {
            VStack(alignment: .leading, spacing: 2) {
                Text("اوقات نماز")
                    .appText(AppType.titleLarge)
                    .foregroundStyle(colors.onSurface)

                Text(model.city.nameDari)
                    .appText(AppType.bodySmall)
                    .foregroundStyle(colors.onSurfaceVariant)
            }

            Spacer()

            Button {
                Task { await model.refresh() }
            } label: {
                Image(systemName: "arrow.clockwise")
                    .font(.system(size: 17, weight: .medium))
                    .frame(width: Spacing.touchTarget, height: Spacing.touchTarget)
            }
            .tint(colors.primary)
            .accessibilityLabel("بارگیری دوباره")
        }
        .padding(.horizontal, Spacing.lg)
        .padding(.vertical, Spacing.sm)
        .background(colors.surface)
        .overlay(alignment: .bottom) {
            Rectangle()
                .fill(colors.outlineVariant)
                .frame(height: 0.5)
        }
    }

    // MARK: - Content

    @ViewBuilder
    private var content: some View {
        if model.isEmpty, model.isLoading {
            // Skeleton, not a spinner: the layout does not jump when data lands.
            PrayerListSkeleton()
        } else if model.isEmpty, let error = model.error {
            ScrollView {
                MessageState(
                    title: error.title,
                    message: error.message,
                    actionLabel: "تلاش دوباره",
                    action: { Task { await model.refresh() } }
                )
                .padding(.top, Spacing.xxl)
            }
            .refreshable { await model.refresh() }
        } else {
            list
        }
    }

    private var list: some View {
        List {
            hero
                .listRowInsets(EdgeInsets(top: Spacing.md, leading: Spacing.lg,
                                          bottom: Spacing.md, trailing: Spacing.lg))
                .listRowBackground(Color.clear)
                .listRowSeparator(.hidden)

            ForEach(model.rows) { row in
                PrayerRowView(row: row)
                    .listRowInsets(EdgeInsets(top: Spacing.xs, leading: Spacing.lg,
                                              bottom: Spacing.xs, trailing: Spacing.lg))
                    .listRowBackground(Color.clear)
                    .listRowSeparator(.hidden)
            }

            // Times are still shown when a refresh fails — but they are labelled.
            if let error = model.error, !model.isEmpty {
                StaleDataNotice(title: error.title)
                    .listRowInsets(EdgeInsets(top: Spacing.md, leading: Spacing.lg,
                                              bottom: Spacing.xl, trailing: Spacing.lg))
                    .listRowBackground(Color.clear)
                    .listRowSeparator(.hidden)
            }
        }
        .listStyle(.plain)
        .scrollContentBackground(.hidden)
        .refreshable { await model.refresh() }
    }

    // MARK: - Hero card

    private var hero: some View {
        VStack(spacing: Spacing.lg) {
            if !model.hijriDate.isEmpty {
                Text(model.hijriDate)
                    .appText(AppType.labelMedium)
                    .foregroundStyle(Color.white.opacity(0.92))
                    .padding(.horizontal, Spacing.md)
                    .padding(.vertical, Spacing.xs)
                    .background(Color.white.opacity(0.16), in: Capsule())
            }

            if let next = model.nextPrayer {
                CountdownRing(next: next, previous: model.previousPrayer, now: model.now)
            } else {
                VStack(spacing: Spacing.sm) {
                    Text("نمازهای امروز تمام شد")
                        .appText(AppType.headlineSmall)
                        .foregroundStyle(.white)
                    Text("اذان فجر فردا به‌موقع پخش می‌شود")
                        .appText(AppType.bodyMedium)
                        .foregroundStyle(Color.white.opacity(0.85))
                }
                .multilineTextAlignment(.center)
                .padding(.vertical, Spacing.xl)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, Spacing.xl)
        .padding(.horizontal, Spacing.lg)
        .background(
            // The brand gradient rather than the scheme's primary colour: in dark mode
            // `primary` is a pale blue, and the white text on this card would sit on it
            // almost unreadably. These two stay dark in both schemes.
            LinearGradient(
                colors: [Palette.gradientStart, Palette.gradientEnd],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            ),
            in: RoundedRectangle(cornerRadius: Radii.xl, style: .continuous)
        )
    }
}
