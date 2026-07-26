package af.namazia.app.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import af.namazia.app.data.Dua
import af.namazia.app.data.DuaCategory
import af.namazia.app.data.DuaData
import af.namazia.app.ui.theme.ArabicVerse
import af.namazia.app.ui.theme.Radii
import af.namazia.app.ui.theme.Spacing
import af.namazia.app.utils.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuaScreen(
    tasbihCount: Int,
    onTasbihIncrement: () -> Unit,
    onTasbihReset: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("اذکار و ادعیه", "تسبیح")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ذکر و دعا", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(title, style = MaterialTheme.typography.labelLarge)
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> DhikrList(categories = DuaData.categories)
                1 -> TasbihContent(
                    count = tasbihCount,
                    onIncrement = onTasbihIncrement,
                    onReset = onTasbihReset
                )
            }
        }
    }
}

@Composable
private fun DhikrList(categories: List<DuaCategory>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        items(categories, key = { it.title }) { category ->
            CategoryCard(category = category)
        }
    }
}

@Composable
private fun CategoryCard(category: DuaCategory) {
    var expanded by rememberSaveable(category.title) { mutableStateOf(false) }

    Surface(
        shape = Radii.lg,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .heightIn(min = Spacing.touchTarget)
                    .padding(Spacing.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${category.duas.size.toPersianDigits()} ذکر",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "بستن" else "باز کردن",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(220)) + fadeIn(tween(220)),
                exit = shrinkVertically(tween(180)) + fadeOut(tween(120))
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = Spacing.lg)
                        .padding(bottom = Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    category.duas.forEachIndexed { index, dua ->
                        DuaCard(dua = dua, ordinal = index + 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun DuaCard(dua: Dua, ordinal: Int) {
    Surface(
        shape = Radii.md,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = ordinal.toPersianDigits(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (dua.count > 1) {
                    Surface(
                        shape = Radii.pill,
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = "${dua.count.toPersianDigits()} مرتبه",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(
                                horizontal = Spacing.sm,
                                vertical = 2.dp
                            )
                        )
                    }
                }
            }

            // Arabic gets its own oversized style with a tall line height — the Material
            // body styles clip harakat and cramp the script.
            Text(
                text = dua.arabic,
                style = ArabicVerse,
                textAlign = TextAlign.Right,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            Text(
                text = dua.dari,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Right,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasbihContent(
    count: Int,
    onIncrement: () -> Unit,
    onReset: () -> Unit
) {
    val context = LocalContext.current
    val targets = listOf(33, 99, 100)
    var target by rememberSaveable { mutableIntStateOf(33) }

    val cycle = if (target > 0) count % target else count
    val rounds = if (target > 0) count / target else 0
    val progress = if (target > 0) cycle.toFloat() / target else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(220),
        label = "tasbihProgress"
    )

    LaunchedEffect(count) {
        if (count > 0) {
            val justCompleted = target > 0 && count % target == 0
            vibrateTasbihTick(context, strong = justCompleted)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.xl, vertical = Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.lg, Alignment.CenterVertically)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            targets.forEach { t ->
                FilterChip(
                    selected = target == t,
                    onClick = { target = t },
                    label = {
                        Text(t.toPersianDigits(), style = MaterialTheme.typography.labelLarge)
                    }
                )
            }
        }

        Text(
            text = if (rounds > 0) "دور کامل: ${rounds.toPersianDigits()}" else " ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Counter dial: the ring gives progress toward the target without reading numbers.
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(236.dp)) {
                val stroke = 12.dp.toPx()
                val inset = stroke / 2f
                val arcSize = Size(size.width - stroke, size.height - stroke)
                drawArc(
                    color = Color.White.copy(alpha = 0.20f),
                    startAngle = -90f, sweepAngle = 360f, useCenter = false,
                    topLeft = Offset(inset, inset), size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                drawArc(
                    color = Color.White,
                    startAngle = -90f, sweepAngle = 360f * animatedProgress, useCenter = false,
                    topLeft = Offset(inset, inset), size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }

            Box(
                modifier = Modifier
                    .size(196.dp)
                    .clip(Radii.pill)
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
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "از ${target.toPersianDigits()}",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        Text(
            text = "برای شمردن، دایره را لمس کنید",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "مجموع: ${count.toPersianDigits()}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                onClick = onReset,
                shape = Radii.pill,
                modifier = Modifier.heightIn(min = Spacing.touchTarget)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(Spacing.xs))
                Text("صفر کردن", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

private fun vibrateTasbihTick(context: Context, strong: Boolean) {
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
