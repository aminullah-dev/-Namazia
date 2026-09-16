package af.namazia.app.ui

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExploreOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import af.namazia.app.R
import af.namazia.app.data.AfghanCity
import af.namazia.app.ui.components.MessageState
import af.namazia.app.ui.theme.Radii
import af.namazia.app.ui.theme.Spacing
import af.namazia.app.utils.QiblaUtil
import af.namazia.app.utils.toPersianDigits
import kotlin.math.abs
import kotlin.math.roundToInt

/** Within this many degrees we call it aligned and confirm with a haptic tick. */
private const val ALIGN_TOLERANCE_DEG = 4f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(city: AfghanCity) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    val qiblaBearing = remember(city) {
        QiblaUtil.bearingToKaaba(city.latitude, city.longitude)
    }

    // Sensors report azimuth from MAGNETIC north; the qibla bearing is from TRUE north.
    // GeomagneticField.declination bridges the two for this location.
    val declination = remember(city) {
        GeomagneticField(
            city.latitude.toFloat(),
            city.longitude.toFloat(),
            0f,
            System.currentTimeMillis()
        ).declination
    }

    var azimuth by remember { mutableFloatStateOf(0f) }
    var hasSensor by remember { mutableStateOf(true) }
    var accuracy by remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_ACCURACY_HIGH) }

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

            override fun onAccuracyChanged(sensor: Sensor?, newAccuracy: Int) {
                accuracy = newAccuracy
            }
        }

        rotationSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose { sensorManager.unregisterListener(listener) }
    }

    if (!hasSensor) {
        // No compass hardware: show the bearing as a number instead of a dial that cannot work.
        QiblaNoSensor(city = city, bearing = qiblaBearing)
        return
    }

    val trueAzimuth = (azimuth + declination + 360f) % 360f
    val qiblaRelative = (qiblaBearing - trueAzimuth + 360f) % 360f
    val offBy = abs(((qiblaRelative + 180f) % 360f) - 180f)
    val aligned = offBy < ALIGN_TOLERANCE_DEG

    // Confirm alignment once per entry, not continuously while held on target.
    LaunchedEffect(aligned) {
        if (aligned) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val animatedDial by animateFloatAsState(
        targetValue = -trueAzimuth,
        animationSpec = tween(220),
        label = "dial"
    )
    val needleColor by animateColorAsState(
        targetValue = if (aligned) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.tertiary
        },
        animationSpec = tween(300),
        label = "needle"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.qibla_title), style = MaterialTheme.typography.titleLarge) },
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
                .padding(horizontal = Spacing.xl),
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
                Spacer(Modifier.width(Spacing.xs))
                Text(
                    text = stringResource(city.nameRes),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.height(Spacing.md))

            AlignmentBadge(aligned = aligned, offBy = offBy)

            Spacer(Modifier.height(Spacing.xl))

            val ringColor = MaterialTheme.colorScheme.outlineVariant
            val tickColor = MaterialTheme.colorScheme.onSurfaceVariant
            val northColor = MaterialTheme.colorScheme.error
            val faceColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            val hubColor = MaterialTheme.colorScheme.surface

            Canvas(modifier = Modifier.size(288.dp)) {
                val r = size.minDimension / 2f
                val center = Offset(r, r)

                drawCircle(color = faceColor, radius = r)
                drawCircle(
                    color = ringColor,
                    radius = r,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Rotating bezel: ticks every 15°, longer every 45°, red at north.
                rotate(degrees = animatedDial, pivot = center) {
                    for (i in 0 until 24) {
                        rotate(degrees = i * 15f, pivot = center) {
                            val isNorth = i == 0
                            val isMajor = i % 3 == 0
                            val len = when {
                                isNorth -> 30f
                                isMajor -> 22f
                                else -> 12f
                            }
                            drawLine(
                                color = if (isNorth) northColor else tickColor.copy(
                                    alpha = if (isMajor) 0.75f else 0.4f
                                ),
                                start = Offset(center.x, center.y - r + 10f),
                                end = Offset(center.x, center.y - r + 10f + len),
                                strokeWidth = if (isNorth) 7f else if (isMajor) 4f else 2.5f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }

                // Needle stays fixed to the qibla direction relative to the device heading.
                rotate(degrees = qiblaRelative, pivot = center) {
                    val tip = center.y - r * 0.74f
                    val needle = Path().apply {
                        moveTo(center.x, tip)
                        lineTo(center.x - 20f, center.y + 6f)
                        lineTo(center.x + 20f, center.y + 6f)
                        close()
                    }
                    drawPath(needle, color = needleColor)
                    drawLine(
                        color = needleColor.copy(alpha = 0.3f),
                        start = center,
                        end = Offset(center.x, center.y + r * 0.5f),
                        strokeWidth = 9f,
                        cap = StrokeCap.Round
                    )
                }

                drawCircle(color = needleColor, radius = 15f, center = center)
                drawCircle(color = hubColor, radius = 6f, center = center)
            }

            Spacer(Modifier.height(Spacing.xl))

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                InfoTile(
                    label = stringResource(R.string.qibla_direction_label),
                    value = "${qiblaBearing.roundToInt().toPersianDigits()}°",
                    caption = stringResource(R.string.qibla_from_true_north)
                )
                InfoTile(
                    label = stringResource(R.string.qibla_offset_label),
                    value = "${offBy.roundToInt().toPersianDigits()}°",
                    caption = stringResource(if (aligned) R.string.qibla_on_target else R.string.qibla_turn_short)
                )
            }

            // Rotation-vector accuracy drops when the magnetometer needs re-calibrating;
            // tell the user how to fix it rather than showing a quietly wrong needle.
            if (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW ||
                accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE
            ) {
                Spacer(Modifier.height(Spacing.lg))
                Surface(
                    shape = Radii.md,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Rotate90DegreesCcw,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            text = stringResource(R.string.qibla_low_accuracy),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlignmentBadge(aligned: Boolean, offBy: Float) {
    val container by animateColorAsState(
        targetValue = if (aligned) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(300),
        label = "badge"
    )
    val content = if (aligned) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(shape = Radii.pill, color = container) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (aligned) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(Spacing.sm))
            }
            Text(
                text = stringResource(if (aligned) R.string.qibla_aligned else R.string.qibla_turn),
                style = MaterialTheme.typography.labelLarge,
                color = content
            )
        }
    }
}

@Composable
private fun RowScope.InfoTile(label: String, value: String, caption: String) {
    Surface(
        shape = Radii.md,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        modifier = Modifier.weight(1f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = Spacing.md, horizontal = Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QiblaNoSensor(city: AfghanCity, bearing: Float) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.qibla_title), style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            MessageState(
                title = stringResource(R.string.qibla_no_compass_title),
                body = stringResource(
                    R.string.qibla_no_compass_body,
                    stringResource(city.nameRes),
                    bearing.roundToInt().toPersianDigits()
                ),
                icon = Icons.Default.ExploreOff
            )
        }
    }
}
