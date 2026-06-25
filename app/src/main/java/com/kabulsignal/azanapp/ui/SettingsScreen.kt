package com.kabulsignal.azanapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabulsignal.azanapp.data.AfghanCity
import com.kabulsignal.azanapp.data.AppSettings
import com.kabulsignal.azanapp.data.CalcMethods
import com.kabulsignal.azanapp.data.PrayerName
import com.kabulsignal.azanapp.utils.toPersianDigits

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
    onVibrationToggled: (Boolean) -> Unit
) {
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
