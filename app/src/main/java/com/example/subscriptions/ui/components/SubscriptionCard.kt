package com.example.subscriptions.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.subscriptions.ui.theme.CategoryEducation
import com.example.subscriptions.ui.theme.CategoryEntertainment
import com.example.subscriptions.ui.theme.CategoryWork
import com.example.subscriptions.ui.theme.OnStatusOkContainer
import com.example.subscriptions.ui.theme.OnStatusSoonContainer
import com.example.subscriptions.ui.theme.StatusOkContainer
import com.example.subscriptions.ui.theme.StatusSoonContainer
import com.example.subscriptions.ui.theme.SubscriptionsTheme
import com.example.subscriptions.util.PaymentUrgency
import com.example.subscriptions.util.daysUntilText
import com.example.subscriptions.util.urgencyFor

/**
 * КАСТОМНЫЙ КОМПОНЕНТ №1 — карточка подписки с динамическим отображением.
 *
 * Особенности:
 *  - цветная полоска слева показывает категорию;
 *  - табличка срока сама меняет цвет в зависимости от количества дней
 *    до списания (красный ≤ 3 дней, оранжевый ≤ 7, зелёный — больше),
 *    смена цвета анимирована;
 *  - при нажатии карточка плавно «проседает» (анимация масштаба).
 */
@Composable
fun SubscriptionCard(
    name: String,
    categoryTitle: String,
    categoryColor: Color,
    priceText: String,
    daysLeft: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        label = "cardScale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Полоска категории
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(categoryColor)
            )
            Spacer(Modifier.width(12.dp))

            // Кружок с первой буквой названия
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(Modifier.width(12.dp))

            // Название и категория
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = categoryTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))

            // Цена и срок
            Column(horizontalAlignment = Alignment.End) {
                Text(text = priceText, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                PaymentStatusChip(daysLeft = daysLeft)
            }
        }
    }
}

/** Табличка «через N дней», цвет которой зависит от срочности */
@Composable
fun PaymentStatusChip(daysLeft: Long, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val (backgroundTarget, contentTarget) = when (urgencyFor(daysLeft)) {
        PaymentUrgency.URGENT -> colors.errorContainer to colors.onErrorContainer
        PaymentUrgency.SOON -> StatusSoonContainer to OnStatusSoonContainer
        PaymentUrgency.NORMAL -> StatusOkContainer to OnStatusOkContainer
    }
    val background by animateColorAsState(backgroundTarget, label = "chipBackground")
    val content by animateColorAsState(contentTarget, label = "chipContent")

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = daysUntilText(daysLeft),
            style = MaterialTheme.typography.labelSmall,
            color = content
        )
    }
}

/** Предпросмотр всех трёх состояний карточки (удобно для скриншота в отчёт) */
@Preview(showBackground = true, widthDp = 380)
@Composable
private fun SubscriptionCardPreview() {
    SubscriptionsTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SubscriptionCard("Яндекс Плюс", "Развлечения", CategoryEntertainment, "399 ₽", 2, {})
            SubscriptionCard("Notion", "Работа", CategoryWork, "990 ₽", 5, {})
            SubscriptionCard("Duolingo", "Обучение", CategoryEducation, "399 ₽", 25, {})
        }
    }
}
