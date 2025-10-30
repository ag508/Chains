package com.chain.messaging.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migrations for Chain messaging app
 */
object DatabaseMigrations {
    
    /**
     * Migration from version 1 to 2 - Adding media and reactions tables
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create media table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `media` (
                    `id` TEXT NOT NULL,
                    `messageId` TEXT NOT NULL,
                    `fileName` TEXT NOT NULL,
                    `filePath` TEXT NOT NULL,
                    `mimeType` TEXT NOT NULL,
                    `fileSize` INTEGER NOT NULL,
                    `width` INTEGER,
                    `height` INTEGER,
                    `duration` INTEGER,
                    `thumbnailPath` TEXT,
                    `isEncrypted` INTEGER NOT NULL DEFAULT 1,
                    `encryptionKey` TEXT,
                    `cloudUrl` TEXT,
                    `cloudService` TEXT,
                    `isUploaded` INTEGER NOT NULL DEFAULT 0,
                    `uploadProgress` INTEGER NOT NULL DEFAULT 0,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`messageId`) REFERENCES `messages`(`id`) ON DELETE CASCADE
                )
            """.trimIndent())
            
            // Create indexes for media table
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_media_messageId` ON `media` (`messageId`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_media_filePath` ON `media` (`filePath`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_media_createdAt` ON `media` (`createdAt`)")
            
            // Create reactions table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `reactions` (
                    `id` TEXT NOT NULL,
                    `messageId` TEXT NOT NULL,
                    `userId` TEXT NOT NULL,
                    `emoji` TEXT NOT NULL,
                    `timestamp` INTEGER NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`messageId`) REFERENCES `messages`(`id`) ON DELETE CASCADE,
                    FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON DELETE CASCADE
                )
            """.trimIndent())
            
            // Create indexes for reactions table
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_reactions_messageId` ON `reactions` (`messageId`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_reactions_userId` ON `reactions` (`userId`)")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_reactions_messageId_userId_emoji` ON `reactions` (`messageId`, `userId`, `emoji`)")
        }
    }
    
    /**
     * Migration from version 2 to 3 - Adding full-text search support
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create FTS table for message search
            database.execSQL("""
                CREATE VIRTUAL TABLE IF NOT EXISTS `messages_fts` USING fts4(
                    content='messages',
                    content_rowid='rowid',
                    `id`,
                    `chatId`,
                    `senderId`,
                    `content`,
                    `type`
                )
            """.trimIndent())
            
            // Populate FTS table with existing messages
            database.execSQL("""
                INSERT INTO `messages_fts`(`id`, `chatId`, `senderId`, `content`, `type`)
                SELECT `id`, `chatId`, `senderId`, `content`, `type` FROM `messages`
            """.trimIndent())
            
            // Create triggers to keep FTS table in sync
            database.execSQL("""
                CREATE TRIGGER IF NOT EXISTS `messages_fts_insert` AFTER INSERT ON `messages` BEGIN
                    INSERT INTO `messages_fts`(`id`, `chatId`, `senderId`, `content`, `type`)
                    VALUES (new.`id`, new.`chatId`, new.`senderId`, new.`content`, new.`type`);
                END
            """.trimIndent())
            
            database.execSQL("""
                CREATE TRIGGER IF NOT EXISTS `messages_fts_update` AFTER UPDATE ON `messages` BEGIN
                    UPDATE `messages_fts` SET
                        `chatId` = new.`chatId`,
                        `senderId` = new.`senderId`,
                        `content` = new.`content`,
                        `type` = new.`type`
                    WHERE `id` = new.`id`;
                END
            """.trimIndent())
            
            database.execSQL("""
                CREATE TRIGGER IF NOT EXISTS `messages_fts_delete` AFTER DELETE ON `messages` BEGIN
                    DELETE FROM `messages_fts` WHERE `id` = old.`id`;
                END
            """.trimIndent())
        }
    }
    
    /**
     * Migration from version 3 to 4 - Adding additional indexes for performance
     */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add composite indexes for better query performance
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_chatId_timestamp` ON `messages` (`chatId`, `timestamp`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_senderId_timestamp` ON `messages` (`senderId`, `timestamp`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_status_timestamp` ON `messages` (`status`, `timestamp`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_isDisappearing_expiresAt` ON `messages` (`isDisappearing`, `expiresAt`)")
            
            // Add indexes for chat queries
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_chats_type_updatedAt` ON `chats` (`type`, `updatedAt`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_chats_isArchived_updatedAt` ON `chats` (`isArchived`, `updatedAt`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_chats_isPinned_updatedAt` ON `chats` (`isPinned`, `updatedAt`)")
            
            // Add indexes for user queries
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_users_status_lastSeen` ON `users` (`status`, `lastSeen`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_users_displayName` ON `users` (`displayName`)")
        }
    }
    
    /**
     * Get all migrations
     */
    fun getAllMigrations(): Array<Migration> {
        return arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4
        )
    }
}