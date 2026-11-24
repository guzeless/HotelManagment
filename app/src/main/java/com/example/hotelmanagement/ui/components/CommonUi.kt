package com.example.hotelmanagement.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hotelmanagement.util.DateUtils

/** Цвета статусов номеров и записей. */
fun statusColor(status: String): Color = when (status) {
    "Свободен", "Активно", "Проживает" -> Color(0xFF2E7D32)   // зелёный
    "Забронирован", "Выполняется" -> Color(0xFFF9A825)          // жёлтый
    "Занят" -> Color(0xFFC62828)                                // красный
    "Выполнен", "Завершено", "Выселён" -> Color(0xFF1565C0)     // синий
    else -> Color(0xFF616161)
}

/**
 * Каркас экрана: заголовок с кнопкой "Главное меню" (аналог btnExit/main_menu).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Главное меню")
                    }
                }
            )
        }
    ) { padding ->
        content(Modifier.padding(padding).padding(16.dp))
    }
}

/** Выпадающий список выбора значения. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selected: String?,
    enabled: Boolean = true,
    emptyText: String = "Нет данных",
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = selected ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            placeholder = { Text(if (options.isEmpty()) emptyText else "") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelect(option); expanded = false }
                )
            }
        }
    }
}

/** Поле выбора даты с диалогом календаря. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(label: String, millis: Long, onPick: (Long) -> Unit) {
    var show by remember { mutableStateOf(false) }
    if (show) {
        val state = rememberDatePickerState(initialSelectedDateMillis = millis)
        DatePickerDialog(
            onDismissRequest = { show = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let(onPick)
                    show = false
                }) { Text("ОК") }
            },
            dismissButton = { TextButton(onClick = { show = false }) { Text("Отмена") } }
        ) {
            DatePicker(state = state)
        }
    }
    OutlinedButton(onClick = { show = true }, modifier = Modifier.fillMaxWidth()) {
        Text("$label: ${DateUtils.format(millis)}")
    }
}

/** Строка "метка — значение" для информационных блоков. */
@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            text = "$label: ",
            fontSize = 14.sp,
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

/** Чип статуса с цветом. */
@Composable
fun StatusChip(status: String) {
    AssistChip(
        onClick = {},
        label = { Text(status, fontSize = 12.sp, color = Color.White) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = statusColor(status)
        )
    )
}

/** Заголовок секции. */
@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
