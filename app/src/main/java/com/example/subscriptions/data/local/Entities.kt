package com.example.subscriptions.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/** Периодичность списания */
enum class BillingPeriod(val title: String) {
    WEEK("Каждую неделю"),
    MONTH("Каждый месяц"),
    YEAR("Каждый год")
}

/** Категория подписки */
enum class Category(val title: String) {
    ENTERTAINMENT("Развлечения"),
    MUSIC("Музыка"),
    WORK("Работа"),
    EDUCATION("Обучение"),
    OTHER("Другое")
}

/** Таблица пользователей. Email уникален, пароль хранится только в виде хэша. */
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val salt: String
)

/** Таблица подписок. Каждая подписка принадлежит пользователю. */
@Entity(
    tableName = "subscriptions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val price: Double,
    val currency: String = "RUB",
    val period: BillingPeriod = BillingPeriod.MONTH,
    val category: Category = Category.OTHER,
    /** Дата следующего списания (количество дней от 01.01.1970) */
    val nextPaymentEpochDay: Long,
    val reminderEnabled: Boolean = true,
    /** Дата добавления подписки */
    val createdEpochDay: Long = LocalDate.now().toEpochDay()
)
