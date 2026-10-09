package af.namazia.app.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import af.namazia.app.R
import af.namazia.app.data.AppError
import af.namazia.app.data.PrayerData
import af.namazia.app.ui.components.MessageState
import af.namazia.app.ui.components.SkeletonBlock
import af.namazia.app.ui.theme.Spacing
import af.namazia.app.utils.toPersianDigits
import java.time.LocalDate

data class CalendarUiState(
    val isLoading: Boolean = false,
    val days: List<PrayerData> = emptyList(),
    val year: Int = LocalDate.now(af.namazia.app.utils.APP_ZONE).year,
    val month: Int = LocalDate.now(af.namazia.app.utils.APP_ZONE).monthValue,
    val error: AppError? = null
)

/**
 * Gregorian months as they are named in Afghanistan — not the Persian solar month
 * names, which belong to a different calendar entirely. Dari and Pashto spell several
 * of them differently, hence resources rather than one hard-coded list.
 */
private val gregorianMonths = listOf(
    R.string.month_1, R.string.month_2, R.string.month_3, R.string.month_4,
    R.string.month_5, R.string.month_6, R.string.month_7, R.string.month_8,
    R.string.month_9, R.string.month_10, R.string.month_11, R.string.month_12
)

// Day column is narrower than the five time columns; weights keep the grid aligned
// between the sticky header and every row.
private const val DAY_WEIGHT = 0.72f
private const val TIME_WEIGHT = 1f

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    calendarState: CalendarUiState,
    onMonthChanged: (year: Int, month: Int) -> Unit
) {
    val listState = rememberLazyListState()
    val today = LocalDate.now(af.namazia.app.utils.APP_ZONE)
    val todayDay = today.dayOfMonth
    val isCurrentMonth = calendarState.year == today.year &&
            calendarState.month == today.monthValue

    // Land on today rather than the 1st — that is the row the user came to read.
    LaunchedEffect(calendarState.days, isCurrentMonth) {
        if (isCurrentMonth && calendarState.days.isNotEmpty()) {
            val todayIndex = calendarState.days.indexOfFirst {
                it.date.gregorian.day.trim().toIntOrNull() == todayDay
            }
            if (todayIndex >= 0) listState.animateScrollToItem(todayIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.calendar_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "${gregorianMonths.getOrNull(calendarState.month - 1)?.let { stringResource(it) } ?: ""} " +
                                    calendarState.year.toPersianDigits(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    // RTL: "previous" sits at the start edge and points right.
                    IconButton(onClick = {
                        val (y, m) = if (calendarState.month == 1) {
                            calendarState.year - 1 to 12
                        } else {
                            calendarState.year to calendarState.month - 1
                        }
                        onMonthChanged(y, m)
                    }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = stringResource(R.string.calendar_prev_month))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val (y, m) = if (calendarState.month == 12) {
                            calendarState.year + 1 to 1
                        } else {
                            calendarState.year to calendarState.month + 1
                        }
                        onMonthChanged(y, m)
                    }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = stringResource(R.string.calendar_next_month))
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
            val error = calendarState.error

            when {
                calendarState.isLoading -> CalendarSkeleton()

                error != null -> MessageState(
                    title = stringResource(error.titleRes),
                    body = stringResource(error.bodyRes),
                    actionLabel = stringResource(R.string.action_retry),
                    onAction = { onMonthChanged(calendarState.year, calendarState.month) }
                )

                calendarState.days.isEmpty() -> MessageState(
                    title = stringResource(R.string.calendar_empty_title),
                    icon = Icons.Default.CalendarMonth,
                    actionLabel = stringResource(R.string.action_retry),
                    onAction = { onMonthChanged(calendarState.year, calendarState.month) }
                )

                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    stickyHeader { CalendarHeaderRow() }

                    itemsIndexed(calendarState.days) { index, dayData ->
                        val dayNum = dayData.date.gregorian.day.trim().toIntOrNull() ?: 0
                        DayRow(
                            dayData = dayData,
                            isToday = isCurrentMonth && dayNum == todayDay,
                            // Zebra striping: six columns of digits are hard to track
                            // across without an alternating ground.
                            striped = index % 2 == 1
                        )
                    }

                    item { Spacer(Modifier.height(Spacing.lg)) }
                }
            }
        }
    }
}

@Composable
private fun CalendarHeaderRow() {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.md)
        ) {
            HeaderCell(stringResource(R.string.calendar_day), DAY_WEIGHT)
            HeaderCell(stringResource(R.string.prayer_fajr), TIME_WEIGHT)
            HeaderCell(stringResource(R.string.prayer_dhuhr), TIME_WEIGHT)
            HeaderCell(stringResource(R.string.prayer_asr), TIME_WEIGHT)
            HeaderCell(stringResource(R.string.prayer_maghrib), TIME_WEIGHT)
            HeaderCell(stringResource(R.string.prayer_isha), TIME_WEIGHT)
        }
    }
}

@Composable
private fun RowScope.HeaderCell(label: String, weight: Float) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier.weight(weight)
    )
}

@Composable
private fun DayRow(dayData: PrayerData, isToday: Boolean, striped: Boolean) {
    val background = when {
        isToday -> MaterialTheme.colorScheme.primaryContainer
        striped -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.surface
    }
    val contentColor = if (isToday) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .padding(horizontal = Spacing.md, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Day cell: Gregorian day over the Hijri day, so both calendars are readable.
        Box(
            modifier = Modifier.weight(DAY_WEIGHT),
            contentAlignment = Alignment.Center
        ) {
            val dayNum = dayData.date.gregorian.day.trim().toPersianDigits()
            val hijriDay = dayData.date.hijri.day.trim().toPersianDigits()

            if (isToday) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayNum,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = dayNum,
                        style = MaterialTheme.typography.labelLarge,
                        color = contentColor
                    )
                    Text(
                        text = hijriDay,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        listOf(
            dayData.timings.Fajr,
            dayData.timings.Dhuhr,
            dayData.timings.Asr,
            dayData.timings.Maghrib,
            dayData.timings.Isha
        ).forEach { raw ->
            Text(
                text = cleanCalTime(raw).toPersianDigits(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = contentColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(TIME_WEIGHT)
            )
        }
    }
}

@Composable
private fun CalendarSkeleton() {
    Column(Modifier.fillMaxSize()) {
        CalendarHeaderRow()
        Column(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            repeat(10) {
                SkeletonBlock(modifier = Modifier.fillMaxWidth(), height = 44.dp)
            }
        }
    }
}

private fun cleanCalTime(time: String): String = time.split(" ").first()
