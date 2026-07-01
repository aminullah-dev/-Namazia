package com.kabulsignal.azanapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabulsignal.azanapp.data.PrayerData
import com.kabulsignal.azanapp.utils.toPersianDigits
import java.time.LocalDate

data class CalendarUiState(
    val isLoading: Boolean = false,
    val days: List<PrayerData> = emptyList(),
    val year: Int = LocalDate.now(com.kabulsignal.azanapp.utils.APP_ZONE).year,
    val month: Int = LocalDate.now(com.kabulsignal.azanapp.utils.APP_ZONE).monthValue,
    val error: String? = null
)

private val persianMonths = listOf(
    "جنوری", "فبروری", "مارچ", "اپریل", "می", "جون",
    "جولای", "آگست", "سپتمبر", "اکتوبر", "نوامبر", "دسمبر"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    calendarState: CalendarUiState,
    onMonthChanged: (year: Int, month: Int) -> Unit
) {
    val listState = rememberLazyListState()
    val today = LocalDate.now(com.kabulsignal.azanapp.utils.APP_ZONE)
    val todayDay = today.dayOfMonth
    val isCurrentMonth = calendarState.year == today.year &&
            calendarState.month == today.monthValue

    // Scroll to today on first load
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("تقویم اوقات", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = "${persianMonths.getOrElse(calendarState.month - 1) { "" }} ${calendarState.year.toPersianDigits()}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        val prev = if (calendarState.month == 1)
                            Pair(calendarState.year - 1, 12)
                        else Pair(calendarState.year, calendarState.month - 1)
                        onMonthChanged(prev.first, prev.second)
                    }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "ماه قبل")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val next = if (calendarState.month == 12)
                            Pair(calendarState.year + 1, 1)
                        else Pair(calendarState.year, calendarState.month + 1)
                        onMonthChanged(next.first, next.second)
                    }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "ماه بعد")
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
            when {
                calendarState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                calendarState.error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(calendarState.error, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = {
                                onMonthChanged(calendarState.year, calendarState.month)
                            }) { Text("تلاش دوباره") }
                        }
                    }
                }
                calendarState.days.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("اطلاعاتی موجود نیست", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            CalendarHeader()
                        }
                        items(calendarState.days) { dayData ->
                            val dayNum = dayData.date.gregorian.day.trim().toIntOrNull() ?: 0
                            val isToday = isCurrentMonth && dayNum == todayDay
                            DayCard(dayData = dayData, isToday = isToday)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("روز", "فجر", "ظهر", "عصر", "مغرب", "عشا").forEach { label ->
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun DayCard(dayData: PrayerData, isToday: Boolean) {
    val dayNum = dayData.date.gregorian.day.trim()
    val hijriDay = dayData.date.hijri.day

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isToday -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isToday) 3.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Day number cell
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (isToday) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dayNum.toPersianDigits(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = dayNum.toPersianDigits(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = hijriDay.toPersianDigits(),
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            // Prayer time cells
            listOf(
                dayData.timings.Fajr,
                dayData.timings.Dhuhr,
                dayData.timings.Asr,
                dayData.timings.Maghrib,
                dayData.timings.Isha
            ).forEach { timeStr ->
                Text(
                    text = cleanCalTime(timeStr).toPersianDigits(),
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = if (isToday)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun cleanCalTime(time: String): String = time.split(" ").first()
