package com.chain.messaging.domain.usecase

import com.chain.messaging.domain.model.UserSettings
import com.chain.messaging.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting user settings
 */
class GetUserSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    
    /**
     * Get user settings once
     */
    suspend operator fun invoke(userId: String): UserSettings? {
        return settingsRepository.getSettings(userId)
    }
    
    /**
     * Get user settings as Flow for reactive updates
     */
    fun asFlow(userId: String): Flow<UserSettings?> {
        return settingsRepository.getSettingsFlow(userId)
    }
}