package com.kabulsignal.azanapp.ui

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabulsignal.azanapp.data.AfghanCity
import com.kabulsignal.azanapp.data.AppSettings
import com.kabulsignal.azanapp.data.CalcMethods
import com.kabulsignal.azanapp.data.PrayerName
import com.kabulsignal.azanapp.utils.toPersianDigits

private const val SUPPORT_EMAIL = "aminhashemi979@gmail.com"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    cities: List<AfghanCity>,
    onCitySelected: (Int) -> Unit,
    onPrayerToggled: (PrayerName, Boolean) -> Unit,
    onReminderChanged: (Int) -> Unit,
    onCalcMethodChanged: (Int) -> Unit,
    onDarkModeToggled: (Boolean) -> Unit,
    onVibrationToggled: (Boolean) -> Unit,
    onTestAzan: (Boolean) -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات", fontWeight = FontWeight.Bold) },
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SectionTitle("شهر")
                CityDropdown(
                    cities = cities,
                    selectedIndex = settings.cityIndex,
                    onSelected = onCitySelected
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionTitle("روش محاسبه")
                CalcMethodDropdown(
                    selectedId = settings.calculationMethod,
                    onSelected = onCalcMethodChanged
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionTitle("اذان فعال")
            }

            item { PrayerToggleRow("فجر", PrayerName.FAJR, settings.fajrEnabled, onPrayerToggled) }
            item { PrayerToggleRow("طلوع آفتاب", PrayerName.SUNRISE, settings.sunriseEnabled, onPrayerToggled) }
            item { PrayerToggleRow("ظهر", PrayerName.DHUHR, settings.dhuhrEnabled, onPrayerToggled) }
            item { PrayerToggleRow("عصر", PrayerName.ASR, settings.asrEnabled, onPrayerToggled) }
            item { PrayerToggleRow("مغرب", PrayerName.MAGHRIB, settings.maghribEnabled, onPrayerToggled) }
            item { PrayerToggleRow("عشا", PrayerName.ISHA, settings.ishaEnabled, onPrayerToggled) }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionTitle("یادآوری")
                ReminderChips(
                    minutes = settings.reminderMinutes,
                    onChanged = onReminderChanged
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionTitle("عمومی")
                SwitchRow("لرزش هنگام اذان", settings.vibrationEnabled, onVibrationToggled)
                Spacer(modifier = Modifier.height(8.dp))
                SwitchRow("حالت شب", settings.darkMode, onDarkModeToggled)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionTitle("اذان")

                // Test azan immediately
                ActionRow(
                    title = "تست اذان (پخش فوری)",
                    subtitle = "برای اطمینان از پخش صدا",
                    icon = Icons.Default.PlayCircle,
                    onClick = { onTestAzan(false) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                ActionRow(
                    title = "تست اذان صبح",
                    subtitle = "پخش فایل اذان فجر",
                    icon = Icons.Default.PlayCircle,
                    onClick = { onTestAzan(true) }
                )

                // Battery optimization — main reason azan does not fire on Samsung/Xiaomi
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                    val ignoringBattery = pm.isIgnoringBatteryOptimizations(context.packageName)
                    if (!ignoringBattery) {
                        Spacer(modifier = Modifier.height(8.dp))
                        ActionRow(
                            title = "غیرفعال کردن بهینه‌سازی باتری",
                            subtitle = "اگر اذان به‌موقع پخش نمی‌شود این را فعال کنید",
                            icon = Icons.Default.BatteryAlert,
                            highlight = true,
                            onClick = {
                                val intent = Intent(
                                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                    Uri.parse("package:${context.packageName}")
                                )
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                                }
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle("پشتیبانی")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$SUPPORT_EMAIL")
                                putExtra(Intent.EXTRA_SUBJECT, "پشتیبانی اپ اوقات نماز")
                            }
                            context.startActivity(Intent.createChooser(intent, "ارسال ایمیل"))
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("تماس با پشتیبانی", fontSize = 16.sp)
                            Text(
                                text = SUPPORT_EMAIL,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("نسخه برنامه", fontSize = 16.sp)
                            Text(
                                text = "نسخه ۱.۰.۰",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (highlight)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp)
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(
                icon,
                contentDescription = null,
                tint = if (highlight)
                    MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
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
            label = { Text("انتخاب شهر") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
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
                    text = { Text(city.nameDari) },
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
            label = { Text("روش محاسبه اوقات") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
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
                    text = { Text(method.nameDari) },
                    onClick = {
                        onSelected(method.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PrayerToggleRow(
    label: String,
    prayer: PrayerName,
    enabled: Boolean,
    onToggle: (PrayerName, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 16.sp)
            Switch(
                checked = enabled,
                onCheckedChange = { onToggle(prayer, it) }
            )
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 16.sp)
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderChips(minutes: Int, onChanged: (Int) -> Unit) {
    Column {
        Text(
            text = if (minutes == 0) "یادآوری غیرفعال"
            else "${minutes.toPersianDigits()} دقیقه قبل از اذان",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(0, 5, 10, 15, 20, 30).forEach { option ->
                FilterChip(
                    selected = minutes == option,
                    onClick = { onChanged(option) },
                    label = {
                        Text(
                            if (option == 0) "خاموش" else "${option.toPersianDigits()} دقیقه",
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }
    }
}
