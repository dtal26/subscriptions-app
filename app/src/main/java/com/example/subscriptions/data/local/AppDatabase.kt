package com.example.subscriptions.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** База данных приложения (SQLite через Room) */
@Database(
    entities = [UserEntity::class, SubscriptionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        /** Возвращает единственный экземпляр базы (паттерн Singleton) */
        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "subscriptions.db"
                ).build().also { instance = it }
            }
    }
}
