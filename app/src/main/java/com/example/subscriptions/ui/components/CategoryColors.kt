package com.example.subscriptions.ui.components

import androidx.compose.ui.graphics.Color
import com.example.subscriptions.data.local.Category
import com.example.subscriptions.ui.theme.CategoryEducation
import com.example.subscriptions.ui.theme.CategoryEntertainment
import com.example.subscriptions.ui.theme.CategoryMusic
import com.example.subscriptions.ui.theme.CategoryOther
import com.example.subscriptions.ui.theme.CategoryWork

/** Цвет категории — одинаковый на карточках и на диаграмме */
fun Category.color(): Color = when (this) {
    Category.WORK -> CategoryWork
    Category.ENTERTAINMENT -> CategoryEntertainment
    Category.EDUCATION -> CategoryEducation
    Category.MUSIC -> CategoryMusic
    Category.OTHER -> CategoryOther
}
