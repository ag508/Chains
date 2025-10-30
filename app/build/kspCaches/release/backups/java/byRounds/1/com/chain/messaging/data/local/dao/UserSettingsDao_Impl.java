package com.chain.messaging.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.chain.messaging.data.local.entity.UserSettingsEntity;
import java.lang.Boolean;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class UserSettingsDao_Impl implements UserSettingsDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<UserSettingsEntity> __insertionAdapterOfUserSettingsEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateProfile;

  private final SharedSQLiteStatement __preparedStmtOfUpdatePrivacySettings;

  private final SharedSQLiteStatement __preparedStmtOfUpdateNotificationSettings;

  private final SharedSQLiteStatement __preparedStmtOfUpdateAppearanceSettings;

  private final SharedSQLiteStatement __preparedStmtOfUpdateAccessibilitySettings;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSettings;

  public UserSettingsDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUserSettingsEntity = new EntityInsertionAdapter<UserSettingsEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `user_settings` (`userId`,`displayName`,`bio`,`avatar`,`phoneNumber`,`email`,`showOnlineStatus`,`showLastSeen`,`readReceipts`,`typingIndicators`,`profilePhotoVisibility`,`lastSeenVisibility`,`onlineStatusVisibility`,`groupInvitePermission`,`disappearingMessagesDefault`,`screenshotNotifications`,`forwardingRestriction`,`messageNotifications`,`callNotifications`,`groupNotifications`,`soundEnabled`,`vibrationEnabled`,`ledEnabled`,`notificationSound`,`quietHoursEnabled`,`quietHoursStart`,`quietHoursEnd`,`showPreview`,`showSenderName`,`theme`,`fontSize`,`chatWallpaper`,`useSystemEmojis`,`showAvatarsInGroups`,`compactMode`,`highContrast`,`largeText`,`reduceMotion`,`screenReaderOptimized`,`hapticFeedback`,`voiceOverEnabled`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserSettingsEntity entity) {
        statement.bindString(1, entity.getUserId());
        statement.bindString(2, entity.getDisplayName());
        if (entity.getBio() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getBio());
        }
        if (entity.getAvatar() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getAvatar());
        }
        if (entity.getPhoneNumber() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getPhoneNumber());
        }
        if (entity.getEmail() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getEmail());
        }
        final int _tmp = entity.getShowOnlineStatus() ? 1 : 0;
        statement.bindLong(7, _tmp);
        final int _tmp_1 = entity.getShowLastSeen() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        final int _tmp_2 = entity.getReadReceipts() ? 1 : 0;
        statement.bindLong(9, _tmp_2);
        final int _tmp_3 = entity.getTypingIndicators() ? 1 : 0;
        statement.bindLong(10, _tmp_3);
        statement.bindString(11, entity.getProfilePhotoVisibility());
        statement.bindString(12, entity.getLastSeenVisibility());
        statement.bindString(13, entity.getOnlineStatusVisibility());
        statement.bindString(14, entity.getGroupInvitePermission());
        statement.bindString(15, entity.getDisappearingMessagesDefault());
        final int _tmp_4 = entity.getScreenshotNotifications() ? 1 : 0;
        statement.bindLong(16, _tmp_4);
        final int _tmp_5 = entity.getForwardingRestriction() ? 1 : 0;
        statement.bindLong(17, _tmp_5);
        final int _tmp_6 = entity.getMessageNotifications() ? 1 : 0;
        statement.bindLong(18, _tmp_6);
        final int _tmp_7 = entity.getCallNotifications() ? 1 : 0;
        statement.bindLong(19, _tmp_7);
        final int _tmp_8 = entity.getGroupNotifications() ? 1 : 0;
        statement.bindLong(20, _tmp_8);
        final int _tmp_9 = entity.getSoundEnabled() ? 1 : 0;
        statement.bindLong(21, _tmp_9);
        final int _tmp_10 = entity.getVibrationEnabled() ? 1 : 0;
        statement.bindLong(22, _tmp_10);
        final int _tmp_11 = entity.getLedEnabled() ? 1 : 0;
        statement.bindLong(23, _tmp_11);
        statement.bindString(24, entity.getNotificationSound());
        final int _tmp_12 = entity.getQuietHoursEnabled() ? 1 : 0;
        statement.bindLong(25, _tmp_12);
        statement.bindString(26, entity.getQuietHoursStart());
        statement.bindString(27, entity.getQuietHoursEnd());
        final int _tmp_13 = entity.getShowPreview() ? 1 : 0;
        statement.bindLong(28, _tmp_13);
        final int _tmp_14 = entity.getShowSenderName() ? 1 : 0;
        statement.bindLong(29, _tmp_14);
        statement.bindString(30, entity.getTheme());
        statement.bindString(31, entity.getFontSize());
        if (entity.getChatWallpaper() == null) {
          statement.bindNull(32);
        } else {
          statement.bindString(32, entity.getChatWallpaper());
        }
        final int _tmp_15 = entity.getUseSystemEmojis() ? 1 : 0;
        statement.bindLong(33, _tmp_15);
        final int _tmp_16 = entity.getShowAvatarsInGroups() ? 1 : 0;
        statement.bindLong(34, _tmp_16);
        final int _tmp_17 = entity.getCompactMode() ? 1 : 0;
        statement.bindLong(35, _tmp_17);
        final int _tmp_18 = entity.getHighContrast() ? 1 : 0;
        statement.bindLong(36, _tmp_18);
        final int _tmp_19 = entity.getLargeText() ? 1 : 0;
        statement.bindLong(37, _tmp_19);
        final int _tmp_20 = entity.getReduceMotion() ? 1 : 0;
        statement.bindLong(38, _tmp_20);
        final int _tmp_21 = entity.getScreenReaderOptimized() ? 1 : 0;
        statement.bindLong(39, _tmp_21);
        final int _tmp_22 = entity.getHapticFeedback() ? 1 : 0;
        statement.bindLong(40, _tmp_22);
        final int _tmp_23 = entity.getVoiceOverEnabled() ? 1 : 0;
        statement.bindLong(41, _tmp_23);
        statement.bindLong(42, entity.getUpdatedAt());
      }
    };
    this.__preparedStmtOfUpdateProfile = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE user_settings \n"
                + "        SET displayName = ?, \n"
                + "            bio = ?, \n"
                + "            avatar = ?, \n"
                + "            phoneNumber = ?, \n"
                + "            email = ?,\n"
                + "            showOnlineStatus = ?,\n"
                + "            showLastSeen = ?,\n"
                + "            updatedAt = ?\n"
                + "        WHERE userId = ?\n"
                + "    ";
        return _query;
      }
    };
    this.__preparedStmtOfUpdatePrivacySettings = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE user_settings \n"
                + "        SET readReceipts = ?,\n"
                + "            typingIndicators = ?,\n"
                + "            profilePhotoVisibility = ?,\n"
                + "            lastSeenVisibility = ?,\n"
                + "            onlineStatusVisibility = ?,\n"
                + "            groupInvitePermission = ?,\n"
                + "            disappearingMessagesDefault = ?,\n"
                + "            screenshotNotifications = ?,\n"
                + "            forwardingRestriction = ?,\n"
                + "            updatedAt = ?\n"
                + "        WHERE userId = ?\n"
                + "    ";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateNotificationSettings = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE user_settings \n"
                + "        SET messageNotifications = ?,\n"
                + "            callNotifications = ?,\n"
                + "            groupNotifications = ?,\n"
                + "            soundEnabled = ?,\n"
                + "            vibrationEnabled = ?,\n"
                + "            ledEnabled = ?,\n"
                + "            notificationSound = ?,\n"
                + "            quietHoursEnabled = ?,\n"
                + "            quietHoursStart = ?,\n"
                + "            quietHoursEnd = ?,\n"
                + "            showPreview = ?,\n"
                + "            showSenderName = ?,\n"
                + "            updatedAt = ?\n"
                + "        WHERE userId = ?\n"
                + "    ";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateAppearanceSettings = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE user_settings \n"
                + "        SET theme = ?,\n"
                + "            fontSize = ?,\n"
                + "            chatWallpaper = ?,\n"
                + "            useSystemEmojis = ?,\n"
                + "            showAvatarsInGroups = ?,\n"
                + "            compactMode = ?,\n"
                + "            updatedAt = ?\n"
                + "        WHERE userId = ?\n"
                + "    ";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateAccessibilitySettings = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE user_settings \n"
                + "        SET highContrast = ?,\n"
                + "            largeText = ?,\n"
                + "            reduceMotion = ?,\n"
                + "            screenReaderOptimized = ?,\n"
                + "            hapticFeedback = ?,\n"
                + "            voiceOverEnabled = ?,\n"
                + "            updatedAt = ?\n"
                + "        WHERE userId = ?\n"
                + "    ";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteSettings = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM user_settings WHERE userId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertOrUpdateSettings(final UserSettingsEntity settings,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUserSettingsEntity.insert(settings);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateProfile(final String userId, final String displayName, final String bio,
      final String avatar, final String phoneNumber, final String email,
      final boolean showOnlineStatus, final boolean showLastSeen, final long updatedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateProfile.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, displayName);
        _argIndex = 2;
        if (bio == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, bio);
        }
        _argIndex = 3;
        if (avatar == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, avatar);
        }
        _argIndex = 4;
        if (phoneNumber == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, phoneNumber);
        }
        _argIndex = 5;
        if (email == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, email);
        }
        _argIndex = 6;
        final int _tmp = showOnlineStatus ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 7;
        final int _tmp_1 = showLastSeen ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_1);
        _argIndex = 8;
        _stmt.bindLong(_argIndex, updatedAt);
        _argIndex = 9;
        _stmt.bindString(_argIndex, userId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateProfile.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updatePrivacySettings(final String userId, final boolean readReceipts,
      final boolean typingIndicators, final String profilePhotoVisibility,
      final String lastSeenVisibility, final String onlineStatusVisibility,
      final String groupInvitePermission, final String disappearingMessagesDefault,
      final boolean screenshotNotifications, final boolean forwardingRestriction,
      final long updatedAt, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdatePrivacySettings.acquire();
        int _argIndex = 1;
        final int _tmp = readReceipts ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        final int _tmp_1 = typingIndicators ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_1);
        _argIndex = 3;
        _stmt.bindString(_argIndex, profilePhotoVisibility);
        _argIndex = 4;
        _stmt.bindString(_argIndex, lastSeenVisibility);
        _argIndex = 5;
        _stmt.bindString(_argIndex, onlineStatusVisibility);
        _argIndex = 6;
        _stmt.bindString(_argIndex, groupInvitePermission);
        _argIndex = 7;
        _stmt.bindString(_argIndex, disappearingMessagesDefault);
        _argIndex = 8;
        final int _tmp_2 = screenshotNotifications ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_2);
        _argIndex = 9;
        final int _tmp_3 = forwardingRestriction ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_3);
        _argIndex = 10;
        _stmt.bindLong(_argIndex, updatedAt);
        _argIndex = 11;
        _stmt.bindString(_argIndex, userId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdatePrivacySettings.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateNotificationSettings(final String userId, final boolean messageNotifications,
      final boolean callNotifications, final boolean groupNotifications, final boolean soundEnabled,
      final boolean vibrationEnabled, final boolean ledEnabled, final String notificationSound,
      final boolean quietHoursEnabled, final String quietHoursStart, final String quietHoursEnd,
      final boolean showPreview, final boolean showSenderName, final long updatedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateNotificationSettings.acquire();
        int _argIndex = 1;
        final int _tmp = messageNotifications ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        final int _tmp_1 = callNotifications ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_1);
        _argIndex = 3;
        final int _tmp_2 = groupNotifications ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_2);
        _argIndex = 4;
        final int _tmp_3 = soundEnabled ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_3);
        _argIndex = 5;
        final int _tmp_4 = vibrationEnabled ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_4);
        _argIndex = 6;
        final int _tmp_5 = ledEnabled ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_5);
        _argIndex = 7;
        _stmt.bindString(_argIndex, notificationSound);
        _argIndex = 8;
        final int _tmp_6 = quietHoursEnabled ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_6);
        _argIndex = 9;
        _stmt.bindString(_argIndex, quietHoursStart);
        _argIndex = 10;
        _stmt.bindString(_argIndex, quietHoursEnd);
        _argIndex = 11;
        final int _tmp_7 = showPreview ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_7);
        _argIndex = 12;
        final int _tmp_8 = showSenderName ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_8);
        _argIndex = 13;
        _stmt.bindLong(_argIndex, updatedAt);
        _argIndex = 14;
        _stmt.bindString(_argIndex, userId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateNotificationSettings.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateAppearanceSettings(final String userId, final String theme,
      final String fontSize, final String chatWallpaper, final boolean useSystemEmojis,
      final boolean showAvatarsInGroups, final boolean compactMode, final long updatedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateAppearanceSettings.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, theme);
        _argIndex = 2;
        _stmt.bindString(_argIndex, fontSize);
        _argIndex = 3;
        if (chatWallpaper == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, chatWallpaper);
        }
        _argIndex = 4;
        final int _tmp = useSystemEmojis ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 5;
        final int _tmp_1 = showAvatarsInGroups ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_1);
        _argIndex = 6;
        final int _tmp_2 = compactMode ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_2);
        _argIndex = 7;
        _stmt.bindLong(_argIndex, updatedAt);
        _argIndex = 8;
        _stmt.bindString(_argIndex, userId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateAppearanceSettings.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateAccessibilitySettings(final String userId, final boolean highContrast,
      final boolean largeText, final boolean reduceMotion, final boolean screenReaderOptimized,
      final boolean hapticFeedback, final boolean voiceOverEnabled, final long updatedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateAccessibilitySettings.acquire();
        int _argIndex = 1;
        final int _tmp = highContrast ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        final int _tmp_1 = largeText ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_1);
        _argIndex = 3;
        final int _tmp_2 = reduceMotion ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_2);
        _argIndex = 4;
        final int _tmp_3 = screenReaderOptimized ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_3);
        _argIndex = 5;
        final int _tmp_4 = hapticFeedback ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_4);
        _argIndex = 6;
        final int _tmp_5 = voiceOverEnabled ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_5);
        _argIndex = 7;
        _stmt.bindLong(_argIndex, updatedAt);
        _argIndex = 8;
        _stmt.bindString(_argIndex, userId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateAccessibilitySettings.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSettings(final String userId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSettings.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, userId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteSettings.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getSettings(final String userId,
      final Continuation<? super UserSettingsEntity> $completion) {
    final String _sql = "SELECT * FROM user_settings WHERE userId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<UserSettingsEntity>() {
      @Override
      @Nullable
      public UserSettingsEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfBio = CursorUtil.getColumnIndexOrThrow(_cursor, "bio");
          final int _cursorIndexOfAvatar = CursorUtil.getColumnIndexOrThrow(_cursor, "avatar");
          final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfShowOnlineStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "showOnlineStatus");
          final int _cursorIndexOfShowLastSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "showLastSeen");
          final int _cursorIndexOfReadReceipts = CursorUtil.getColumnIndexOrThrow(_cursor, "readReceipts");
          final int _cursorIndexOfTypingIndicators = CursorUtil.getColumnIndexOrThrow(_cursor, "typingIndicators");
          final int _cursorIndexOfProfilePhotoVisibility = CursorUtil.getColumnIndexOrThrow(_cursor, "profilePhotoVisibility");
          final int _cursorIndexOfLastSeenVisibility = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSeenVisibility");
          final int _cursorIndexOfOnlineStatusVisibility = CursorUtil.getColumnIndexOrThrow(_cursor, "onlineStatusVisibility");
          final int _cursorIndexOfGroupInvitePermission = CursorUtil.getColumnIndexOrThrow(_cursor, "groupInvitePermission");
          final int _cursorIndexOfDisappearingMessagesDefault = CursorUtil.getColumnIndexOrThrow(_cursor, "disappearingMessagesDefault");
          final int _cursorIndexOfScreenshotNotifications = CursorUtil.getColumnIndexOrThrow(_cursor, "screenshotNotifications");
          final int _cursorIndexOfForwardingRestriction = CursorUtil.getColumnIndexOrThrow(_cursor, "forwardingRestriction");
          final int _cursorIndexOfMessageNotifications = CursorUtil.getColumnIndexOrThrow(_cursor, "messageNotifications");
          final int _cursorIndexOfCallNotifications = CursorUtil.getColumnIndexOrThrow(_cursor, "callNotifications");
          final int _cursorIndexOfGroupNotifications = CursorUtil.getColumnIndexOrThrow(_cursor, "groupNotifications");
          final int _cursorIndexOfSoundEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "soundEnabled");
          final int _cursorIndexOfVibrationEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "vibrationEnabled");
          final int _cursorIndexOfLedEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "ledEnabled");
          final int _cursorIndexOfNotificationSound = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationSound");
          final int _cursorIndexOfQuietHoursEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "quietHoursEnabled");
          final int _cursorIndexOfQuietHoursStart = CursorUtil.getColumnIndexOrThrow(_cursor, "quietHoursStart");
          final int _cursorIndexOfQuietHoursEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "quietHoursEnd");
          final int _cursorIndexOfShowPreview = CursorUtil.getColumnIndexOrThrow(_cursor, "showPreview");
          final int _cursorIndexOfShowSenderName = CursorUtil.getColumnIndexOrThrow(_cursor, "showSenderName");
          final int _cursorIndexOfTheme = CursorUtil.getColumnIndexOrThrow(_cursor, "theme");
          final int _cursorIndexOfFontSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fontSize");
          final int _cursorIndexOfChatWallpaper = CursorUtil.getColumnIndexOrThrow(_cursor, "chatWallpaper");
          final int _cursorIndexOfUseSystemEmojis = CursorUtil.getColumnIndexOrThrow(_cursor, "useSystemEmojis");
          final int _cursorIndexOfShowAvatarsInGroups = CursorUtil.getColumnIndexOrThrow(_cursor, "showAvatarsInGroups");
          final int _cursorIndexOfCompactMode = CursorUtil.getColumnIndexOrThrow(_cursor, "compactMode");
          final int _cursorIndexOfHighContrast = CursorUtil.getColumnIndexOrThrow(_cursor, "highContrast");
          final int _cursorIndexOfLargeText = CursorUtil.getColumnIndexOrThrow(_cursor, "largeText");
          final int _cursorIndexOfReduceMotion = CursorUtil.getColumnIndexOrThrow(_cursor, "reduceMotion");
          final int _cursorIndexOfScreenReaderOptimized = CursorUtil.getColumnIndexOrThrow(_cursor, "screenReaderOptimized");
          final int _cursorIndexOfHapticFeedback = CursorUtil.getColumnIndexOrThrow(_cursor, "hapticFeedback");
          final int _cursorIndexOfVoiceOverEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "voiceOverEnabled");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final UserSettingsEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final String _tmpBio;
            if (_cursor.isNull(_cursorIndexOfBio)) {
              _tmpBio = null;
            } else {
              _tmpBio = _cursor.getString(_cursorIndexOfBio);
            }
            final String _tmpAvatar;
            if (_cursor.isNull(_cursorIndexOfAvatar)) {
              _tmpAvatar = null;
            } else {
              _tmpAvatar = _cursor.getString(_cursorIndexOfAvatar);
            }
            final String _tmpPhoneNumber;
            if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
              _tmpPhoneNumber = null;
            } else {
              _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final boolean _tmpShowOnlineStatus;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfShowOnlineStatus);
            _tmpShowOnlineStatus = _tmp != 0;
            final boolean _tmpShowLastSeen;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfShowLastSeen);
            _tmpShowLastSeen = _tmp_1 != 0;
            final boolean _tmpReadReceipts;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfReadReceipts);
            _tmpReadReceipts = _tmp_2 != 0;
            final boolean _tmpTypingIndicators;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfTypingIndicators);
            _tmpTypingIndicators = _tmp_3 != 0;
            final String _tmpProfilePhotoVisibility;
            _tmpProfilePhotoVisibility = _cursor.getString(_cursorIndexOfProfilePhotoVisibility);
            final String _tmpLastSeenVisibility;
            _tmpLastSeenVisibility = _cursor.getString(_cursorIndexOfLastSeenVisibility);
            final String _tmpOnlineStatusVisibility;
            _tmpOnlineStatusVisibility = _cursor.getString(_cursorIndexOfOnlineStatusVisibility);
            final String _tmpGroupInvitePermission;
            _tmpGroupInvitePermission = _cursor.getString(_cursorIndexOfGroupInvitePermission);
            final String _tmpDisappearingMessagesDefault;
            _tmpDisappearingMessagesDefault = _cursor.getString(_cursorIndexOfDisappearingMessagesDefault);
            final boolean _tmpScreenshotNotifications;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfScreenshotNotifications);
            _tmpScreenshotNotifications = _tmp_4 != 0;
            final boolean _tmpForwardingRestriction;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfForwardingRestriction);
            _tmpForwardingRestriction = _tmp_5 != 0;
            final boolean _tmpMessageNotifications;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfMessageNotifications);
            _tmpMessageNotifications = _tmp_6 != 0;
            final boolean _tmpCallNotifications;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfCallNotifications);
            _tmpCallNotifications = _tmp_7 != 0;
            final boolean _tmpGroupNotifications;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfGroupNotifications);
            _tmpGroupNotifications = _tmp_8 != 0;
            final boolean _tmpSoundEnabled;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfSoundEnabled);
            _tmpSoundEnabled = _tmp_9 != 0;
            final boolean _tmpVibrationEnabled;
            final int _tmp_10;
            _tmp_10 = _cursor.getInt(_cursorIndexOfVibrationEnabled);
            _tmpVibrationEnabled = _tmp_10 != 0;
            final boolean _tmpLedEnabled;
            final int _tmp_11;
            _tmp_11 = _cursor.getInt(_cursorIndexOfLedEnabled);
            _tmpLedEnabled = _tmp_11 != 0;
            final String _tmpNotificationSound;
            _tmpNotificationSound = _cursor.getString(_cursorIndexOfNotificationSound);
            final boolean _tmpQuietHoursEnabled;
            final int _tmp_12;
            _tmp_12 = _cursor.getInt(_cursorIndexOfQuietHoursEnabled);
            _tmpQuietHoursEnabled = _tmp_12 != 0;
            final String _tmpQuietHoursStart;
            _tmpQuietHoursStart = _cursor.getString(_cursorIndexOfQuietHoursStart);
            final String _tmpQuietHoursEnd;
            _tmpQuietHoursEnd = _cursor.getString(_cursorIndexOfQuietHoursEnd);
            final boolean _tmpShowPreview;
            final int _tmp_13;
            _tmp_13 = _cursor.getInt(_cursorIndexOfShowPreview);
            _tmpShowPreview = _tmp_13 != 0;
            final boolean _tmpShowSenderName;
            final int _tmp_14;
            _tmp_14 = _cursor.getInt(_cursorIndexOfShowSenderName);
            _tmpShowSenderName = _tmp_14 != 0;
            final String _tmpTheme;
            _tmpTheme = _cursor.getString(_cursorIndexOfTheme);
            final String _tmpFontSize;
            _tmpFontSize = _cursor.getString(_cursorIndexOfFontSize);
            final String _tmpChatWallpaper;
            if (_cursor.isNull(_cursorIndexOfChatWallpaper)) {
              _tmpChatWallpaper = null;
            } else {
              _tmpChatWallpaper = _cursor.getString(_cursorIndexOfChatWallpaper);
            }
            final boolean _tmpUseSystemEmojis;
            final int _tmp_15;
            _tmp_15 = _cursor.getInt(_cursorIndexOfUseSystemEmojis);
            _tmpUseSystemEmojis = _tmp_15 != 0;
            final boolean _tmpShowAvatarsInGroups;
            final int _tmp_16;
            _tmp_16 = _cursor.getInt(_cursorIndexOfShowAvatarsInGroups);
            _tmpShowAvatarsInGroups = _tmp_16 != 0;
            final boolean _tmpCompactMode;
            final int _tmp_17;
            _tmp_17 = _cursor.getInt(_cursorIndexOfCompactMode);
            _tmpCompactMode = _tmp_17 != 0;
            final boolean _tmpHighContrast;
            final int _tmp_18;
            _tmp_18 = _cursor.getInt(_cursorIndexOfHighContrast);
            _tmpHighContrast = _tmp_18 != 0;
            final boolean _tmpLargeText;
            final int _tmp_19;
            _tmp_19 = _cursor.getInt(_cursorIndexOfLargeText);
            _tmpLargeText = _tmp_19 != 0;
            final boolean _tmpReduceMotion;
            final int _tmp_20;
            _tmp_20 = _cursor.getInt(_cursorIndexOfReduceMotion);
            _tmpReduceMotion = _tmp_20 != 0;
            final boolean _tmpScreenReaderOptimized;
            final int _tmp_21;
            _tmp_21 = _cursor.getInt(_cursorIndexOfScreenReaderOptimized);
            _tmpScreenReaderOptimized = _tmp_21 != 0;
            final boolean _tmpHapticFeedback;
            final int _tmp_22;
            _tmp_22 = _cursor.getInt(_cursorIndexOfHapticFeedback);
            _tmpHapticFeedback = _tmp_22 != 0;
            final boolean _tmpVoiceOverEnabled;
            final int _tmp_23;
            _tmp_23 = _cursor.getInt(_cursorIndexOfVoiceOverEnabled);
            _tmpVoiceOverEnabled = _tmp_23 != 0;
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new UserSettingsEntity(_tmpUserId,_tmpDisplayName,_tmpBio,_tmpAvatar,_tmpPhoneNumber,_tmpEmail,_tmpShowOnlineStatus,_tmpShowLastSeen,_tmpReadReceipts,_tmpTypingIndicators,_tmpProfilePhotoVisibility,_tmpLastSeenVisibility,_tmpOnlineStatusVisibility,_tmpGroupInvitePermission,_tmpDisappearingMessagesDefault,_tmpScreenshotNotifications,_tmpForwardingRestriction,_tmpMessageNotifications,_tmpCallNotifications,_tmpGroupNotifications,_tmpSoundEnabled,_tmpVibrationEnabled,_tmpLedEnabled,_tmpNotificationSound,_tmpQuietHoursEnabled,_tmpQuietHoursStart,_tmpQuietHoursEnd,_tmpShowPreview,_tmpShowSenderName,_tmpTheme,_tmpFontSize,_tmpChatWallpaper,_tmpUseSystemEmojis,_tmpShowAvatarsInGroups,_tmpCompactMode,_tmpHighContrast,_tmpLargeText,_tmpReduceMotion,_tmpScreenReaderOptimized,_tmpHapticFeedback,_tmpVoiceOverEnabled,_tmpUpdatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<UserSettingsEntity> getSettingsFlow(final String userId) {
    final String _sql = "SELECT * FROM user_settings WHERE userId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"user_settings"}, new Callable<UserSettingsEntity>() {
      @Override
      @Nullable
      public UserSettingsEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfBio = CursorUtil.getColumnIndexOrThrow(_cursor, "bio");
          final int _cursorIndexOfAvatar = CursorUtil.getColumnIndexOrThrow(_cursor, "avatar");
          final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfShowOnlineStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "showOnlineStatus");
          final int _cursorIndexOfShowLastSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "showLastSeen");
          final int _cursorIndexOfReadReceipts = CursorUtil.getColumnIndexOrThrow(_cursor, "readReceipts");
          final int _cursorIndexOfTypingIndicators = CursorUtil.getColumnIndexOrThrow(_cursor, "typingIndicators");
          final int _cursorIndexOfProfilePhotoVisibility = CursorUtil.getColumnIndexOrThrow(_cursor, "profilePhotoVisibility");
          final int _cursorIndexOfLastSeenVisibility = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSeenVisibility");
          final int _cursorIndexOfOnlineStatusVisibility = CursorUtil.getColumnIndexOrThrow(_cursor, "onlineStatusVisibility");
          final int _cursorIndexOfGroupInvitePermission = CursorUtil.getColumnIndexOrThrow(_cursor, "groupInvitePermission");
          final int _cursorIndexOfDisappearingMessagesDefault = CursorUtil.getColumnIndexOrThrow(_cursor, "disappearingMessagesDefault");
          final int _cursorIndexOfScreenshotNotifications = CursorUtil.getColumnIndexOrThrow(_cursor, "screenshotNotifications");
          final int _cursorIndexOfForwardingRestriction = CursorUtil.getColumnIndexOrThrow(_cursor, "forwardingRestriction");
          final int _cursorIndexOfMessageNotifications = CursorUtil.getColumnIndexOrThrow(_cursor, "messageNotifications");
          final int _cursorIndexOfCallNotifications = CursorUtil.getColumnIndexOrThrow(_cursor, "callNotifications");
          final int _cursorIndexOfGroupNotifications = CursorUtil.getColumnIndexOrThrow(_cursor, "groupNotifications");
          final int _cursorIndexOfSoundEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "soundEnabled");
          final int _cursorIndexOfVibrationEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "vibrationEnabled");
          final int _cursorIndexOfLedEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "ledEnabled");
          final int _cursorIndexOfNotificationSound = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationSound");
          final int _cursorIndexOfQuietHoursEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "quietHoursEnabled");
          final int _cursorIndexOfQuietHoursStart = CursorUtil.getColumnIndexOrThrow(_cursor, "quietHoursStart");
          final int _cursorIndexOfQuietHoursEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "quietHoursEnd");
          final int _cursorIndexOfShowPreview = CursorUtil.getColumnIndexOrThrow(_cursor, "showPreview");
          final int _cursorIndexOfShowSenderName = CursorUtil.getColumnIndexOrThrow(_cursor, "showSenderName");
          final int _cursorIndexOfTheme = CursorUtil.getColumnIndexOrThrow(_cursor, "theme");
          final int _cursorIndexOfFontSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fontSize");
          final int _cursorIndexOfChatWallpaper = CursorUtil.getColumnIndexOrThrow(_cursor, "chatWallpaper");
          final int _cursorIndexOfUseSystemEmojis = CursorUtil.getColumnIndexOrThrow(_cursor, "useSystemEmojis");
          final int _cursorIndexOfShowAvatarsInGroups = CursorUtil.getColumnIndexOrThrow(_cursor, "showAvatarsInGroups");
          final int _cursorIndexOfCompactMode = CursorUtil.getColumnIndexOrThrow(_cursor, "compactMode");
          final int _cursorIndexOfHighContrast = CursorUtil.getColumnIndexOrThrow(_cursor, "highContrast");
          final int _cursorIndexOfLargeText = CursorUtil.getColumnIndexOrThrow(_cursor, "largeText");
          final int _cursorIndexOfReduceMotion = CursorUtil.getColumnIndexOrThrow(_cursor, "reduceMotion");
          final int _cursorIndexOfScreenReaderOptimized = CursorUtil.getColumnIndexOrThrow(_cursor, "screenReaderOptimized");
          final int _cursorIndexOfHapticFeedback = CursorUtil.getColumnIndexOrThrow(_cursor, "hapticFeedback");
          final int _cursorIndexOfVoiceOverEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "voiceOverEnabled");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final UserSettingsEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final String _tmpBio;
            if (_cursor.isNull(_cursorIndexOfBio)) {
              _tmpBio = null;
            } else {
              _tmpBio = _cursor.getString(_cursorIndexOfBio);
            }
            final String _tmpAvatar;
            if (_cursor.isNull(_cursorIndexOfAvatar)) {
              _tmpAvatar = null;
            } else {
              _tmpAvatar = _cursor.getString(_cursorIndexOfAvatar);
            }
            final String _tmpPhoneNumber;
            if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
              _tmpPhoneNumber = null;
            } else {
              _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final boolean _tmpShowOnlineStatus;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfShowOnlineStatus);
            _tmpShowOnlineStatus = _tmp != 0;
            final boolean _tmpShowLastSeen;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfShowLastSeen);
            _tmpShowLastSeen = _tmp_1 != 0;
            final boolean _tmpReadReceipts;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfReadReceipts);
            _tmpReadReceipts = _tmp_2 != 0;
            final boolean _tmpTypingIndicators;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfTypingIndicators);
            _tmpTypingIndicators = _tmp_3 != 0;
            final String _tmpProfilePhotoVisibility;
            _tmpProfilePhotoVisibility = _cursor.getString(_cursorIndexOfProfilePhotoVisibility);
            final String _tmpLastSeenVisibility;
            _tmpLastSeenVisibility = _cursor.getString(_cursorIndexOfLastSeenVisibility);
            final String _tmpOnlineStatusVisibility;
            _tmpOnlineStatusVisibility = _cursor.getString(_cursorIndexOfOnlineStatusVisibility);
            final String _tmpGroupInvitePermission;
            _tmpGroupInvitePermission = _cursor.getString(_cursorIndexOfGroupInvitePermission);
            final String _tmpDisappearingMessagesDefault;
            _tmpDisappearingMessagesDefault = _cursor.getString(_cursorIndexOfDisappearingMessagesDefault);
            final boolean _tmpScreenshotNotifications;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfScreenshotNotifications);
            _tmpScreenshotNotifications = _tmp_4 != 0;
            final boolean _tmpForwardingRestriction;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfForwardingRestriction);
            _tmpForwardingRestriction = _tmp_5 != 0;
            final boolean _tmpMessageNotifications;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfMessageNotifications);
            _tmpMessageNotifications = _tmp_6 != 0;
            final boolean _tmpCallNotifications;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfCallNotifications);
            _tmpCallNotifications = _tmp_7 != 0;
            final boolean _tmpGroupNotifications;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfGroupNotifications);
            _tmpGroupNotifications = _tmp_8 != 0;
            final boolean _tmpSoundEnabled;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfSoundEnabled);
            _tmpSoundEnabled = _tmp_9 != 0;
            final boolean _tmpVibrationEnabled;
            final int _tmp_10;
            _tmp_10 = _cursor.getInt(_cursorIndexOfVibrationEnabled);
            _tmpVibrationEnabled = _tmp_10 != 0;
            final boolean _tmpLedEnabled;
            final int _tmp_11;
            _tmp_11 = _cursor.getInt(_cursorIndexOfLedEnabled);
            _tmpLedEnabled = _tmp_11 != 0;
            final String _tmpNotificationSound;
            _tmpNotificationSound = _cursor.getString(_cursorIndexOfNotificationSound);
            final boolean _tmpQuietHoursEnabled;
            final int _tmp_12;
            _tmp_12 = _cursor.getInt(_cursorIndexOfQuietHoursEnabled);
            _tmpQuietHoursEnabled = _tmp_12 != 0;
            final String _tmpQuietHoursStart;
            _tmpQuietHoursStart = _cursor.getString(_cursorIndexOfQuietHoursStart);
            final String _tmpQuietHoursEnd;
            _tmpQuietHoursEnd = _cursor.getString(_cursorIndexOfQuietHoursEnd);
            final boolean _tmpShowPreview;
            final int _tmp_13;
            _tmp_13 = _cursor.getInt(_cursorIndexOfShowPreview);
            _tmpShowPreview = _tmp_13 != 0;
            final boolean _tmpShowSenderName;
            final int _tmp_14;
            _tmp_14 = _cursor.getInt(_cursorIndexOfShowSenderName);
            _tmpShowSenderName = _tmp_14 != 0;
            final String _tmpTheme;
            _tmpTheme = _cursor.getString(_cursorIndexOfTheme);
            final String _tmpFontSize;
            _tmpFontSize = _cursor.getString(_cursorIndexOfFontSize);
            final String _tmpChatWallpaper;
            if (_cursor.isNull(_cursorIndexOfChatWallpaper)) {
              _tmpChatWallpaper = null;
            } else {
              _tmpChatWallpaper = _cursor.getString(_cursorIndexOfChatWallpaper);
            }
            final boolean _tmpUseSystemEmojis;
            final int _tmp_15;
            _tmp_15 = _cursor.getInt(_cursorIndexOfUseSystemEmojis);
            _tmpUseSystemEmojis = _tmp_15 != 0;
            final boolean _tmpShowAvatarsInGroups;
            final int _tmp_16;
            _tmp_16 = _cursor.getInt(_cursorIndexOfShowAvatarsInGroups);
            _tmpShowAvatarsInGroups = _tmp_16 != 0;
            final boolean _tmpCompactMode;
            final int _tmp_17;
            _tmp_17 = _cursor.getInt(_cursorIndexOfCompactMode);
            _tmpCompactMode = _tmp_17 != 0;
            final boolean _tmpHighContrast;
            final int _tmp_18;
            _tmp_18 = _cursor.getInt(_cursorIndexOfHighContrast);
            _tmpHighContrast = _tmp_18 != 0;
            final boolean _tmpLargeText;
            final int _tmp_19;
            _tmp_19 = _cursor.getInt(_cursorIndexOfLargeText);
            _tmpLargeText = _tmp_19 != 0;
            final boolean _tmpReduceMotion;
            final int _tmp_20;
            _tmp_20 = _cursor.getInt(_cursorIndexOfReduceMotion);
            _tmpReduceMotion = _tmp_20 != 0;
            final boolean _tmpScreenReaderOptimized;
            final int _tmp_21;
            _tmp_21 = _cursor.getInt(_cursorIndexOfScreenReaderOptimized);
            _tmpScreenReaderOptimized = _tmp_21 != 0;
            final boolean _tmpHapticFeedback;
            final int _tmp_22;
            _tmp_22 = _cursor.getInt(_cursorIndexOfHapticFeedback);
            _tmpHapticFeedback = _tmp_22 != 0;
            final boolean _tmpVoiceOverEnabled;
            final int _tmp_23;
            _tmp_23 = _cursor.getInt(_cursorIndexOfVoiceOverEnabled);
            _tmpVoiceOverEnabled = _tmp_23 != 0;
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new UserSettingsEntity(_tmpUserId,_tmpDisplayName,_tmpBio,_tmpAvatar,_tmpPhoneNumber,_tmpEmail,_tmpShowOnlineStatus,_tmpShowLastSeen,_tmpReadReceipts,_tmpTypingIndicators,_tmpProfilePhotoVisibility,_tmpLastSeenVisibility,_tmpOnlineStatusVisibility,_tmpGroupInvitePermission,_tmpDisappearingMessagesDefault,_tmpScreenshotNotifications,_tmpForwardingRestriction,_tmpMessageNotifications,_tmpCallNotifications,_tmpGroupNotifications,_tmpSoundEnabled,_tmpVibrationEnabled,_tmpLedEnabled,_tmpNotificationSound,_tmpQuietHoursEnabled,_tmpQuietHoursStart,_tmpQuietHoursEnd,_tmpShowPreview,_tmpShowSenderName,_tmpTheme,_tmpFontSize,_tmpChatWallpaper,_tmpUseSystemEmojis,_tmpShowAvatarsInGroups,_tmpCompactMode,_tmpHighContrast,_tmpLargeText,_tmpReduceMotion,_tmpScreenReaderOptimized,_tmpHapticFeedback,_tmpVoiceOverEnabled,_tmpUpdatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object settingsExist(final String userId,
      final Continuation<? super Boolean> $completion) {
    final String _sql = "SELECT COUNT(*) > 0 FROM user_settings WHERE userId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Boolean>() {
      @Override
      @NonNull
      public Boolean call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Boolean _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp != 0;
          } else {
            _result = false;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
