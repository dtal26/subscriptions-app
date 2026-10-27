package com.example.subscriptions.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subscriptions.ui.AppViewModelProvider
import com.example.subscriptions.ui.components.DonutChart
import com.example.subscriptions.ui.components.DonutSlice
import com.example.subscriptions.ui.components.color
import com.example.subscriptions.util.formatRub
import kotlin.math.roundToInt

/** Экран статистики расходов */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsState()
    // 0 — за месяц, 1 — за год
    var periodIndex by rememberSaveable { mutableStateOf(0) }
    val isYear = periodIndex == 1
    val multiplier = if (isYear) 12.0 else 1.0

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Статистика", style = MaterialTheme.typography.headlineSmall)

        // Переключатель «Месяц / Год»
        val periods = listOf("Месяц", "Год")
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            periods.forEachIndexed { index, title ->
                SegmentedButton(
                    selected = periodIndex == index,
                    onClick = { periodIndex = index },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = periods.size)
                ) {
                    Text(title)
                }
            }
        }

        if (state.categories.isEmpty()) {
            Text(
                text = "Добавьте подписки на главном экране, чтобы увидеть статистику",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            return@Column
        }

        // Кольцевая диаграмма по центру
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            DonutChart(
                slices = state.categories.map {
                    DonutSlice(
                        value = (it.monthlyAmount * multiplier).toFloat(),
                        color = it.category.color()
                    )
                },
                centerTitle = formatRub(state.totalMonthly * multiplier),
                centerSubtitle = if (isYear) "в год" else "в месяц"
            )
        }

        // Легенда
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.categories.forEach { stat ->
                LegendRow(
                    stat = stat,
                    amountText = formatRub(stat.monthlyAmount * multiplier)
                )
            }
        }

        // Две карточки с итогами
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryTile(
                title = if (isYear) "В месяц" else "В год",
                value = formatRub(if (isYear) state.totalMonthly else state.totalMonthly * 12),
                modifier = Modifier.weight(1f)
            )
            SummaryTile(
                title = "Самая дорогая",
                value = state.mostExpensiveName ?: "—",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LegendRow(stat: CategoryStat, amountText: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(stat.category.color(), CircleShape)
        )
        Box(Modifier.width(12.dp))
        Text(
            text = stat.category.title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${(stat.fraction * 100).roundToInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(Modifier.width(16.dp))
        Text(text = amountText, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun SummaryTile(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
