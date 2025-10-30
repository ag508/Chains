package com.chain.messaging.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.chain.messaging.data.local.dao.ChatDao;
import com.chain.messaging.data.local.dao.ChatDao_Impl;
import com.chain.messaging.data.local.dao.DeviceDao;
import com.chain.messaging.data.local.dao.DeviceDao_Impl;
import com.chain.messaging.data.local.dao.MessageDao;
import com.chain.messaging.data.local.dao.MessageDao_Impl;
import com.chain.messaging.data.local.dao.PerformanceDao;
import com.chain.messaging.data.local.dao.PerformanceDao_Impl;
import com.chain.messaging.data.local.dao.QueuedMessageDao;
import com.chain.messaging.data.local.dao.QueuedMessageDao_Impl;
import com.chain.messaging.data.local.dao.ReactionDao;
import com.chain.messaging.data.local.dao.ReactionDao_Impl;
import com.chain.messaging.data.local.dao.SecurityEventDao;
import com.chain.messaging.data.local.dao.SecurityEventDao_Impl;
import com.chain.messaging.data.local.dao.SyncLogDao;
import com.chain.messaging.data.local.dao.SyncLogDao_Impl;
import com.chain.messaging.data.local.dao.UserDao;
import com.chain.messaging.data.local.dao.UserDao_Impl;
import com.chain.messaging.data.local.dao.UserSettingsDao;
import com.chain.messaging.data.local.dao.UserSettingsDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ChainDatabase_Impl extends ChainDatabase {
  private volatile UserDao _userDao;

  private volatile MessageDao _messageDao;

  private volatile ChatDao _chatDao;

  private volatile ReactionDao _reactionDao;

  private volatile PerformanceDao _performanceDao;

  private volatile QueuedMessageDao _queuedMessageDao;

  private volatile DeviceDao _deviceDao;

  private volatile SecurityEventDao _securityEventDao;

  private volatile SyncLogDao _syncLogDao;

  private volatile UserSettingsDao _userSettingsDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` TEXT NOT NULL, `publicKey` TEXT NOT NULL, `displayName` TEXT NOT NULL, `avatar` TEXT, `status` TEXT NOT NULL, `lastSeen` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `messages` (`id` TEXT NOT NULL, `chatId` TEXT NOT NULL, `senderId` TEXT NOT NULL, `content` TEXT NOT NULL, `type` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `status` TEXT NOT NULL, `replyTo` TEXT, `isEncrypted` INTEGER NOT NULL, `disappearingMessageTimer` INTEGER, `expiresAt` INTEGER, `isDisappearing` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_chatId` ON `messages` (`chatId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_senderId` ON `messages` (`senderId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_timestamp` ON `messages` (`timestamp`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `chats` (`id` TEXT NOT NULL, `type` TEXT NOT NULL, `name` TEXT NOT NULL, `participants` TEXT NOT NULL, `admins` TEXT NOT NULL, `isNotificationsEnabled` INTEGER NOT NULL, `disappearingMessagesTimer` INTEGER, `isArchived` INTEGER NOT NULL, `isPinned` INTEGER NOT NULL, `isMuted` INTEGER NOT NULL, `unreadCount` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `reactions` (`id` TEXT NOT NULL, `messageId` TEXT NOT NULL, `userId` TEXT NOT NULL, `emoji` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`messageId`) REFERENCES `messages`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_reactions_messageId` ON `reactions` (`messageId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_reactions_userId` ON `reactions` (`userId`)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_reactions_messageId_userId_emoji` ON `reactions` (`messageId`, `userId`, `emoji`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `performance_metrics` (`timestamp` INTEGER NOT NULL, `messagesPerSecond` REAL NOT NULL, `averageLatencyMs` INTEGER NOT NULL, `totalMessages` INTEGER NOT NULL, `usedMemoryMb` INTEGER NOT NULL, `totalMemoryMb` INTEGER NOT NULL, `memoryUsagePercentage` REAL NOT NULL, `gcCount` INTEGER NOT NULL, `gcTimeMs` INTEGER NOT NULL, `batteryLevel` REAL NOT NULL, `isCharging` INTEGER NOT NULL, `batteryDrainRate` REAL NOT NULL, `networkLatencyMs` INTEGER NOT NULL, `networkThroughputKbps` INTEGER NOT NULL, `networkQuality` TEXT NOT NULL, `cpuUsagePercentage` REAL NOT NULL, `threadCount` INTEGER NOT NULL, PRIMARY KEY(`timestamp`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `performance_alerts` (`id` TEXT NOT NULL, `type` TEXT NOT NULL, `severity` TEXT NOT NULL, `message` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `metricsJson` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `queued_messages` (`id` TEXT NOT NULL, `messageId` TEXT NOT NULL, `chatId` TEXT NOT NULL, `senderId` TEXT NOT NULL, `content` TEXT NOT NULL, `messageType` TEXT NOT NULL, `queuedAt` TEXT NOT NULL, `retryCount` INTEGER NOT NULL, `lastRetryAt` TEXT, `priority` TEXT NOT NULL, `maxRetries` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `registered_devices` (`deviceId` TEXT NOT NULL, `deviceName` TEXT NOT NULL, `deviceType` TEXT NOT NULL, `platform` TEXT NOT NULL, `platformVersion` TEXT NOT NULL, `appVersion` TEXT NOT NULL, `publicKey` TEXT NOT NULL, `lastSeen` TEXT NOT NULL, `registeredAt` TEXT NOT NULL, `isTrusted` INTEGER NOT NULL, `lastSyncAt` TEXT, `syncStatus` TEXT NOT NULL, `isCurrentDevice` INTEGER NOT NULL, PRIMARY KEY(`deviceId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `security_events` (`id` TEXT NOT NULL, `type` TEXT NOT NULL, `timestamp` TEXT NOT NULL, `severity` TEXT NOT NULL, `description` TEXT NOT NULL, `metadata` TEXT NOT NULL, `userId` TEXT, `deviceId` TEXT, `ipAddress` TEXT, `isAcknowledged` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `sync_logs` (`id` TEXT NOT NULL, `deviceId` TEXT NOT NULL, `syncType` TEXT NOT NULL, `status` TEXT NOT NULL, `startTime` TEXT NOT NULL, `endTime` TEXT, `messagesSynced` INTEGER NOT NULL, `keysSynced` INTEGER NOT NULL, `settingsSynced` INTEGER NOT NULL, `errorMessage` TEXT, `retryCount` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `user_settings` (`userId` TEXT NOT NULL, `displayName` TEXT NOT NULL, `bio` TEXT, `avatar` TEXT, `phoneNumber` TEXT, `email` TEXT, `showOnlineStatus` INTEGER NOT NULL, `showLastSeen` INTEGER NOT NULL, `readReceipts` INTEGER NOT NULL, `typingIndicators` INTEGER NOT NULL, `profilePhotoVisibility` TEXT NOT NULL, `lastSeenVisibility` TEXT NOT NULL, `onlineStatusVisibility` TEXT NOT NULL, `groupInvitePermission` TEXT NOT NULL, `disappearingMessagesDefault` TEXT NOT NULL, `screenshotNotifications` INTEGER NOT NULL, `forwardingRestriction` INTEGER NOT NULL, `messageNotifications` INTEGER NOT NULL, `callNotifications` INTEGER NOT NULL, `groupNotifications` INTEGER NOT NULL, `soundEnabled` INTEGER NOT NULL, `vibrationEnabled` INTEGER NOT NULL, `ledEnabled` INTEGER NOT NULL, `notificationSound` TEXT NOT NULL, `quietHoursEnabled` INTEGER NOT NULL, `quietHoursStart` TEXT NOT NULL, `quietHoursEnd` TEXT NOT NULL, `showPreview` INTEGER NOT NULL, `showSenderName` INTEGER NOT NULL, `theme` TEXT NOT NULL, `fontSize` TEXT NOT NULL, `chatWallpaper` TEXT, `useSystemEmojis` INTEGER NOT NULL, `showAvatarsInGroups` INTEGER NOT NULL, `compactMode` INTEGER NOT NULL, `highContrast` INTEGER NOT NULL, `largeText` INTEGER NOT NULL, `reduceMotion` INTEGER NOT NULL, `screenReaderOptimized` INTEGER NOT NULL, `hapticFeedback` INTEGER NOT NULL, `voiceOverEnabled` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`userId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '8f8e95fd6102c024fedee66593837d98')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `users`");
        db.execSQL("DROP TABLE IF EXISTS `messages`");
        db.execSQL("DROP TABLE IF EXISTS `chats`");
        db.execSQL("DROP TABLE IF EXISTS `reactions`");
        db.execSQL("DROP TABLE IF EXISTS `performance_metrics`");
        db.execSQL("DROP TABLE IF EXISTS `performance_alerts`");
        db.execSQL("DROP TABLE IF EXISTS `queued_messages`");
        db.execSQL("DROP TABLE IF EXISTS `registered_devices`");
        db.execSQL("DROP TABLE IF EXISTS `security_events`");
        db.execSQL("DROP TABLE IF EXISTS `sync_logs`");
        db.execSQL("DROP TABLE IF EXISTS `user_settings`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsUsers = new HashMap<String, TableInfo.Column>(8);
        _columnsUsers.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("publicKey", new TableInfo.Column("publicKey", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("displayName", new TableInfo.Column("displayName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("avatar", new TableInfo.Column("avatar", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("lastSeen", new TableInfo.Column("lastSeen", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUsers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUsers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUsers = new TableInfo("users", _columnsUsers, _foreignKeysUsers, _indicesUsers);
        final TableInfo _existingUsers = TableInfo.read(db, "users");
        if (!_infoUsers.equals(_existingUsers)) {
          return new RoomOpenHelper.ValidationResult(false, "users(com.chain.messaging.data.local.entity.UserEntity).\n"
                  + " Expected:\n" + _infoUsers + "\n"
                  + " Found:\n" + _existingUsers);
        }
        final HashMap<String, TableInfo.Column> _columnsMessages = new HashMap<String, TableInfo.Column>(14);
        _columnsMessages.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("chatId", new TableInfo.Column("chatId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("senderId", new TableInfo.Column("senderId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("content", new TableInfo.Column("content", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("replyTo", new TableInfo.Column("replyTo", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("isEncrypted", new TableInfo.Column("isEncrypted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("disappearingMessageTimer", new TableInfo.Column("disappearingMessageTimer", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("expiresAt", new TableInfo.Column("expiresAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("isDisappearing", new TableInfo.Column("isDisappearing", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMessages = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMessages = new HashSet<TableInfo.Index>(3);
        _indicesMessages.add(new TableInfo.Index("index_messages_chatId", false, Arrays.asList("chatId"), Arrays.asList("ASC")));
        _indicesMessages.add(new TableInfo.Index("index_messages_senderId", false, Arrays.asList("senderId"), Arrays.asList("ASC")));
        _indicesMessages.add(new TableInfo.Index("index_messages_timestamp", false, Arrays.asList("timestamp"), Arrays.asList("ASC")));
        final TableInfo _infoMessages = new TableInfo("messages", _columnsMessages, _foreignKeysMessages, _indicesMessages);
        final TableInfo _existingMessages = TableInfo.read(db, "messages");
        if (!_infoMessages.equals(_existingMessages)) {
          return new RoomOpenHelper.ValidationResult(false, "messages(com.chain.messaging.data.local.entity.MessageEntity).\n"
                  + " Expected:\n" + _infoMessages + "\n"
                  + " Found:\n" + _existingMessages);
        }
        final HashMap<String, TableInfo.Column> _columnsChats = new HashMap<String, TableInfo.Column>(13);
        _columnsChats.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("participants", new TableInfo.Column("participants", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("admins", new TableInfo.Column("admins", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("isNotificationsEnabled", new TableInfo.Column("isNotificationsEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("disappearingMessagesTimer", new TableInfo.Column("disappearingMessagesTimer", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("isArchived", new TableInfo.Column("isArchived", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("isPinned", new TableInfo.Column("isPinned", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("isMuted", new TableInfo.Column("isMuted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("unreadCount", new TableInfo.Column("unreadCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysChats = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesChats = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoChats = new TableInfo("chats", _columnsChats, _foreignKeysChats, _indicesChats);
        final TableInfo _existingChats = TableInfo.read(db, "chats");
        if (!_infoChats.equals(_existingChats)) {
          return new RoomOpenHelper.ValidationResult(false, "chats(com.chain.messaging.data.local.entity.ChatEntity).\n"
                  + " Expected:\n" + _infoChats + "\n"
                  + " Found:\n" + _existingChats);
        }
        final HashMap<String, TableInfo.Column> _columnsReactions = new HashMap<String, TableInfo.Column>(6);
        _columnsReactions.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReactions.put("messageId", new TableInfo.Column("messageId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReactions.put("userId", new TableInfo.Column("userId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReactions.put("emoji", new TableInfo.Column("emoji", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReactions.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReactions.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysReactions = new HashSet<TableInfo.ForeignKey>(2);
        _foreignKeysReactions.add(new TableInfo.ForeignKey("messages", "CASCADE", "NO ACTION", Arrays.asList("messageId"), Arrays.asList("id")));
        _foreignKeysReactions.add(new TableInfo.ForeignKey("users", "CASCADE", "NO ACTION", Arrays.asList("userId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesReactions = new HashSet<TableInfo.Index>(3);
        _indicesReactions.add(new TableInfo.Index("index_reactions_messageId", false, Arrays.asList("messageId"), Arrays.asList("ASC")));
        _indicesReactions.add(new TableInfo.Index("index_reactions_userId", false, Arrays.asList("userId"), Arrays.asList("ASC")));
        _indicesReactions.add(new TableInfo.Index("index_reactions_messageId_userId_emoji", true, Arrays.asList("messageId", "userId", "emoji"), Arrays.asList("ASC", "ASC", "ASC")));
        final TableInfo _infoReactions = new TableInfo("reactions", _columnsReactions, _foreignKeysReactions, _indicesReactions);
        final TableInfo _existingReactions = TableInfo.read(db, "reactions");
        if (!_infoReactions.equals(_existingReactions)) {
          return new RoomOpenHelper.ValidationResult(false, "reactions(com.chain.messaging.data.local.entity.ReactionEntity).\n"
                  + " Expected:\n" + _infoReactions + "\n"
                  + " Found:\n" + _existingReactions);
        }
        final HashMap<String, TableInfo.Column> _columnsPerformanceMetrics = new HashMap<String, TableInfo.Column>(17);
        _columnsPerformanceMetrics.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerformanceMetrics.put("messagesPerSecond", new TableInfo.Column("messagesPerSecond", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerformanceMetrics.put("averageLatencyMs", new TableInfo.Column("averageLatencyMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerformanceMetrics.put("totalMessages", new TableInfo.Column("totalMessages", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerformanceMetrics.put("usedMemoryMb", new TableInfo.Column("usedMemoryMb", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerformanceMetrics.put("totalMemoryMb", new TableInfo.Column("totalMemoryMb", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerformanceMetrics.put("memoryUsagePercentage", new TableInfo.Column("memoryUsagePercentage", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerformanceMetrics.put("gcCount", new TableInfo.Column("gcCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerformanceMetrics.put("gcTimeMs", new TableInfo.Column("gcTimeMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerformanceMetrics.put("batteryLevel", new TableInfo.Column("batteryLevel", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerformanceMetrics.put("isCharging", new TableInfo.Column("isCharging", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerformanceMetrics.put("batteryDrainRate", new TableInfo.Column("batteryDrainRate", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerformanceMetrics.put("networkLatencyMs", new TableInfo.Column("networkLatencyMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerformanceMetrics.put("networkThroughputKbps", new TableInfo.Column("networkThroughputKbps", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerformanceMetrics.put("networkQuality", new TableInfo.Column("networkQuality", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerformanceMetrics.put("cpuUsagePercentage", new TableInfo.Column("cpuUsagePercentage", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerformanceMetrics.put("threadCount", new TableInfo.Column("threadCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPerformanceMetrics = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPerformanceMetrics = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPerformanceMetrics = new TableInfo("performance_metrics", _columnsPerformanceMetrics, _foreignKeysPerformanceMetrics, _indicesPerformanceMetrics);
        final TableInfo _existingPerformanceMetrics = TableInfo.read(db, "performance_metrics");
        if (!_infoPerformanceMetrics.equals(_existingPerformanceMetrics)) {
          return new RoomOpenHelper.ValidationResult(false, "performance_metrics(com.chain.messaging.data.local.entity.PerformanceMetricsEntity).\n"
                  + " Expected:\n" + _infoPerformanceMetrics + "\n"
                  + " Found:\n" + _existingPerformanceMetrics);
        }
        final HashMap<String, TableInfo.Column> _columnsPerformanceAlerts = new HashMap<String, TableInfo.Column>(6);
        _columnsPerformanceAlerts.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerformanceAlerts.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerformanceAlerts.put("severity", new TableInfo.Column("severity", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerformanceAlerts.put("message", new TableInfo.Column("message", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerformanceAlerts.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerformanceAlerts.put("metricsJson", new TableInfo.Column("metricsJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPerformanceAlerts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPerformanceAlerts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPerformanceAlerts = new TableInfo("performance_alerts", _columnsPerformanceAlerts, _foreignKeysPerformanceAlerts, _indicesPerformanceAlerts);
        final TableInfo _existingPerformanceAlerts = TableInfo.read(db, "performance_alerts");
        if (!_infoPerformanceAlerts.equals(_existingPerformanceAlerts)) {
          return new RoomOpenHelper.ValidationResult(false, "performance_alerts(com.chain.messaging.data.local.entity.PerformanceAlertEntity).\n"
                  + " Expected:\n" + _infoPerformanceAlerts + "\n"
                  + " Found:\n" + _existingPerformanceAlerts);
        }
        final HashMap<String, TableInfo.Column> _columnsQueuedMessages = new HashMap<String, TableInfo.Column>(11);
        _columnsQueuedMessages.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQueuedMessages.put("messageId", new TableInfo.Column("messageId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQueuedMessages.put("chatId", new TableInfo.Column("chatId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQueuedMessages.put("senderId", new TableInfo.Column("senderId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQueuedMessages.put("content", new TableInfo.Column("content", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQueuedMessages.put("messageType", new TableInfo.Column("messageType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQueuedMessages.put("queuedAt", new TableInfo.Column("queuedAt", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQueuedMessages.put("retryCount", new TableInfo.Column("retryCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQueuedMessages.put("lastRetryAt", new TableInfo.Column("lastRetryAt", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQueuedMessages.put("priority", new TableInfo.Column("priority", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQueuedMessages.put("maxRetries", new TableInfo.Column("maxRetries", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysQueuedMessages = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesQueuedMessages = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoQueuedMessages = new TableInfo("queued_messages", _columnsQueuedMessages, _foreignKeysQueuedMessages, _indicesQueuedMessages);
        final TableInfo _existingQueuedMessages = TableInfo.read(db, "queued_messages");
        if (!_infoQueuedMessages.equals(_existingQueuedMessages)) {
          return new RoomOpenHelper.ValidationResult(false, "queued_messages(com.chain.messaging.data.local.entity.QueuedMessageEntity).\n"
                  + " Expected:\n" + _infoQueuedMessages + "\n"
                  + " Found:\n" + _existingQueuedMessages);
        }
        final HashMap<String, TableInfo.Column> _columnsRegisteredDevices = new HashMap<String, TableInfo.Column>(13);
        _columnsRegisteredDevices.put("deviceId", new TableInfo.Column("deviceId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRegisteredDevices.put("deviceName", new TableInfo.Column("deviceName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRegisteredDevices.put("deviceType", new TableInfo.Column("deviceType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRegisteredDevices.put("platform", new TableInfo.Column("platform", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRegisteredDevices.put("platformVersion", new TableInfo.Column("platformVersion", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRegisteredDevices.put("appVersion", new TableInfo.Column("appVersion", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRegisteredDevices.put("publicKey", new TableInfo.Column("publicKey", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRegisteredDevices.put("lastSeen", new TableInfo.Column("lastSeen", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRegisteredDevices.put("registeredAt", new TableInfo.Column("registeredAt", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRegisteredDevices.put("isTrusted", new TableInfo.Column("isTrusted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRegisteredDevices.put("lastSyncAt", new TableInfo.Column("lastSyncAt", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRegisteredDevices.put("syncStatus", new TableInfo.Column("syncStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRegisteredDevices.put("isCurrentDevice", new TableInfo.Column("isCurrentDevice", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRegisteredDevices = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesRegisteredDevices = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoRegisteredDevices = new TableInfo("registered_devices", _columnsRegisteredDevices, _foreignKeysRegisteredDevices, _indicesRegisteredDevices);
        final TableInfo _existingRegisteredDevices = TableInfo.read(db, "registered_devices");
        if (!_infoRegisteredDevices.equals(_existingRegisteredDevices)) {
          return new RoomOpenHelper.ValidationResult(false, "registered_devices(com.chain.messaging.data.local.entity.RegisteredDeviceEntity).\n"
                  + " Expected:\n" + _infoRegisteredDevices + "\n"
                  + " Found:\n" + _existingRegisteredDevices);
        }
        final HashMap<String, TableInfo.Column> _columnsSecurityEvents = new HashMap<String, TableInfo.Column>(10);
        _columnsSecurityEvents.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSecurityEvents.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSecurityEvents.put("timestamp", new TableInfo.Column("timestamp", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSecurityEvents.put("severity", new TableInfo.Column("severity", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSecurityEvents.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSecurityEvents.put("metadata", new TableInfo.Column("metadata", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSecurityEvents.put("userId", new TableInfo.Column("userId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSecurityEvents.put("deviceId", new TableInfo.Column("deviceId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSecurityEvents.put("ipAddress", new TableInfo.Column("ipAddress", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSecurityEvents.put("isAcknowledged", new TableInfo.Column("isAcknowledged", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSecurityEvents = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSecurityEvents = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSecurityEvents = new TableInfo("security_events", _columnsSecurityEvents, _foreignKeysSecurityEvents, _indicesSecurityEvents);
        final TableInfo _existingSecurityEvents = TableInfo.read(db, "security_events");
        if (!_infoSecurityEvents.equals(_existingSecurityEvents)) {
          return new RoomOpenHelper.ValidationResult(false, "security_events(com.chain.messaging.data.local.entity.SecurityEventEntity).\n"
                  + " Expected:\n" + _infoSecurityEvents + "\n"
                  + " Found:\n" + _existingSecurityEvents);
        }
        final HashMap<String, TableInfo.Column> _columnsSyncLogs = new HashMap<String, TableInfo.Column>(11);
        _columnsSyncLogs.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncLogs.put("deviceId", new TableInfo.Column("deviceId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncLogs.put("syncType", new TableInfo.Column("syncType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncLogs.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncLogs.put("startTime", new TableInfo.Column("startTime", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncLogs.put("endTime", new TableInfo.Column("endTime", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncLogs.put("messagesSynced", new TableInfo.Column("messagesSynced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncLogs.put("keysSynced", new TableInfo.Column("keysSynced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncLogs.put("settingsSynced", new TableInfo.Column("settingsSynced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncLogs.put("errorMessage", new TableInfo.Column("errorMessage", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncLogs.put("retryCount", new TableInfo.Column("retryCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSyncLogs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSyncLogs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSyncLogs = new TableInfo("sync_logs", _columnsSyncLogs, _foreignKeysSyncLogs, _indicesSyncLogs);
        final TableInfo _existingSyncLogs = TableInfo.read(db, "sync_logs");
        if (!_infoSyncLogs.equals(_existingSyncLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "sync_logs(com.chain.messaging.data.local.entity.SyncLogEntity).\n"
                  + " Expected:\n" + _infoSyncLogs + "\n"
                  + " Found:\n" + _existingSyncLogs);
        }
        final HashMap<String, TableInfo.Column> _columnsUserSettings = new HashMap<String, TableInfo.Column>(42);
        _columnsUserSettings.put("userId", new TableInfo.Column("userId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("displayName", new TableInfo.Column("displayName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("bio", new TableInfo.Column("bio", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("avatar", new TableInfo.Column("avatar", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("phoneNumber", new TableInfo.Column("phoneNumber", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("email", new TableInfo.Column("email", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("showOnlineStatus", new TableInfo.Column("showOnlineStatus", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("showLastSeen", new TableInfo.Column("showLastSeen", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("readReceipts", new TableInfo.Column("readReceipts", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("typingIndicators", new TableInfo.Column("typingIndicators", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("profilePhotoVisibility", new TableInfo.Column("profilePhotoVisibility", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("lastSeenVisibility", new TableInfo.Column("lastSeenVisibility", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("onlineStatusVisibility", new TableInfo.Column("onlineStatusVisibility", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("groupInvitePermission", new TableInfo.Column("groupInvitePermission", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("disappearingMessagesDefault", new TableInfo.Column("disappearingMessagesDefault", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("screenshotNotifications", new TableInfo.Column("screenshotNotifications", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("forwardingRestriction", new TableInfo.Column("forwardingRestriction", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("messageNotifications", new TableInfo.Column("messageNotifications", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("callNotifications", new TableInfo.Column("callNotifications", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("groupNotifications", new TableInfo.Column("groupNotifications", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("soundEnabled", new TableInfo.Column("soundEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("vibrationEnabled", new TableInfo.Column("vibrationEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("ledEnabled", new TableInfo.Column("ledEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("notificationSound", new TableInfo.Column("notificationSound", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("quietHoursEnabled", new TableInfo.Column("quietHoursEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("quietHoursStart", new TableInfo.Column("quietHoursStart", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("quietHoursEnd", new TableInfo.Column("quietHoursEnd", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("showPreview", new TableInfo.Column("showPreview", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("showSenderName", new TableInfo.Column("showSenderName", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("theme", new TableInfo.Column("theme", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("fontSize", new TableInfo.Column("fontSize", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("chatWallpaper", new TableInfo.Column("chatWallpaper", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("useSystemEmojis", new TableInfo.Column("useSystemEmojis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("showAvatarsInGroups", new TableInfo.Column("showAvatarsInGroups", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("compactMode", new TableInfo.Column("compactMode", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("highContrast", new TableInfo.Column("highContrast", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("largeText", new TableInfo.Column("largeText", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("reduceMotion", new TableInfo.Column("reduceMotion", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("screenReaderOptimized", new TableInfo.Column("screenReaderOptimized", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("hapticFeedback", new TableInfo.Column("hapticFeedback", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("voiceOverEnabled", new TableInfo.Column("voiceOverEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUserSettings = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUserSettings = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUserSettings = new TableInfo("user_settings", _columnsUserSettings, _foreignKeysUserSettings, _indicesUserSettings);
        final TableInfo _existingUserSettings = TableInfo.read(db, "user_settings");
        if (!_infoUserSettings.equals(_existingUserSettings)) {
          return new RoomOpenHelper.ValidationResult(false, "user_settings(com.chain.messaging.data.local.entity.UserSettingsEntity).\n"
                  + " Expected:\n" + _infoUserSettings + "\n"
                  + " Found:\n" + _existingUserSettings);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "8f8e95fd6102c024fedee66593837d98", "55d004b4beeb371330e194500d6f97f6");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "users","messages","chats","reactions","performance_metrics","performance_alerts","queued_messages","registered_devices","security_events","sync_logs","user_settings");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `users`");
      _db.execSQL("DELETE FROM `messages`");
      _db.execSQL("DELETE FROM `chats`");
      _db.execSQL("DELETE FROM `reactions`");
      _db.execSQL("DELETE FROM `performance_metrics`");
      _db.execSQL("DELETE FROM `performance_alerts`");
      _db.execSQL("DELETE FROM `queued_messages`");
      _db.execSQL("DELETE FROM `registered_devices`");
      _db.execSQL("DELETE FROM `security_events`");
      _db.execSQL("DELETE FROM `sync_logs`");
      _db.execSQL("DELETE FROM `user_settings`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(UserDao.class, UserDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MessageDao.class, MessageDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ChatDao.class, ChatDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ReactionDao.class, ReactionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PerformanceDao.class, PerformanceDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(QueuedMessageDao.class, QueuedMessageDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(DeviceDao.class, DeviceDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SecurityEventDao.class, SecurityEventDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SyncLogDao.class, SyncLogDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(UserSettingsDao.class, UserSettingsDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public UserDao userDao() {
    if (_userDao != null) {
      return _userDao;
    } else {
      synchronized(this) {
        if(_userDao == null) {
          _userDao = new UserDao_Impl(this);
        }
        return _userDao;
      }
    }
  }

  @Override
  public MessageDao messageDao() {
    if (_messageDao != null) {
      return _messageDao;
    } else {
      synchronized(this) {
        if(_messageDao == null) {
          _messageDao = new MessageDao_Impl(this);
        }
        return _messageDao;
      }
    }
  }

  @Override
  public ChatDao chatDao() {
    if (_chatDao != null) {
      return _chatDao;
    } else {
      synchronized(this) {
        if(_chatDao == null) {
          _chatDao = new ChatDao_Impl(this);
        }
        return _chatDao;
      }
    }
  }

  @Override
  public ReactionDao reactionDao() {
    if (_reactionDao != null) {
      return _reactionDao;
    } else {
      synchronized(this) {
        if(_reactionDao == null) {
          _reactionDao = new ReactionDao_Impl(this);
        }
        return _reactionDao;
      }
    }
  }

  @Override
  public PerformanceDao performanceDao() {
    if (_performanceDao != null) {
      return _performanceDao;
    } else {
      synchronized(this) {
        if(_performanceDao == null) {
          _performanceDao = new PerformanceDao_Impl(this);
        }
        return _performanceDao;
      }
    }
  }

  @Override
  public QueuedMessageDao queuedMessageDao() {
    if (_queuedMessageDao != null) {
      return _queuedMessageDao;
    } else {
      synchronized(this) {
        if(_queuedMessageDao == null) {
          _queuedMessageDao = new QueuedMessageDao_Impl(this);
        }
        return _queuedMessageDao;
      }
    }
  }

  @Override
  public DeviceDao deviceDao() {
    if (_deviceDao != null) {
      return _deviceDao;
    } else {
      synchronized(this) {
        if(_deviceDao == null) {
          _deviceDao = new DeviceDao_Impl(this);
        }
        return _deviceDao;
      }
    }
  }

  @Override
  public SecurityEventDao securityEventDao() {
    if (_securityEventDao != null) {
      return _securityEventDao;
    } else {
      synchronized(this) {
        if(_securityEventDao == null) {
          _securityEventDao = new SecurityEventDao_Impl(this);
        }
        return _securityEventDao;
      }
    }
  }

  @Override
  public SyncLogDao syncLogDao() {
    if (_syncLogDao != null) {
      return _syncLogDao;
    } else {
      synchronized(this) {
        if(_syncLogDao == null) {
          _syncLogDao = new SyncLogDao_Impl(this);
        }
        return _syncLogDao;
      }
    }
  }

  @Override
  public UserSettingsDao userSettingsDao() {
    if (_userSettingsDao != null) {
      return _userSettingsDao;
    } else {
      synchronized(this) {
        if(_userSettingsDao == null) {
          _userSettingsDao = new UserSettingsDao_Impl(this);
        }
        return _userSettingsDao;
      }
    }
  }
}
