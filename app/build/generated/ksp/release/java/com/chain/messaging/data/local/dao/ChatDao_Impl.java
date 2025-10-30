package com.chain.messaging.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.chain.messaging.data.local.entity.ChatEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ChatDao_Impl implements ChatDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ChatEntity> __insertionAdapterOfChatEntity;

  private final EntityDeletionOrUpdateAdapter<ChatEntity> __deletionAdapterOfChatEntity;

  private final EntityDeletionOrUpdateAdapter<ChatEntity> __updateAdapterOfChatEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateUnreadCount;

  private final SharedSQLiteStatement __preparedStmtOfUpdateLastActivity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteChatById;

  public ChatDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfChatEntity = new EntityInsertionAdapter<ChatEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `chats` (`id`,`type`,`name`,`participants`,`admins`,`isNotificationsEnabled`,`disappearingMessagesTimer`,`isArchived`,`isPinned`,`isMuted`,`unreadCount`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChatEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getType());
        statement.bindString(3, entity.getName());
        statement.bindString(4, entity.getParticipants());
        statement.bindString(5, entity.getAdmins());
        final int _tmp = entity.isNotificationsEnabled() ? 1 : 0;
        statement.bindLong(6, _tmp);
        if (entity.getDisappearingMessagesTimer() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getDisappearingMessagesTimer());
        }
        final int _tmp_1 = entity.isArchived() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        final int _tmp_2 = entity.isPinned() ? 1 : 0;
        statement.bindLong(9, _tmp_2);
        final int _tmp_3 = entity.isMuted() ? 1 : 0;
        statement.bindLong(10, _tmp_3);
        statement.bindLong(11, entity.getUnreadCount());
        statement.bindLong(12, entity.getCreatedAt());
        statement.bindLong(13, entity.getUpdatedAt());
      }
    };
    this.__deletionAdapterOfChatEntity = new EntityDeletionOrUpdateAdapter<ChatEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `chats` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChatEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__updateAdapterOfChatEntity = new EntityDeletionOrUpdateAdapter<ChatEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `chats` SET `id` = ?,`type` = ?,`name` = ?,`participants` = ?,`admins` = ?,`isNotificationsEnabled` = ?,`disappearingMessagesTimer` = ?,`isArchived` = ?,`isPinned` = ?,`isMuted` = ?,`unreadCount` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChatEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getType());
        statement.bindString(3, entity.getName());
        statement.bindString(4, entity.getParticipants());
        statement.bindString(5, entity.getAdmins());
        final int _tmp = entity.isNotificationsEnabled() ? 1 : 0;
        statement.bindLong(6, _tmp);
        if (entity.getDisappearingMessagesTimer() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getDisappearingMessagesTimer());
        }
        final int _tmp_1 = entity.isArchived() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        final int _tmp_2 = entity.isPinned() ? 1 : 0;
        statement.bindLong(9, _tmp_2);
        final int _tmp_3 = entity.isMuted() ? 1 : 0;
        statement.bindLong(10, _tmp_3);
        statement.bindLong(11, entity.getUnreadCount());
        statement.bindLong(12, entity.getCreatedAt());
        statement.bindLong(13, entity.getUpdatedAt());
        statement.bindString(14, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateUnreadCount = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE chats SET unreadCount = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateLastActivity = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE chats SET updatedAt = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteChatById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM chats WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertChat(final ChatEntity chat, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfChatEntity.insert(chat);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertChats(final List<ChatEntity> chats,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfChatEntity.insert(chats);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteChat(final ChatEntity chat, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfChatEntity.handle(chat);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateChat(final ChatEntity chat, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfChatEntity.handle(chat);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateUnreadCount(final String chatId, final int count,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateUnreadCount.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, count);
        _argIndex = 2;
        _stmt.bindString(_argIndex, chatId);
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
          __preparedStmtOfUpdateUnreadCount.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateLastActivity(final String chatId, final long timestamp,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateLastActivity.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, timestamp);
        _argIndex = 2;
        _stmt.bindString(_argIndex, chatId);
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
          __preparedStmtOfUpdateLastActivity.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteChatById(final String chatId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteChatById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, chatId);
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
          __preparedStmtOfDeleteChatById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllChats(final Continuation<? super List<ChatEntity>> $completion) {
    final String _sql = "SELECT * FROM chats ORDER BY updatedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ChatEntity>>() {
      @Override
      @NonNull
      public List<ChatEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfParticipants = CursorUtil.getColumnIndexOrThrow(_cursor, "participants");
          final int _cursorIndexOfAdmins = CursorUtil.getColumnIndexOrThrow(_cursor, "admins");
          final int _cursorIndexOfIsNotificationsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isNotificationsEnabled");
          final int _cursorIndexOfDisappearingMessagesTimer = CursorUtil.getColumnIndexOrThrow(_cursor, "disappearingMessagesTimer");
          final int _cursorIndexOfIsArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "isArchived");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfIsMuted = CursorUtil.getColumnIndexOrThrow(_cursor, "isMuted");
          final int _cursorIndexOfUnreadCount = CursorUtil.getColumnIndexOrThrow(_cursor, "unreadCount");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<ChatEntity> _result = new ArrayList<ChatEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChatEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpParticipants;
            _tmpParticipants = _cursor.getString(_cursorIndexOfParticipants);
            final String _tmpAdmins;
            _tmpAdmins = _cursor.getString(_cursorIndexOfAdmins);
            final boolean _tmpIsNotificationsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsNotificationsEnabled);
            _tmpIsNotificationsEnabled = _tmp != 0;
            final Long _tmpDisappearingMessagesTimer;
            if (_cursor.isNull(_cursorIndexOfDisappearingMessagesTimer)) {
              _tmpDisappearingMessagesTimer = null;
            } else {
              _tmpDisappearingMessagesTimer = _cursor.getLong(_cursorIndexOfDisappearingMessagesTimer);
            }
            final boolean _tmpIsArchived;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsArchived);
            _tmpIsArchived = _tmp_1 != 0;
            final boolean _tmpIsPinned;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_2 != 0;
            final boolean _tmpIsMuted;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsMuted);
            _tmpIsMuted = _tmp_3 != 0;
            final int _tmpUnreadCount;
            _tmpUnreadCount = _cursor.getInt(_cursorIndexOfUnreadCount);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new ChatEntity(_tmpId,_tmpType,_tmpName,_tmpParticipants,_tmpAdmins,_tmpIsNotificationsEnabled,_tmpDisappearingMessagesTimer,_tmpIsArchived,_tmpIsPinned,_tmpIsMuted,_tmpUnreadCount,_tmpCreatedAt,_tmpUpdatedAt);
            _result.add(_item);
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
  public Object getChatById(final String chatId,
      final Continuation<? super ChatEntity> $completion) {
    final String _sql = "SELECT * FROM chats WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, chatId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ChatEntity>() {
      @Override
      @Nullable
      public ChatEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfParticipants = CursorUtil.getColumnIndexOrThrow(_cursor, "participants");
          final int _cursorIndexOfAdmins = CursorUtil.getColumnIndexOrThrow(_cursor, "admins");
          final int _cursorIndexOfIsNotificationsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isNotificationsEnabled");
          final int _cursorIndexOfDisappearingMessagesTimer = CursorUtil.getColumnIndexOrThrow(_cursor, "disappearingMessagesTimer");
          final int _cursorIndexOfIsArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "isArchived");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfIsMuted = CursorUtil.getColumnIndexOrThrow(_cursor, "isMuted");
          final int _cursorIndexOfUnreadCount = CursorUtil.getColumnIndexOrThrow(_cursor, "unreadCount");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final ChatEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpParticipants;
            _tmpParticipants = _cursor.getString(_cursorIndexOfParticipants);
            final String _tmpAdmins;
            _tmpAdmins = _cursor.getString(_cursorIndexOfAdmins);
            final boolean _tmpIsNotificationsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsNotificationsEnabled);
            _tmpIsNotificationsEnabled = _tmp != 0;
            final Long _tmpDisappearingMessagesTimer;
            if (_cursor.isNull(_cursorIndexOfDisappearingMessagesTimer)) {
              _tmpDisappearingMessagesTimer = null;
            } else {
              _tmpDisappearingMessagesTimer = _cursor.getLong(_cursorIndexOfDisappearingMessagesTimer);
            }
            final boolean _tmpIsArchived;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsArchived);
            _tmpIsArchived = _tmp_1 != 0;
            final boolean _tmpIsPinned;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_2 != 0;
            final boolean _tmpIsMuted;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsMuted);
            _tmpIsMuted = _tmp_3 != 0;
            final int _tmpUnreadCount;
            _tmpUnreadCount = _cursor.getInt(_cursorIndexOfUnreadCount);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new ChatEntity(_tmpId,_tmpType,_tmpName,_tmpParticipants,_tmpAdmins,_tmpIsNotificationsEnabled,_tmpDisappearingMessagesTimer,_tmpIsArchived,_tmpIsPinned,_tmpIsMuted,_tmpUnreadCount,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Flow<List<ChatEntity>> observeAllChats() {
    final String _sql = "SELECT * FROM chats ORDER BY updatedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"chats"}, new Callable<List<ChatEntity>>() {
      @Override
      @NonNull
      public List<ChatEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfParticipants = CursorUtil.getColumnIndexOrThrow(_cursor, "participants");
          final int _cursorIndexOfAdmins = CursorUtil.getColumnIndexOrThrow(_cursor, "admins");
          final int _cursorIndexOfIsNotificationsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isNotificationsEnabled");
          final int _cursorIndexOfDisappearingMessagesTimer = CursorUtil.getColumnIndexOrThrow(_cursor, "disappearingMessagesTimer");
          final int _cursorIndexOfIsArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "isArchived");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfIsMuted = CursorUtil.getColumnIndexOrThrow(_cursor, "isMuted");
          final int _cursorIndexOfUnreadCount = CursorUtil.getColumnIndexOrThrow(_cursor, "unreadCount");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<ChatEntity> _result = new ArrayList<ChatEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChatEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpParticipants;
            _tmpParticipants = _cursor.getString(_cursorIndexOfParticipants);
            final String _tmpAdmins;
            _tmpAdmins = _cursor.getString(_cursorIndexOfAdmins);
            final boolean _tmpIsNotificationsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsNotificationsEnabled);
            _tmpIsNotificationsEnabled = _tmp != 0;
            final Long _tmpDisappearingMessagesTimer;
            if (_cursor.isNull(_cursorIndexOfDisappearingMessagesTimer)) {
              _tmpDisappearingMessagesTimer = null;
            } else {
              _tmpDisappearingMessagesTimer = _cursor.getLong(_cursorIndexOfDisappearingMessagesTimer);
            }
            final boolean _tmpIsArchived;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsArchived);
            _tmpIsArchived = _tmp_1 != 0;
            final boolean _tmpIsPinned;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_2 != 0;
            final boolean _tmpIsMuted;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsMuted);
            _tmpIsMuted = _tmp_3 != 0;
            final int _tmpUnreadCount;
            _tmpUnreadCount = _cursor.getInt(_cursorIndexOfUnreadCount);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new ChatEntity(_tmpId,_tmpType,_tmpName,_tmpParticipants,_tmpAdmins,_tmpIsNotificationsEnabled,_tmpDisappearingMessagesTimer,_tmpIsArchived,_tmpIsPinned,_tmpIsMuted,_tmpUnreadCount,_tmpCreatedAt,_tmpUpdatedAt);
            _result.add(_item);
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
  public Flow<ChatEntity> observeChatById(final String chatId) {
    final String _sql = "SELECT * FROM chats WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, chatId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"chats"}, new Callable<ChatEntity>() {
      @Override
      @Nullable
      public ChatEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfParticipants = CursorUtil.getColumnIndexOrThrow(_cursor, "participants");
          final int _cursorIndexOfAdmins = CursorUtil.getColumnIndexOrThrow(_cursor, "admins");
          final int _cursorIndexOfIsNotificationsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isNotificationsEnabled");
          final int _cursorIndexOfDisappearingMessagesTimer = CursorUtil.getColumnIndexOrThrow(_cursor, "disappearingMessagesTimer");
          final int _cursorIndexOfIsArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "isArchived");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfIsMuted = CursorUtil.getColumnIndexOrThrow(_cursor, "isMuted");
          final int _cursorIndexOfUnreadCount = CursorUtil.getColumnIndexOrThrow(_cursor, "unreadCount");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final ChatEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpParticipants;
            _tmpParticipants = _cursor.getString(_cursorIndexOfParticipants);
            final String _tmpAdmins;
            _tmpAdmins = _cursor.getString(_cursorIndexOfAdmins);
            final boolean _tmpIsNotificationsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsNotificationsEnabled);
            _tmpIsNotificationsEnabled = _tmp != 0;
            final Long _tmpDisappearingMessagesTimer;
            if (_cursor.isNull(_cursorIndexOfDisappearingMessagesTimer)) {
              _tmpDisappearingMessagesTimer = null;
            } else {
              _tmpDisappearingMessagesTimer = _cursor.getLong(_cursorIndexOfDisappearingMessagesTimer);
            }
            final boolean _tmpIsArchived;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsArchived);
            _tmpIsArchived = _tmp_1 != 0;
            final boolean _tmpIsPinned;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_2 != 0;
            final boolean _tmpIsMuted;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsMuted);
            _tmpIsMuted = _tmp_3 != 0;
            final int _tmpUnreadCount;
            _tmpUnreadCount = _cursor.getInt(_cursorIndexOfUnreadCount);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new ChatEntity(_tmpId,_tmpType,_tmpName,_tmpParticipants,_tmpAdmins,_tmpIsNotificationsEnabled,_tmpDisappearingMessagesTimer,_tmpIsArchived,_tmpIsPinned,_tmpIsMuted,_tmpUnreadCount,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getChatCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM chats";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
