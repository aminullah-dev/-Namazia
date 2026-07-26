package af.namazia.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import af.namazia.app.data.AfghanCity
import af.namazia.app.data.AppSettings
import af.namazia.app.data.CalcMethods
import af.namazia.app.data.Madhabs
import af.namazia.app.data.PrayerName
import af.namazia.app.ui.theme.Radii
import af.namazia.app.ui.theme.Spacing
import af.namazia.app.utils.toPersianDigits

private const val SUPPORT_EMAIL = "aminhashemi979@gmail.com"

private data class PrayerToggle(
    val label: String,
    val prayer: PrayerName,
    val enabled: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    cities: List<AfghanCity>,
    onCitySelected: (Int) -> Unit,
    onPrayerToggled: (PrayerName, Boolean) -> Unit,
    onReminderChanged: (Int) -> Unit,
    onCalcMethodChanged: (Int) -> Unit,
    onAsrSchoolChanged: (Int) -> Unit,
    onDarkModeToggled: (Boolean) -> Unit,
    onVibrationToggled: (Boolean) -> Unit,
    onTestAzan: (Boolean) -> Unit
) {
    val context = LocalContext.current

    val prayerToggles = listOf(
        PrayerToggle("فجر", PrayerName.FAJR, settings.fajrEnabled),
        PrayerToggle("طلوع آفتاب", PrayerName.SUNRISE, settings.sunriseEnabled),
        PrayerToggle("ظهر", PrayerName.DHUHR, settings.dhuhrEnabled),
        PrayerToggle("عصر", PrayerName.ASR, settings.asrEnabled),
        PrayerToggle("مغرب", PrayerName.MAGHRIB, settings.maghribEnabled),
        PrayerToggle("عشا", PrayerName.ISHA, settings.ishaEnabled)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = Spacing.lg,
                end = Spacing.lg,
                top = Spacing.sm,
                bottom = Spacing.xl
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            item {
                SettingsGroup("محل و محاسبه") {
                    CityDropdown(
                        cities = cities,
                        selectedIndex = settings.cityIndex,
                        onSelected = onCitySelected
                    )
                    Spacer(Modifier.height(Spacing.md))
                    CalcMethodDropdown(
                        selectedId = settings.calculationMethod,
                        onSelected = onCalcMethodChanged
                    )
                    Spacer(Modifier.height(Spacing.md))
                    MadhabDropdown(
                        selectedSchool = settings.asrSchool,
                        onSelected = onAsrSchoolChanged
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        text = "مذهب تنها بر وقت عصر تأثیر دارد. در حنفی عصر دیرتر می‌شود.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                SettingsGroup("اذان برای کدام وقت‌ها") {
                    prayerToggles.forEachIndexed { index, toggle ->
                        SwitchRow(
                            label = toggle.label,
                            checked = toggle.enabled,
                            onChange = { onPrayerToggled(toggle.prayer, it) }
                        )
                        if (index != prayerToggles.lastIndex) {
                            Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }

            item {
                SettingsGroup("یادآوری پیش از اذان") {
                    ReminderChips(
                        minutes = settings.reminderMinutes,
                        onChanged = onReminderChanged
                    )
                }
            }

            item {
                SettingsGroup("پخش اذان") {
                    ActionRow(
                        title = "تست اذان",
                        subtitle = "برای اطمینان از پخش صدا، همین حالا پخش می‌شود",
                        icon = Icons.Default.PlayCircle,
                        onClick = { onTestAzan(false) }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    ActionRow(
                        title = "تست اذان صبح",
                        subtitle = "فایل جداگانه اذان فجر",
                        icon = Icons.Default.PlayCircle,
                        onClick = { onTestAzan(true) }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    SwitchRow(
                        label = "لرزش هنگام اذان",
                        checked = settings.vibrationEnabled,
                        onChange = onVibrationToggled
                    )
                }
            }

            // Battery optimisation is the usual reason a scheduled azan never fires on
            // Samsung/Xiaomi, so surface it prominently — but only while it applies.
            item {
                BatteryOptimisationCard(context = context)
            }

            item {
                SettingsGroup("نمایش") {
                    SwitchRow(
                        label = "حالت شب",
                        checked = settings.darkMode,
                        onChange = onDarkModeToggled
                    )
                }
            }

            item {
                SettingsGroup("پشتیبانی") {
                    ActionRow(
                        title = "تماس با پشتیبانی",
                        subtitle = SUPPORT_EMAIL,
                        icon = Icons.Default.Email,
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$SUPPORT_EMAIL")
                                putExtra(Intent.EXTRA_SUBJECT, "پشتیبانی اپ اوقات نماز")
                            }
                            runCatching {
                                context.startActivity(
                                    Intent.createChooser(intent, "ارسال ایمیل")
                                )
                            }
                        }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    ActionRow(
                        title = "نسخه برنامه",
                        subtitle = "۱.۰.۰",
                        icon = Icons.Default.Info,
                        onClick = null
                    )
                }
            }
        }
    }
}

/** Titled card that groups related settings — replaces bare section labels over loose rows. */
@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(top = Spacing.sm)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(
                start = Spacing.xs,
                bottom = Spacing.sm
            )
        )
        Surface(
            shape = Radii.lg,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(Spacing.lg), content = content)
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Whole row toggles, not just the switch — a 48dp target instead of a thumb.
            .clickable { onChange(!checked) }
            .heightIn(min = Spacing.touchTarget),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun ActionRow(
    title: String,
    subtitle: String?,
    icon: ImageVector,
    highlight: Boolean = false,
    onClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .heightIn(min = Spacing.touchTarget)
            .padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (highlight) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
        Spacer(Modifier.width(Spacing.md))
        Icon(
            icon,
            contentDescription = null,
            tint = when {
                highlight -> MaterialTheme.colorScheme.error
                onClick != null -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outline
            }
        )
    }
}

@Composable
private fun BatteryOptimisationCard(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    // Re-checked on each recomposition so the card disappears once the user grants it.
    val exempt = powerManager.isIgnoringBatteryOptimizations(context.packageName)
    if (exempt) return

    Column(modifier = Modifier.padding(top = Spacing.sm)) {
        Surface(
            shape = Radii.lg,
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(Spacing.lg)) {
                ActionRow(
                    title = "بهینه‌سازی باتری را غیرفعال کنید",
                    subtitle = "بدون این کار، سیستم ممکن است اذان را به‌موقع پخش نکند",
                    icon = Icons.Default.BatteryAlert,
                    highlight = true,
                    onClick = {
                        val direct = Intent(
                            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                            Uri.parse("package:${context.packageName}")
                        )
                        val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        runCatching { context.startActivity(direct) }
                            .recoverCatching { context.startActivity(fallback) }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CityDropdown(
    cities: List<AfghanCity>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = cities.getOrNull(selectedIndex)?.nameDari ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("شهر") },
            textStyle = MaterialTheme.typography.bodyLarge,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = Radii.md,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            cities.forEachIndexed { index, city ->
                DropdownMenuItem(
                    text = {
                        Text(city.nameDari, style = MaterialTheme.typography.bodyLarge)
                    },
                    onClick = {
                        onSelected(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalcMethodDropdown(
    selectedId: Int,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = CalcMethods.nameOf(selectedId),
            onValueChange = {},
            readOnly = true,
            label = { Text("روش محاسبه") },
            textStyle = MaterialTheme.typography.bodyLarge,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = Radii.md,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            CalcMethods.list.forEach { method ->
                DropdownMenuItem(
                    text = {
                        Text(method.nameDari, style = MaterialTheme.typography.bodyLarge)
                    },
                    onClick = {
                        onSelected(method.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MadhabDropdown(
    selectedSchool: Int,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = Madhabs.nameOf(selectedSchool),
            onValueChange = {},
            readOnly = true,
            label = { Text("مذهب") },
            textStyle = MaterialTheme.typography.bodyLarge,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = Radii.md,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Madhabs.list.forEach { madhab ->
                DropdownMenuItem(
                    text = {
                        Text(madhab.nameDari, style = MaterialTheme.typography.bodyLarge)
                    },
                    onClick = {
                        onSelected(madhab.school)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ReminderChips(minutes: Int, onChanged: (Int) -> Unit) {
    Column {
        Text(
            text = if (minutes == 0) {
                "یادآوری خاموش است"
            } else {
                "${minutes.toPersianDigits()} دقیقه پیش از اذان"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Spacing.md))
        // FlowRow so the six chips wrap instead of being squeezed off-screen when
        // Dari labels run long.
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            listOf(0, 5, 10, 15, 20, 30).forEach { option ->
                FilterChip(
                    selected = minutes == option,
                    onClick = { onChanged(option) },
                    label = {
                        Text(
                            text = if (option == 0) {
                                "خاموش"
                            } else {
                                "${option.toPersianDigits()} دقیقه"
                            },
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                )
            }
        }
    }
}
