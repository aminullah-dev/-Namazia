package af.namazia.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import af.namazia.app.R
import af.namazia.app.data.PrayerTime
import af.namazia.app.ui.components.MessageState
import af.namazia.app.ui.components.PrayerListSkeleton
import af.namazia.app.ui.theme.Radii
import af.namazia.app.ui.theme.Spacing
import af.namazia.app.utils.APP_ZONE
import af.namazia.app.utils.toPersianDigits
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onRefresh: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = uiState.currentCity.nameDari,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "بارگزاری مجدد")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val error = uiState.error

            when {
                // Skeleton, not a spinner: the layout does not jump when data lands.
                uiState.isLoading && uiState.prayerTimes.isEmpty() -> PrayerListSkeleton()

                error != null && uiState.prayerTimes.isEmpty() -> MessageState(
                    title = stringResource(error.titleRes),
                    body = stringResource(error.bodyRes),
                    actionLabel = stringResource(R.string.action_retry),
                    onAction = onRefresh
                )

                else -> PrayerTimesList(uiState, onRefresh)
            }
        }
    }
}

@Composable
private fun PrayerTimesList(uiState: HomeUiState, onRefresh: () -> Unit) {
    // Bound to a local so the value is usable inside the item lambdas below.
    val staleError = uiState.error

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.lg,
            end = Spacing.lg,
            top = Spacing.md,
            bottom = Spacing.xl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        item {
            NextPrayerHero(
                next = uiState.nextPrayer,
                previous = uiState.prayerTimes.lastOrNull { it.isPast },
                hijriDate = uiState.hijriDate,
                onReached = onRefresh
            )
            Spacer(Modifier.height(Spacing.md))
        }

        items(uiState.prayerTimes, key = { it.nameEn }) { prayer ->
            PrayerRow(prayer = prayer)
        }

        // Cached data still shows, but say so rather than passing it off as live.
        if (staleError != null) {
            item {
                Spacer(Modifier.height(Spacing.sm))
                StaleDataNotice(title = stringResource(staleError.titleRes))
            }
        }
    }
}

/**
 * Hero card: the one thing the user opens the app for. A ring closes as the current
 * interval elapses, so remaining time reads at a glance before any digits are parsed.
 */
@Composable
private fun NextPrayerHero(
    next: PrayerTime?,
    previous: PrayerTime?,
    hijriDate: String,
    onReached: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Radii.xl)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    ),
                    start = Offset.Zero,
                    end = Offset(900f, 900f)
                )
            )
            .padding(vertical = Spacing.xl, horizontal = Spacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (hijriDate.isNotEmpty()) {
                Surface(
                    shape = Radii.pill,
                    color = Color.White.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = hijriDate,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.92f),
                        modifier = Modifier.padding(
                            horizontal = Spacing.md,
                            vertical = Spacing.xs
                        )
                    )
                }
                Spacer(Modifier.height(Spacing.lg))
            }

            if (next == null) {
                // Every prayer of the day is behind us; Fajr arrives with tomorrow's data.
                Text(
                    text = "نمازهای امروز تمام شد",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = "اذان فجر فردا به‌موقع پخش می‌شود",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            } else {
                CountdownRing(
                    next = next,
                    previous = previous,
                    onReached = onReached
                )
            }
        }
    }
}

