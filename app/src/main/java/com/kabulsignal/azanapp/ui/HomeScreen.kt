package com.kabulsignal.azanapp.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabulsignal.azanapp.data.PrayerTime
import com.kabulsignal.azanapp.utils.toPersianDigits
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
                            text = "اوقات نماز",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = uiState.currentCity.nameDari,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
            when {
                uiState.isLoading -> LoadingState()
                uiState.error != null -> ErrorState(uiState.error, onRefresh)
                else -> PrayerTimesList(uiState, onRefresh)
            }
        }
    }
}

@Composable
private fun PrayerTimesList(uiState: HomeUiState, onRefresh: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        uiState.nextPrayer?.let { next ->
            item {
                NextPrayerCard(prayer = next, hijriDate = uiState.hijriDate, onReached = onRefresh)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (uiState.nextPrayer == null && uiState.hijriDate.isNotEmpty()) {
            item {
                Text(
                    text = uiState.hijriDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        items(uiState.prayerTimes) { prayer ->
            PrayerTimeCard(prayer = prayer)
        }
    }
}

@Composable
private fun NextPrayerCard(prayer: PrayerTime, hijriDate: String, onReached: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (hijriDate.isNotEmpty()) {
                    Text(
                        text = hijriDate,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = "نماز بعدی",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = prayer.name,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = prayer.time.toPersianDigits(),
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                CountdownPill(targetTime = prayer.time, onReached = onReached)
            }
        }
    }
}

@Composable
private fun CountdownPill(targetTime: String, onReached: () -> Unit) {
    var remaining by remember(targetTime) { mutableStateOf(formatRemaining(secondsUntil(targetTime))) }

    LaunchedEffect(targetTime) {
        while (true) {
            val secs = secondsUntil(targetTime)
            if (secs <= 0L) {
                // Prayer time reached — ask for a refresh so the card advances to the next prayer.
                onReached()
                break
            }
            remaining = formatRemaining(secs)
            delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .background(
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = "تا اذان: ${remaining.toPersianDigits()}",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private val countdownFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** Seconds from now until [targetTime] today. Negative/zero once the time has passed. */
private fun secondsUntil(targetTime: String): Long {
    return try {
        val now = LocalTime.now(com.kabulsignal.azanapp.utils.APP_ZONE)
        val target = LocalTime.parse(targetTime, countdownFormatter)
        Duration.between(now, target).seconds
    } catch (e: Exception) {
        -1L
    }
}

private fun formatRemaining(seconds: Long): String {
    val safe = if (seconds < 0L) 0L else seconds
    val h = safe / 3600
    val m = (safe % 3600) / 60
    val s = safe % 60
    return "%02d:%02d:%02d".format(h, m, s)
}

@Composable
private fun PrayerTimeCard(prayer: PrayerTime) {
    val alpha = if (prayer.isPast) 0.45f else 1f
    val bgColor = when {
        prayer.isNext -> MaterialTheme.colorScheme.primaryContainer
        prayer.isPast -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (prayer.isNext) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = prayer.name,
                    fontWeight = if (prayer.isNext) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 18.sp,
                    color = if (prayer.isNext)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = prayer.nameEn,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (prayer.isPast) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "گذشته",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                if (prayer.isNext) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "بعدی",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = prayer.time.toPersianDigits(),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = if (prayer.isNext)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("در حال بارگزاری اوقات نماز...")
        }
    }
}

@Composable
private fun ErrorState(error: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text("تلاش دوباره")
            }
        }
    }
}
