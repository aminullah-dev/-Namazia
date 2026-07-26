package com.kabulsignal.azanapp.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kabulsignal.azanapp.ui.theme.Radii
import com.kabulsignal.azanapp.ui.theme.Spacing

/** A single shimmering placeholder block. */
@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    height: Dp = 20.dp
) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )

    Box(
        modifier = modifier
            .height(height)
            .clip(Radii.sm)
            .alpha(alpha)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

/**
 * Skeleton stand-in for the prayer list. A shaped placeholder beats a spinner here:
 * the layout does not jump when real data lands, so the first frame feels instant.
 */
@Composable
fun PrayerListSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg, vertical = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        SkeletonBlock(modifier = Modifier.fillMaxWidth(), height = 210.dp)
        Spacer(Modifier.height(Spacing.xs))
        repeat(5) {
            SkeletonBlock(modifier = Modifier.fillMaxWidth(), height = 68.dp)
        }
    }
}

/** Full-screen message with an icon and an optional action — used for errors and empty data. */
@Composable
fun MessageState(
    title: String,
    body: String? = null,
    icon: ImageVector = Icons.Default.CloudOff,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(Spacing.xxl)
        ) {
            Surface(
                shape = Radii.pill,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(Modifier.height(Spacing.lg))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (body != null) {
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(Spacing.xl))
                Button(
                    onClick = onAction,
                    shape = Radii.pill,
                    modifier = Modifier.heightIn(min = Spacing.touchTarget)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Spacing.sm))
                    Text(actionLabel, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
