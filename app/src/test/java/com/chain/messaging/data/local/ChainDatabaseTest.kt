package com.chain.messaging.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chain.messaging.data.local.entity.UserEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Test for ChainDatabase functionality
 */
@RunWith(AndroidJUnit4::class)
class ChainDatabaseTest {
    
    private lateinit var database: ChainDatabase
    
    @Before
    fun setup() {
        // Use in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ChainDatabase::class.java
        ).allowMainThreadQueries().build()
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun insertAndRetrieveUser() = runBlocking {
        val user = UserEntity(
            id = "test-user-1",
            publicKey = "test-public-key",
            displayName = "Test User",
            avatar = null,
            status = "online",
            lastSeen = System.currentTimeMillis(),
            devices = emptyList()
        )
        
        database.userDao().insertUser(user)
        val retrievedUser = database.userDao().getUserById("test-user-1")
        
        assertNotNull(retrievedUser)
        assertEquals(user.id, retrievedUser?.id)
        assertEquals(user.displayName, retrievedUser?.displayName)
    }
}