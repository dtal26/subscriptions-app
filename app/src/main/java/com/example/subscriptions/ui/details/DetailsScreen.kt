package com.example.subscriptions.ui.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subscriptions.data.local.BillingPeriod
import com.example.subscriptions.data.local.SubscriptionEntity
import com.example.subscriptions.ui.AppViewModelProvider
import com.example.subscriptions.ui.theme.OnStatusOkContainer
import com.example.subscriptions.ui.theme.OnStatusSoonContainer
import com.example.subscriptions.ui.theme.StatusOkContainer
import com.example.subscriptions.ui.theme.StatusSoonContainer
import com.example.subscriptions.util.PaymentUrgency
import com.example.subscriptions.util.daysUntilPayment
import com.example.subscriptions.util.daysUntilText
import com.example.subscriptions.util.formatDate
import com.example.subscriptions.util.formatMoney
import com.example.subscriptions.util.monthlyPrice
import com.example.subscriptions.util.nextPaymentDate
import com.example.subscriptions.util.urgencyFor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Экран подробной информации о подписке */
@Composable
fun DetailsScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: DetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val subscription = state.subscription
    if (subscription == null) {
        // Подписка удалена — возвращаемся назад
        LaunchedEffect(Unit) { onBack() }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Шапка: назад, заголовок, карандаш
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
            Text(
                text = "Подписка",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
        }

        MainInfo(subscription)
        NextPaymentBanner(subscription)
        InfoList(subscription)

        Spacer(Modifier.height(4.dp))

        Button(
            onClick = { onEdit(subscription.id) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Изменить", style = MaterialTheme.typography.labelLarge)
        }
        OutlinedButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Удалить подписку", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(24.dp))
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
            title = { Text("Удалить подписку?") },
            text = {
                Text(
                    text = "«${subscription.name}» исчезнет из списка и статистики. " +
                        "Это действие нельзя отменить.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        // после удаления экран сам вернётся назад (см. проверку subscription == null)
                        viewModel.delete {}
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") }
            }
        )
    }
}

/** Иконка, название, категория и цена по центру */
@Composable
private fun MainInfo(subscription: SubscriptionEntity) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = subscription.name.take(1).uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Text(subscription.name, style = MaterialTheme.typography.titleLarge)
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = subscription.category.title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(formatMoney(subscription.price), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.width(4.dp))
            Text(
                text = "/ ${periodSuffix(subscription.period)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }
    }
}

/** Плашка «Следующее списание», цвет зависит от срочности */
@Composable
private fun NextPaymentBanner(subscription: SubscriptionEntity) {
    val today = LocalDate.now()
    val days = subscription.daysUntilPayment(today)
    val date = subscription.nextPaymentDate(today)
    val dateText = date.format(DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("ru")))

    val (background: Color, content: Color) = when (urgencyFor(days)) {
        PaymentUrgency.URGENT ->
            MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        PaymentUrgency.SOON -> StatusSoonContainer to OnStatusSoonContainer
        PaymentUrgency.NORMAL -> StatusOkContainer to OnStatusOkContainer
    }

    Surface(
        color = background,
        contentColor = content,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.CalendarToday, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Следующее списание", style = MaterialTheme.typography.labelMedium)
                Text("$dateText · ${daysUntilText(days)}", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

/** Список параметров с разделителями */
@Composable
private fun InfoList(subscription: SubscriptionEntity) {
    val rows = listOf(
        "Периодичность" to subscription.period.title,
        "В год" to formatMoney(subscription.monthlyPrice() * 12),
        "Добавлена" to formatDate(LocalDate.ofEpochDay(subscription.createdEpochDay)),
        "Напоминание" to if (subscription.reminderEnabled) "Включено" else "Выключено"
    )
    Column {
        rows.forEach { (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(value, style = MaterialTheme.typography.bodyLarge)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

private fun periodSuffix(period: BillingPeriod): String = when (period) {
    BillingPeriod.WEEK -> "неделя"
    BillingPeriod.MONTH -> "месяц"
    BillingPeriod.YEAR -> "год"
}