@Composable
private fun CountdownRing(
    next: PrayerTime,
    previous: PrayerTime?,
    onReached: () -> Unit
) {
    var secondsLeft by remember(next.time) { mutableLongStateOf(secondsUntil(next.time)) }

    LaunchedEffect(next.time) {
        while (true) {
            val secs = secondsUntil(next.time)
            if (secs <= 0L) {
                // Time reached — ask for a reload so the hero advances to the next prayer.
                onReached()
                break
            }
            secondsLeft = secs
            delay(1000)
        }
    }

    // Fraction of the current interval already elapsed, so the ring tracks the real
    // gap between prayers instead of an arbitrary span.
    val intervalSeconds = remember(next.time, previous?.time) {
        val span = previous?.time?.let { secondsBetween(it, next.time) } ?: 0L
        // No earlier prayer today (pre-Fajr): fall back to a 6h window.
        if (span > 0L) span else 6 * 3600L
    }
    val progress = ((intervalSeconds - secondsLeft).toFloat() / intervalSeconds)
        .coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = "ringProgress"
    )

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(232.dp)) {
            val stroke = 10.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)

            drawArc(
                color = Color.White.copy(alpha = 0.18f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = Color.White,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "نماز بعدی",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
            Text(
                text = next.name,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = next.time.toPersianDigits(),
                style = MaterialTheme.typography.displayMedium,
                color = Color.White
            )
            Spacer(Modifier.height(Spacing.sm))
            Surface(
                shape = Radii.pill,
                color = Color.White.copy(alpha = 0.18f)
            ) {
                Text(
                    text = "تا اذان ${formatRemaining(secondsLeft).toPersianDigits()}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier.padding(
                        horizontal = Spacing.md,
                        vertical = Spacing.xs
                    )
                )
            }
        }
    }
}

@Composable
private fun PrayerRow(prayer: PrayerTime) {
    val containerColor by animateColorAsState(
        targetValue = when {
            prayer.isNext -> MaterialTheme.colorScheme.primaryContainer
            prayer.isPast -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(400),
        label = "rowColor"
    )
    val contentColor = when {
        prayer.isNext -> MaterialTheme.colorScheme.onPrimaryContainer
        prayer.isPast -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        shape = Radii.md,
        color = containerColor,
        tonalElevation = if (prayer.isNext) 3.dp else 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading marker so state is not carried by colour alone.
            StatusDot(prayer = prayer)

            Spacer(Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = prayer.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (prayer.isNext) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor
                )
                Text(
                    text = prayer.nameEn,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.6f)
                )
            }

            if (!prayer.enabled) {
                Icon(
                    Icons.Default.NotificationsOff,
                    contentDescription = "اذان این وقت خاموش است",
                    tint = contentColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(Spacing.sm))
            }

            Text(
                text = prayer.time.toPersianDigits(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = if (prayer.isNext) FontWeight.Bold else FontWeight.Medium,
                color = if (prayer.isNext) MaterialTheme.colorScheme.primary else contentColor
            )
        }
    }
}

@Composable
private fun StatusDot(prayer: PrayerTime) {
    val dotSize = 32.dp
    val iconSize = 16.dp

    when {
        prayer.isNext -> Box(
            modifier = Modifier
                .size(dotSize)
                .clip(Radii.pill)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.NotificationsActive,
                contentDescription = "نماز بعدی",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(iconSize)
            )
        }

        prayer.isPast -> Box(
            modifier = Modifier
                .size(dotSize)
                .clip(Radii.pill)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = "گذشته",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(iconSize)
            )
        }

        else -> Box(
            modifier = Modifier
                .size(dotSize)
                .clip(Radii.pill)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Brightness5,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun StaleDataNotice(title: String) {
    Surface(
        shape = Radii.md,
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$title — اوقات نمایش‌داده‌شده از حافظه است",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(Spacing.md)
        )
    }
}

private val countdownFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** Seconds from now until [targetTime] today; zero or negative once it has passed. */
private fun secondsUntil(targetTime: String): Long = try {
    Duration.between(
        LocalTime.now(APP_ZONE),
        LocalTime.parse(targetTime, countdownFormatter)
    ).seconds
} catch (e: Exception) {
    -1L
}

/** Seconds between two HH:mm times on the same day. */
private fun secondsBetween(from: String, to: String): Long = try {
    Duration.between(
        LocalTime.parse(from, countdownFormatter),
        LocalTime.parse(to, countdownFormatter)
    ).seconds
} catch (e: Exception) {
    0L
}

private fun formatRemaining(seconds: Long): String {
    val safe = if (seconds < 0L) 0L else seconds
    return "%02d:%02d:%02d".format(safe / 3600, (safe % 3600) / 60, safe % 60)
}
