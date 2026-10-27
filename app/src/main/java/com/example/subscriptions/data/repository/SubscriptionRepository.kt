package com.example.subscriptions.data.repository

import com.example.subscriptions.data.local.SubscriptionDao
import com.example.subscriptions.data.local.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

/** Работа с подписками: получение, сохранение, удаление */
class SubscriptionRepository(private val dao: SubscriptionDao) {

    fun observeAll(userId: Long): Flow<List<SubscriptionEntity>> = dao.observeAll(userId)

    fun observe(id: Long): Flow<SubscriptionEntity?> = dao.observeById(id)

    suspend fun get(id: Long): SubscriptionEntity? = dao.getById(id)

    /** Новая подписка (id = 0) добавляется, существующая — обновляется */
    suspend fun save(subscription: SubscriptionEntity) {
        if (subscription.id == 0L) dao.insert(subscription) else dao.update(subscription)
    }

    suspend fun delete(id: Long) = dao.deleteById(id)
}
