package com.chain.messaging.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.chain.messaging.core.config.AppConfig
import com.chain.messaging.data.local.dao.ChatDao
import com.chain.messaging.data.local.dao.DeviceDao
// import com.chain.messaging.data.local.dao.MediaDao
import com.chain.messaging.data.local.dao.MessageDao
// import com.chain.messaging.data.local.dao.MessageSearchDao
import com.chain.messaging.data.local.dao.PerformanceDao
import com.chain.messaging.data.local.dao.QueuedMessageDao
import com.chain.messaging.data.local.dao.ReactionDao
import com.chain.messaging.data.local.dao.SecurityEventDao
import com.chain.messaging.data.local.dao.SyncLogDao
import com.chain.messaging.data.local.dao.UserDao
import com.chain.messaging.data.local.dao.UserSettingsDao
import com.chain.messaging.data.local.entity.ChatEntity
// import com.chain.messaging.data.local.entity.MediaEntity
import com.chain.messaging.data.local.entity.MessageEntity
import com.chain.messaging.data.local.entity.PerformanceAlertEntity
import com.chain.messaging.data.local.entity.PerformanceMetricsEntity
import com.chain.messaging.data.local.entity.QueuedMessageEntity
import com.chain.messaging.data.local.entity.ReactionEntity
import com.chain.messaging.data.local.entity.RegisteredDeviceEntity
import com.chain.messaging.data.local.entity.SecurityEventEntity
import com.chain.messaging.data.local.entity.SyncLogEntity
import com.chain.messaging.data.local.entity.UserEntity
import com.chain.messaging.data.local.entity.UserSettingsEntity
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

/**
 * Room database for Chain messaging app with SQLCipher encryption
 */
@Database(
    entities = [
        UserEntity::class,
        MessageEntity::class,
        ChatEntity::class,
        // MediaEntity::class,
        ReactionEntity::class,
        PerformanceMetricsEntity::class,
        PerformanceAlertEntity::class,
        QueuedMessageEntity::class,
        RegisteredDeviceEntity::class,
        SecurityEventEntity::class,
        SyncLogEntity::class,
        UserSettingsEntity::class
    ],
    version = AppConfig.DATABASE_VERSION,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ChainDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun chatDao(): ChatDao
    // abstract fun mediaDao(): MediaDao
    abstract fun reactionDao(): ReactionDao
    // abstract fun messageSearchDao(): MessageSearchDao
    abstract fun performanceDao(): PerformanceDao
    abstract fun queuedMessageDao(): QueuedMessageDao
    abstract fun deviceDao(): DeviceDao
    abstract fun securityEventDao(): SecurityEventDao
    abstract fun syncLogDao(): SyncLogDao
    abstract fun userSettingsDao(): UserSettingsDao
    
    companion object {
        
        @Volatile
        private var INSTANCE: ChainDatabase? = null
        
        fun getDatabase(context: Context, passphrase: String): ChainDatabase {
            return INSTANCE ?: synchronized(this) {
                val factory = SupportFactory(SQLiteDatabase.getBytes(passphrase.toCharArray()))
                
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChainDatabase::class.java,
                    AppConfig.DATABASE_NAME
                )
                    .openHelperFactory(factory)
                    // .addMigrations(*DatabaseMigrations.getAllMigrations())
                    .fallbackToDestructiveMigration()
                    .build()
                
                INSTANCE = instance
                instance
            }
        }
        
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}