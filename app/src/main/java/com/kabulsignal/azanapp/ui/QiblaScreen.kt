package com.kabulsignal.azanapp.ui

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabulsignal.azanapp.data.AfghanCity
import com.kabulsignal.azanapp.utils.QiblaUtil
import com.kabulsignal.azanapp.utils.toPersianDigits
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(city: AfghanCity) {
    val context = LocalContext.current

    val qiblaBearing = remember(city) {
        QiblaUtil.bearingToKaaba(city.latitude, city.longitude)
    }

    // Sensors report azimuth from MAGNETIC north; the qibla bearing is from TRUE north.
    // GeomagneticField.declination bridges the two for the current location.
    val declination = remember(city) {
        GeomagneticField(
            city.latitude.toFloat(),
            city.longitude.toFloat(),
            0f,
            System.currentTimeMillis()
        ).declination
    }

    var azimuth by remember { mutableStateOf(0f) }
    var hasSensor by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        hasSensor = rotationSensor != null

        val listener = object : SensorEventListener {
            private val rotationMatrix = FloatArray(9)
            private val orientation = FloatArray(3)
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientation)
                val deg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                azimuth = (deg + 360f) % 360f
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        rotationSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose { sensorManager.unregisterListener(listener) }
    }

    // Heading corrected to true north.
    val trueAzimuth = (azimuth + declination + 360f) % 360f

    // Where the qibla sits relative to the phone's current heading
    val qiblaRelative = (qiblaBearing - trueAzimuth + 360f) % 360f
    val animatedDial by animateFloatAsState(
        targetValue = -trueAzimuth,
        animationSpec = tween(250),
        label = "dial"
    )

    val aligned = abs(((qiblaRelative + 180f) % 360f) - 180f) < 4f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("قبله‌نما", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(city.nameDari, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = if (aligned) "رو به قبله ✓" else "گوشی را بچرخانید",
                fontSize = 14.sp,
                color = if (aligned) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(32.dp))

            val ringColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            val tickColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            val northColor = MaterialTheme.colorScheme.error
            val qiblaColor = if (aligned) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.tertiary

            Canvas(modifier = Modifier.size(280.dp)) {
                val r = size.minDimension / 2f
                val center = Offset(r, r)

                // Outer ring
                drawCircle(color = ringColor, radius = r, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
                drawCircle(color = ringColor.copy(alpha = 0.15f), radius = r)

                // Rotating dial — tick marks every 30°, north marker
                rotate(degrees = animatedDial, pivot = center) {
                    for (i in 0 until 12) {
                        rotate(degrees = i * 30f, pivot = center) {
                            val long = i % 3 == 0
                            drawLine(
                                color = if (i == 0) northColor else tickColor,
                                start = Offset(r, r - r + 8f),
                                end = Offset(r, r - r + (if (long) 26f else 16f)),
                                strokeWidth = if (i == 0) 6f else 3f
                            )
                        }
                    }
                }

                // Fixed qibla needle pointing to qiblaRelative
                rotate(degrees = qiblaRelative, pivot = center) {
                    val needle = Path().apply {
                        moveTo(center.x, center.y - r * 0.72f) // tip
                        lineTo(center.x - 22f, center.y)
                        lineTo(center.x + 22f, center.y)
                        close()
                    }
                    drawPath(needle, color = qiblaColor)
                    // tail
                    drawLine(
                        color = qiblaColor.copy(alpha = 0.35f),
                        start = center,
                        end = Offset(center.x, center.y + r * 0.55f),
                        strokeWidth = 10f
                    )
                }

                drawCircle(color = qiblaColor, radius = 14f, center = center)
                drawCircle(color = Color.White, radius = 5f, center = center)
            }

            Spacer(Modifier.height(32.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("جهت قبله", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(
                        text = "${qiblaBearing.roundToInt().toString().toPersianDigits()}°",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("نسبت به شمال", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }

            if (!hasSensor) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "این دستگاه حسگر قطب‌نما ندارد — فقط زاویه قبله نمایش داده می‌شود",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
