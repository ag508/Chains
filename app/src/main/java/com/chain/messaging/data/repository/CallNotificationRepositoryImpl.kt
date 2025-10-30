package com.chain.messaging.data.repository

import com.chain.messaging.data.local.dao.CallNotificationDao
import com.chain.messaging.data.local.entity.toEntity
import com.chain.messaging.data.local.entity.toDomain
import com.chain.messaging.domain.model.CallNotification
import com.chain.messaging.domain.repository.CallNotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of CallNotificationRepository
 */
@Singleton
class CallNotificationRepositoryImpl @Inject constructor(
    private val callNotificationDao: CallNotificationDao
) : CallNotificationRepository {

    override suspend fun getAllCallNotifications(): List<CallNotification> {
        return callNotificationDao.getAllCallNotifications().map { it.toDomain() }
    }

    override fun observeCallNotifications(): Flow<List<CallNotification>> {
        return callNotificationDao.observeAllCallNotifications()
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun saveCallNotification(notification: CallNotification): Result<Unit> {
        return try {
            callNotificationDao.insertCallNotification(notification.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearCallNotification(notificationId: String): Result<Unit> {
        return try {
            callNotificationDao.clearCallNotification(notificationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearAllCallNotifications(): Result<Unit> {
        return try {
            callNotificationDao.deleteAllCallNotifications()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMissedCallCount(): Int {
        return callNotificationDao.getMissedCallCount()
    }
}
