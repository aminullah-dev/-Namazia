package com.kabulsignal.azanapp.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabulsignal.azanapp.utils.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihScreen(
    count: Int,
    onIncrement: () -> Unit,
    onReset: () -> Unit
) {
    val context = LocalContext.current
    val targets = listOf(33, 99, 100)
    var target by remember { mutableStateOf(33) }

    val cycle = if (target > 0) count % target else count
    val rounds = if (target > 0) count / target else 0

    // Light tap feedback; stronger pulse when a round completes
    LaunchedEffect(count) {
        if (count > 0) {
            val justCompleted = target > 0 && count % target == 0
            vibrateTick(context, strong = justCompleted)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تسبیح", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onReset) {
                        Icon(Icons.Default.Refresh, contentDescription = "صفر کردن")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
        ) {
            // Target selector
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                targets.forEach { t ->
                    FilterChip(
                        selected = target == t,
                        onClick = { target = t },
                        label = { Text(t.toPersianDigits()) }
                    )
                }
            }

            // Round counter
            Text(
                text = if (rounds > 0) "دور: ${rounds.toPersianDigits()}" else "",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            // Big tap circle
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onIncrement() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = cycle.toPersianDigits(),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "از ${target.toPersianDigits()}",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            Text(
                text = "برای شمردن، دایره را لمس کنید",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Text(
                text = "مجموع: ${count.toPersianDigits()}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

private fun vibrateTick(context: Context, strong: Boolean) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    val ms = if (strong) 120L else 20L
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(ms)
    }
}
