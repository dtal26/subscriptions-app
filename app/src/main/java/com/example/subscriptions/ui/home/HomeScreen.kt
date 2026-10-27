package com.example.subscriptions.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subscriptions.ui.AppViewModelProvider
import com.example.subscriptions.ui.components.SubscriptionCard
import com.example.subscriptions.ui.components.color
import com.example.subscriptions.util.daysUntilText
import com.example.subscriptions.util.formatDate
import com.example.subscriptions.util.formatMoney
import com.example.subscriptions.util.formatRate
import com.example.subscriptions.util.formatRub
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Главный экран: сумма расходов и список подписок */
@Composable
fun HomeScreen(
    onAddClick: () -> Unit,
    onSubscriptionClick: (Long) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsState()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        // снизу большой отступ, чтобы кнопка «+» не закрывала последнюю карточку
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Greeting(name = state.userName) }
        item {
            Box(Modifier.padding(top = 4.dp)) {
                SummaryCard(
                    state = state,
                    onRefreshRates = viewModel::refreshRates
                )
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Мои подписки",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = activeCountText(state.items.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (state.items.isEmpty()) {
            item {
                EmptyState(onAddClick = onAddClick, onDemoClick = viewModel::addDemoData)
            }
        } else {
            items(state.items, key = { it.id }) { item ->
                SubscriptionCard(
                    name = item.name,
                    categoryTitle = item.category.title,
                    categoryColor = item.category.color(),
                    priceText = formatMoney(item.price),
                    daysLeft = item.daysLeft,
                    onClick = { onSubscriptionClick(item.id) }
                )
            }
        }
    }
}

@Composable
private fun Greeting(name: String) {
    val month = LocalDate.now()
        .format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale.forLanguageTag("ru")))
        .replaceFirstChar { it.titlecase(Locale.forLanguageTag("ru")) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Здравствуйте, $name!",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = month,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun SummaryCard(state: HomeUiState, onRefreshRates: () -> Unit) {
    val nearest = state.items.firstOrNull()
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Расходы в месяц", style = MaterialTheme.typography.bodyMedium)
            Text(formatRub(state.totalMonthly), style = MaterialTheme.typography.displaySmall)
            Text(
                text = if (nearest != null) {
                    "Ближайшее списание: ${nearest.name} · ${daysUntilText(nearest.daysLeft)}"
                } else {
                    "Добавьте первую подписку"
                },
                style = MaterialTheme.typography.bodySmall
            )
            RatesRow(state = state, onRefresh = onRefreshRates)
        }
    }
}

/** Справочная строка с курсом валют ЦБ (данные загружаются из сети) */
@Composable
private fun RatesRow(state: HomeUiState, onRefresh: () -> Unit) {
    val rates = state.rates
    val text = when {
        state.ratesStatus == RatesStatus.LOADING && rates == null -> "Загрузка курса валют…"
        rates == null -> "Нет связи: курс валют не загружен"
        else -> {
            val base = "Курс ЦБ на ${formatDate(rates.date)}: $ ${formatRate(rates.usd)} · € ${formatRate(rates.eur)}"
            if (state.ratesStatus == RatesStatus.FAILED) "$base (нет связи)" else base
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.weight(1f)
        )
        if (state.ratesStatus == RatesStatus.LOADING) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Обновить курс валют",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyState(onAddClick: () -> Unit, onDemoClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(48.dp)
            )
        }
        Text("Подписок пока нет", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Нажмите +, чтобы добавить первую подписку и следить за расходами",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Button(onClick = onAddClick) { Text("Добавить подписку") }
        TextButton(onClick = onDemoClick) { Text("Заполнить примерами") }
    }
}

/** 1 активная, 2 активные, 5 активных */
private fun activeCountText(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    val word = when {
        mod10 == 1 && mod100 != 11 -> "активная"
        mod10 in 2..4 && mod100 !in 12..14 -> "активные"
        else -> "активных"
    }
    return "$count $word"
}
