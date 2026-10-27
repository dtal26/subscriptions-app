package com.example.subscriptions.ui.edit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subscriptions.data.local.BillingPeriod
import com.example.subscriptions.data.local.Category
import com.example.subscriptions.ui.AppViewModelProvider
import com.example.subscriptions.ui.components.LabeledTextField
import com.example.subscriptions.util.formatDate
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Экран добавления и редактирования подписки */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditScreen(
    onBack: () -> Unit,
    viewModel: AddEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state = viewModel.state
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Шапка со стрелкой «назад»
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
            Text(
                text = if (state.isEdit) "Редактирование" else "Новая подписка",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        LabeledTextField(
            label = "Название",
            value = state.name,
            onValueChange = viewModel::onNameChange,
            placeholder = "Например, Яндекс Плюс",
            error = state.nameError
        )

        LabeledTextField(
            label = "Стоимость, ₽",
            value = state.price,
            onValueChange = viewModel::onPriceChange,
            placeholder = "0",
            error = state.priceError,
            keyboardType = KeyboardType.Decimal
        )

        // Периодичность
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FieldLabel("Периодичность")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BillingPeriod.entries.forEach { period ->
                    SelectableChip(
                        text = periodShortTitle(period),
                        selected = state.period == period,
                        onClick = { viewModel.onPeriodChange(period) }
                    )
                }
            }
        }

        // Дата следующего списания
        DateField(
            label = "Дата следующего списания",
            date = state.nextPaymentDate,
            onClick = { showDatePicker = true }
        )

        // Категория
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FieldLabel("Категория")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Category.entries.forEach { category ->
                    SelectableChip(
                        text = category.title,
                        selected = state.category == category,
                        onClick = { viewModel.onCategoryChange(category) }
                    )
                }
            }
        }

        // Напоминание
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Напомнить о списании",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = state.reminderEnabled,
                onCheckedChange = viewModel::onReminderChange
            )
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { viewModel.save(onBack) },
            enabled = !state.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Сохранить", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(Modifier.height(24.dp))
    }

    // Календарь выбора даты
    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.nextPaymentDate
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        viewModel.onDateChange(millisToLocalDate(millis))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** Чип выбора с галочкой у выбранного варианта */
@Composable
private fun SelectableChip(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = if (selected) {
            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
        } else {
            null
        }
    )
}

/** Поле с датой: по нажатию открывается календарь */
@Composable
private fun DateField(label: String, date: LocalDate, onClick: () -> Unit) {
    Column {
        FieldLabel(label)
        Spacer(Modifier.height(6.dp))
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDate(date),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = "Выбрать дату",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun periodShortTitle(period: BillingPeriod): String = when (period) {
    BillingPeriod.WEEK -> "Неделя"
    BillingPeriod.MONTH -> "Месяц"
    BillingPeriod.YEAR -> "Год"
}

private fun millisToLocalDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
