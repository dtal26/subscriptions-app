package com.example.subscriptions.ui.theme

import androidx.compose.ui.graphics.Color

// ===== Палитра Material 3 из Figma (Material Theme Builder) =====
val primaryLight = Color(0xFF6A538C)
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFFEDDCFF)
val onPrimaryContainerLight = Color(0xFF523C73)
val secondaryLight = Color(0xFF645A70)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFFEBDDF7)
val onSecondaryContainerLight = Color(0xFF4C4357)
val tertiaryLight = Color(0xFF7F525B)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFFFFD9DF)
val onTertiaryContainerLight = Color(0xFF643B44)
val errorLight = Color(0xFFBA1A1A)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFFFDAD6)
val onErrorContainerLight = Color(0xFF93000A)
val backgroundLight = Color(0xFFFEF7FF)
val onBackgroundLight = Color(0xFF1D1A20)
val surfaceLight = Color(0xFFFEF7FF)
val onSurfaceLight = Color(0xFF1D1A20)
val surfaceVariantLight = Color(0xFFE8E0EB)
val onSurfaceVariantLight = Color(0xFF4A454E)
val outlineLight = Color(0xFF7B757F)
val outlineVariantLight = Color(0xFFCBC4CF)
val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF322F35)
val inverseOnSurfaceLight = Color(0xFFF6EEF7)
val inversePrimaryLight = Color(0xFFD6BBFB)
val surfaceDimLight = Color(0xFFDFD8E0)
val surfaceBrightLight = Color(0xFFFEF7FF)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFF9F1F9)
val surfaceContainerLight = Color(0xFFF3ECF4)
val surfaceContainerHighLight = Color(0xFFEDE6EE)
val surfaceContainerHighestLight = Color(0xFFE7E0E8)

// ===== Собственные цвета приложения (их нет в палитре Material 3) =====

// Срок списания: больше 7 дней — зелёный
val StatusOkContainer = Color(0xFFD7F0D8)
val OnStatusOkContainer = Color(0xFF1E5E2A)

// Срок списания: 4–7 дней — оранжевый
val StatusSoonContainer = Color(0xFFFFE0B2)
val OnStatusSoonContainer = Color(0xFF8A4B00)

// Срок списания: до 3 дней — используем errorContainer / onErrorContainer из палитры

// Цвета категорий (диаграмма и полоски на карточках)
val CategoryWork = primaryLight
val CategoryEntertainment = tertiaryLight
val CategoryEducation = Color(0xFF1D9E75)
val CategoryMusic = Color(0xFFEF9F27)
val CategoryOther = outlineLight
