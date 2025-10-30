package com.chain.messaging

import com.chain.messaging.core.blockchain.BlockchainManager
import com.chain.messaging.core.crypto.SignalEncryptionService
import com.chain.messaging.core.messaging.MessagingService
import com.chain.messaging.core.p2p.P2PManager
import com.chain.messaging.data.local.ChainDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Singleton

/**
 * Test configuration module for comprehensive testing
 * Provides test doubles and configurations for the test environment
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [
        com.chain.messaging.di.DatabaseModule::class,
        com.chain.messaging.di.CryptoModule::class,
        com.chain.messaging.di.BlockchainModule::class
    ]
)
object TestConfiguration {

    @Provides
    @Singleton
    fun provideTestDatabase(): ChainDatabase {
        // Return in-memory test database
        return mockk<ChainDatabase>(relaxed = true)
    }

    @Provides
    @Singleton
    fun provideTestEncryptionService(): SignalEncryptionService {
        return mockk<SignalEncryptionService>(relaxed = true) {
            // Configure mock behaviors for testing
        }
    }

    @Provides
    @Singleton
    fun provideTestBlockchainManager(): BlockchainManager {
        return mockk<BlockchainManager>(relaxed = true)
    }

    @Provides
    @Singleton
    fun provideTestP2PManager(): P2PManager {
        return mockk<P2PManager>(relaxed = true)
    }

    @Provides
    @Singleton
    fun provideTestMessagingService(): MessagingService {
        return mockk<MessagingService>(relaxed = true)
    }
}

/**
 * Test utilities and helper functions
 */
object TestUtils {
    
    fun generateTestUserId(): String = "test_user_${System.currentTimeMillis()}"
    
    fun generateTestChatId(): String = "test_chat_${System.currentTimeMillis()}"
    
    fun generateTestGroupId(): String = "test_group_${System.currentTimeMillis()}"
    
    fun createTestMessage(
        content: String = "Test message",
        senderId: String = generateTestUserId(),
        recipientId: String = generateTestUserId()
    ) = com.chain.messaging.domain.model.Message(
        id = "msg_${System.currentTimeMillis()}",
        chatId = generateTestChatId(),
        senderId = senderId,
        content = content,
        type = com.chain.messaging.domain.model.MessageType.TEXT,
        timestamp = System.currentTimeMillis(),
        status = com.chain.messaging.domain.model.MessageStatus.SENDING
    )
    
    fun createTestUser(
        id: String = generateTestUserId(),
        displayName: String = "Test User"
    ) = com.chain.messaging.domain.model.User(
        id = id,
        publicKey = "test_public_key_$id",
        displayName = displayName,
        status = "online"
    )
}

/**
 * Test constants used across test suites
 */
object TestConstants {
    const val TEST_TIMEOUT_MS = 30000L
    const val NETWORK_DELAY_MS = 1000L
    const val ENCRYPTION_DELAY_MS = 500L
    const val BLOCKCHAIN_CONFIRMATION_DELAY_MS = 2000L
    
    const val LARGE_GROUP_SIZE = 10000
    const val MASSIVE_GROUP_SIZE = 100000
    const val HIGH_LOAD_MESSAGE_COUNT = 1000
    const val CONCURRENT_CALL_COUNT = 50
    
    const val MIN_THROUGHPUT_MESSAGES_PER_SECOND = 100
    const val MIN_SUCCESS_RATE_PERCENT = 95.0
    const val MIN_CALL_QUALITY_SCORE = 7.0
    
    const val MAX_MEMORY_INCREASE_PERCENT = 200.0
    const val MAX_MEMORY_INCREASE_MB = 500
}