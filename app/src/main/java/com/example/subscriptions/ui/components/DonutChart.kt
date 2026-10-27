package com.example.subscriptions.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.subscriptions.ui.theme.CategoryEducation
import com.example.subscriptions.ui.theme.CategoryEntertainment
import com.example.subscriptions.ui.theme.CategoryMusic
import com.example.subscriptions.ui.theme.CategoryWork
import com.example.subscriptions.ui.theme.SubscriptionsTheme

/** Один сегмент диаграммы: значение и цвет */
data class DonutSlice(val value: Float, val color: Color)

/**
 * КАСТОМНЫЙ КОМПОНЕНТ №2 — анимированная кольцевая диаграмма.
 *
 * Рисуется вручную на Canvas (без сторонних библиотек):
 *  - каждый сегмент — дуга, длина которой пропорциональна доле значения;
 *  - между сегментами небольшие промежутки;
 *  - при появлении и при смене данных кольцо плавно «разворачивается»;
 *  - в центре выводится подпись (например, общая сумма).
 */
@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    centerTitle: String,
    centerSubtitle: String,
    modifier: Modifier = Modifier,
    diameter: Dp = 200.dp,
    strokeWidth: Dp = 28.dp,
    gapDegrees: Float = 2f,
    emptyColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest
) {
    // Прогресс анимации от 0 до 1; перезапускается, когда меняются данные
    val progress = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .size(diameter)
            .semantics { contentDescription = "Диаграмма расходов: $centerTitle $centerSubtitle" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val arcDiameter = size.minDimension - strokePx
            val topLeft = Offset(
                x = (size.width - arcDiameter) / 2f,
                y = (size.height - arcDiameter) / 2f
            )
            val arcSize = Size(arcDiameter, arcDiameter)
            val stroke = Stroke(width = strokePx)

            // Серое кольцо-фон (видно, пока данных нет)
            drawArc(
                color = emptyColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )

            val total = slices.sumOf { it.value.toDouble() }.toFloat()
            if (total <= 0f) return@Canvas

            val gap = if (slices.size > 1) gapDegrees else 0f
            var accumulated = 0f // сколько градусов уже занято предыдущими сегментами

            slices.forEach { slice ->
                val sweep = 360f * slice.value / total
                val visibleSweep = ((sweep - gap) * progress.value).coerceAtLeast(0f)
                drawArc(
                    color = slice.color,
                    // -90° — начинаем сверху, «с 12 часов»
                    startAngle = -90f + (accumulated + gap / 2f) * progress.value,
                    sweepAngle = visibleSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke
                )
                accumulated += sweep
            }
        }

        // Подпись в центре кольца
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(strokeWidth)
        ) {
            Text(text = centerTitle, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = centerSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DonutChartPreview() {
    SubscriptionsTheme {
        DonutChart(
            slices = listOf(
                DonutSlice(990f, CategoryWork),
                DonutSlice(848f, CategoryEntertainment),
                DonutSlice(399f, CategoryEducation),
                DonutSlice(199f, CategoryMusic)
            ),
            centerTitle = "2 436 ₽",
            centerSubtitle = "в месяц",
            modifier = Modifier.padding(16.dp)
        )
    }
}
