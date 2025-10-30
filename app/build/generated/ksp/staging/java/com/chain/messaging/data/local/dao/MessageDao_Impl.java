package com.chain.messaging.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.RelationUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.chain.messaging.data.local.entity.MessageEntity;
import com.chain.messaging.data.local.entity.MessageWithReactions;
import com.chain.messaging.data.local.entity.ReactionEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MessageDao_Impl implements MessageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MessageEntity> __insertionAdapterOfMessageEntity;

  private final EntityDeletionOrUpdateAdapter<MessageEntity> __deletionAdapterOfMessageEntity;

  private final EntityDeletionOrUpdateAdapter<MessageEntity> __updateAdapterOfMessageEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMessagesByChatId;

  private final SharedSQLiteStatement __preparedStmtOfDeleteExpiredMessages;

  public MessageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMessageEntity = new EntityInsertionAdapter<MessageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `messages` (`id`,`chatId`,`senderId`,`content`,`type`,`timestamp`,`status`,`replyTo`,`isEncrypted`,`disappearingMessageTimer`,`expiresAt`,`isDisappearing`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MessageEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getChatId());
        statement.bindString(3, entity.getSenderId());
        statement.bindString(4, entity.getContent());
        statement.bindString(5, entity.getType());
        statement.bindLong(6, entity.getTimestamp());
        statement.bindString(7, entity.getStatus());
        if (entity.getReplyTo() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getReplyTo());
        }
        final int _tmp = entity.isEncrypted() ? 1 : 0;
        statement.bindLong(9, _tmp);
        if (entity.getDisappearingMessageTimer() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getDisappearingMessageTimer());
        }
        if (entity.getExpiresAt() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getExpiresAt());
        }
        final int _tmp_1 = entity.isDisappearing() ? 1 : 0;
        statement.bindLong(12, _tmp_1);
        statement.bindLong(13, entity.getCreatedAt());
        statement.bindLong(14, entity.getUpdatedAt());
      }
    };
    this.__deletionAdapterOfMessageEntity = new EntityDeletionOrUpdateAdapter<MessageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `messages` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MessageEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__updateAdapterOfMessageEntity = new EntityDeletionOrUpdateAdapter<MessageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `messages` SET `id` = ?,`chatId` = ?,`senderId` = ?,`content` = ?,`type` = ?,`timestamp` = ?,`status` = ?,`replyTo` = ?,`isEncrypted` = ?,`disappearingMessageTimer` = ?,`expiresAt` = ?,`isDisappearing` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MessageEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getChatId());
        statement.bindString(3, entity.getSenderId());
        statement.bindString(4, entity.getContent());
        statement.bindString(5, entity.getType());
        statement.bindLong(6, entity.getTimestamp());
        statement.bindString(7, entity.getStatus());
        if (entity.getReplyTo() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getReplyTo());
        }
        final int _tmp = entity.isEncrypted() ? 1 : 0;
        statement.bindLong(9, _tmp);
        if (entity.getDisappearingMessageTimer() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getDisappearingMessageTimer());
        }
        if (entity.getExpiresAt() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getExpiresAt());
        }
        final int _tmp_1 = entity.isDisappearing() ? 1 : 0;
        statement.bindLong(12, _tmp_1);
        statement.bindLong(13, entity.getCreatedAt());
        statement.bindLong(14, entity.getUpdatedAt());
        statement.bindString(15, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteMessagesByChatId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM messages WHERE chatId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteExpiredMessages = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM messages WHERE isDisappearing = 1 AND expiresAt <= ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertMessage(final MessageEntity message,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMessageEntity.insert(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertMessages(final List<MessageEntity> messages,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMessageEntity.insert(messages);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMessage(final MessageEntity message,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfMessageEntity.handle(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMessage(final MessageEntity message,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfMessageEntity.handle(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMessagesByChatId(final String chatId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMessagesByChatId.acquire();
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
          __preparedStmtOfDeleteMessagesByChatId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteExpiredMessages(final long currentTime,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteExpiredMessages.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, currentTime);
        try {
          __db.beginTransaction();
          try {
            final Integer _result = _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteExpiredMessages.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getMessagesByChatId(final String chatId, final int limit, final int offset,
      final Continuation<? super List<MessageEntity>> $completion) {
    final String _sql = "SELECT * FROM messages WHERE chatId = ? ORDER BY timestamp DESC LIMIT ? OFFSET ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, chatId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    _argIndex = 3;
    _statement.bindLong(_argIndex, offset);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MessageEntity>>() {
      @Override
      @NonNull
      public List<MessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfChatId = CursorUtil.getColumnIndexOrThrow(_cursor, "chatId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfReplyTo = CursorUtil.getColumnIndexOrThrow(_cursor, "replyTo");
          final int _cursorIndexOfIsEncrypted = CursorUtil.getColumnIndexOrThrow(_cursor, "isEncrypted");
          final int _cursorIndexOfDisappearingMessageTimer = CursorUtil.getColumnIndexOrThrow(_cursor, "disappearingMessageTimer");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfIsDisappearing = CursorUtil.getColumnIndexOrThrow(_cursor, "isDisappearing");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<MessageEntity> _result = new ArrayList<MessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpChatId;
            _tmpChatId = _cursor.getString(_cursorIndexOfChatId);
            final String _tmpSenderId;
            _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpReplyTo;
            if (_cursor.isNull(_cursorIndexOfReplyTo)) {
              _tmpReplyTo = null;
            } else {
              _tmpReplyTo = _cursor.getString(_cursorIndexOfReplyTo);
            }
            final boolean _tmpIsEncrypted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEncrypted);
            _tmpIsEncrypted = _tmp != 0;
            final Long _tmpDisappearingMessageTimer;
            if (_cursor.isNull(_cursorIndexOfDisappearingMessageTimer)) {
              _tmpDisappearingMessageTimer = null;
            } else {
              _tmpDisappearingMessageTimer = _cursor.getLong(_cursorIndexOfDisappearingMessageTimer);
            }
            final Long _tmpExpiresAt;
            if (_cursor.isNull(_cursorIndexOfExpiresAt)) {
              _tmpExpiresAt = null;
            } else {
              _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            }
            final boolean _tmpIsDisappearing;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDisappearing);
            _tmpIsDisappearing = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new MessageEntity(_tmpId,_tmpChatId,_tmpSenderId,_tmpContent,_tmpType,_tmpTimestamp,_tmpStatus,_tmpReplyTo,_tmpIsEncrypted,_tmpDisappearingMessageTimer,_tmpExpiresAt,_tmpIsDisappearing,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getMessageById(final String messageId,
      final Continuation<? super MessageEntity> $completion) {
    final String _sql = "SELECT * FROM messages WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, messageId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MessageEntity>() {
      @Override
      @Nullable
      public MessageEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfChatId = CursorUtil.getColumnIndexOrThrow(_cursor, "chatId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfReplyTo = CursorUtil.getColumnIndexOrThrow(_cursor, "replyTo");
          final int _cursorIndexOfIsEncrypted = CursorUtil.getColumnIndexOrThrow(_cursor, "isEncrypted");
          final int _cursorIndexOfDisappearingMessageTimer = CursorUtil.getColumnIndexOrThrow(_cursor, "disappearingMessageTimer");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfIsDisappearing = CursorUtil.getColumnIndexOrThrow(_cursor, "isDisappearing");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final MessageEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpChatId;
            _tmpChatId = _cursor.getString(_cursorIndexOfChatId);
            final String _tmpSenderId;
            _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpReplyTo;
            if (_cursor.isNull(_cursorIndexOfReplyTo)) {
              _tmpReplyTo = null;
            } else {
              _tmpReplyTo = _cursor.getString(_cursorIndexOfReplyTo);
            }
            final boolean _tmpIsEncrypted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEncrypted);
            _tmpIsEncrypted = _tmp != 0;
            final Long _tmpDisappearingMessageTimer;
            if (_cursor.isNull(_cursorIndexOfDisappearingMessageTimer)) {
              _tmpDisappearingMessageTimer = null;
            } else {
              _tmpDisappearingMessageTimer = _cursor.getLong(_cursorIndexOfDisappearingMessageTimer);
            }
            final Long _tmpExpiresAt;
            if (_cursor.isNull(_cursorIndexOfExpiresAt)) {
              _tmpExpiresAt = null;
            } else {
              _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            }
            final boolean _tmpIsDisappearing;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDisappearing);
            _tmpIsDisappearing = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new MessageEntity(_tmpId,_tmpChatId,_tmpSenderId,_tmpContent,_tmpType,_tmpTimestamp,_tmpStatus,_tmpReplyTo,_tmpIsEncrypted,_tmpDisappearingMessageTimer,_tmpExpiresAt,_tmpIsDisappearing,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object searchMessages(final String query,
      final Continuation<? super List<MessageEntity>> $completion) {
    final String _sql = "SELECT * FROM messages WHERE content LIKE '%' || ? || '%' ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MessageEntity>>() {
      @Override
      @NonNull
      public List<MessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfChatId = CursorUtil.getColumnIndexOrThrow(_cursor, "chatId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfReplyTo = CursorUtil.getColumnIndexOrThrow(_cursor, "replyTo");
          final int _cursorIndexOfIsEncrypted = CursorUtil.getColumnIndexOrThrow(_cursor, "isEncrypted");
          final int _cursorIndexOfDisappearingMessageTimer = CursorUtil.getColumnIndexOrThrow(_cursor, "disappearingMessageTimer");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfIsDisappearing = CursorUtil.getColumnIndexOrThrow(_cursor, "isDisappearing");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<MessageEntity> _result = new ArrayList<MessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpChatId;
            _tmpChatId = _cursor.getString(_cursorIndexOfChatId);
            final String _tmpSenderId;
            _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpReplyTo;
            if (_cursor.isNull(_cursorIndexOfReplyTo)) {
              _tmpReplyTo = null;
            } else {
              _tmpReplyTo = _cursor.getString(_cursorIndexOfReplyTo);
            }
            final boolean _tmpIsEncrypted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEncrypted);
            _tmpIsEncrypted = _tmp != 0;
            final Long _tmpDisappearingMessageTimer;
            if (_cursor.isNull(_cursorIndexOfDisappearingMessageTimer)) {
              _tmpDisappearingMessageTimer = null;
            } else {
              _tmpDisappearingMessageTimer = _cursor.getLong(_cursorIndexOfDisappearingMessageTimer);
            }
            final Long _tmpExpiresAt;
            if (_cursor.isNull(_cursorIndexOfExpiresAt)) {
              _tmpExpiresAt = null;
            } else {
              _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            }
            final boolean _tmpIsDisappearing;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDisappearing);
            _tmpIsDisappearing = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new MessageEntity(_tmpId,_tmpChatId,_tmpSenderId,_tmpContent,_tmpType,_tmpTimestamp,_tmpStatus,_tmpReplyTo,_tmpIsEncrypted,_tmpDisappearingMessageTimer,_tmpExpiresAt,_tmpIsDisappearing,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Flow<List<MessageEntity>> observeMessagesByChatId(final String chatId) {
    final String _sql = "SELECT * FROM messages WHERE chatId = ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, chatId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"messages"}, new Callable<List<MessageEntity>>() {
      @Override
      @NonNull
      public List<MessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfChatId = CursorUtil.getColumnIndexOrThrow(_cursor, "chatId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfReplyTo = CursorUtil.getColumnIndexOrThrow(_cursor, "replyTo");
          final int _cursorIndexOfIsEncrypted = CursorUtil.getColumnIndexOrThrow(_cursor, "isEncrypted");
          final int _cursorIndexOfDisappearingMessageTimer = CursorUtil.getColumnIndexOrThrow(_cursor, "disappearingMessageTimer");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfIsDisappearing = CursorUtil.getColumnIndexOrThrow(_cursor, "isDisappearing");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<MessageEntity> _result = new ArrayList<MessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpChatId;
            _tmpChatId = _cursor.getString(_cursorIndexOfChatId);
            final String _tmpSenderId;
            _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpReplyTo;
            if (_cursor.isNull(_cursorIndexOfReplyTo)) {
              _tmpReplyTo = null;
            } else {
              _tmpReplyTo = _cursor.getString(_cursorIndexOfReplyTo);
            }
            final boolean _tmpIsEncrypted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEncrypted);
            _tmpIsEncrypted = _tmp != 0;
            final Long _tmpDisappearingMessageTimer;
            if (_cursor.isNull(_cursorIndexOfDisappearingMessageTimer)) {
              _tmpDisappearingMessageTimer = null;
            } else {
              _tmpDisappearingMessageTimer = _cursor.getLong(_cursorIndexOfDisappearingMessageTimer);
            }
            final Long _tmpExpiresAt;
            if (_cursor.isNull(_cursorIndexOfExpiresAt)) {
              _tmpExpiresAt = null;
            } else {
              _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            }
            final boolean _tmpIsDisappearing;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDisappearing);
            _tmpIsDisappearing = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new MessageEntity(_tmpId,_tmpChatId,_tmpSenderId,_tmpContent,_tmpType,_tmpTimestamp,_tmpStatus,_tmpReplyTo,_tmpIsEncrypted,_tmpDisappearingMessageTimer,_tmpExpiresAt,_tmpIsDisappearing,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getLastMessageByChatId(final String chatId,
      final Continuation<? super MessageEntity> $completion) {
    final String _sql = "SELECT * FROM messages WHERE chatId = ? ORDER BY timestamp DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, chatId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MessageEntity>() {
      @Override
      @Nullable
      public MessageEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfChatId = CursorUtil.getColumnIndexOrThrow(_cursor, "chatId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfReplyTo = CursorUtil.getColumnIndexOrThrow(_cursor, "replyTo");
          final int _cursorIndexOfIsEncrypted = CursorUtil.getColumnIndexOrThrow(_cursor, "isEncrypted");
          final int _cursorIndexOfDisappearingMessageTimer = CursorUtil.getColumnIndexOrThrow(_cursor, "disappearingMessageTimer");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfIsDisappearing = CursorUtil.getColumnIndexOrThrow(_cursor, "isDisappearing");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final MessageEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpChatId;
            _tmpChatId = _cursor.getString(_cursorIndexOfChatId);
            final String _tmpSenderId;
            _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpReplyTo;
            if (_cursor.isNull(_cursorIndexOfReplyTo)) {
              _tmpReplyTo = null;
            } else {
              _tmpReplyTo = _cursor.getString(_cursorIndexOfReplyTo);
            }
            final boolean _tmpIsEncrypted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEncrypted);
            _tmpIsEncrypted = _tmp != 0;
            final Long _tmpDisappearingMessageTimer;
            if (_cursor.isNull(_cursorIndexOfDisappearingMessageTimer)) {
              _tmpDisappearingMessageTimer = null;
            } else {
              _tmpDisappearingMessageTimer = _cursor.getLong(_cursorIndexOfDisappearingMessageTimer);
            }
            final Long _tmpExpiresAt;
            if (_cursor.isNull(_cursorIndexOfExpiresAt)) {
              _tmpExpiresAt = null;
            } else {
              _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            }
            final boolean _tmpIsDisappearing;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDisappearing);
            _tmpIsDisappearing = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new MessageEntity(_tmpId,_tmpChatId,_tmpSenderId,_tmpContent,_tmpType,_tmpTimestamp,_tmpStatus,_tmpReplyTo,_tmpIsEncrypted,_tmpDisappearingMessageTimer,_tmpExpiresAt,_tmpIsDisappearing,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getUnreadMessageCount(final String chatId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM messages WHERE chatId = ? AND status != 'READ'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, chatId);
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

  @Override
  public Object getExpiredMessages(final long currentTime,
      final Continuation<? super List<MessageEntity>> $completion) {
    final String _sql = "SELECT * FROM messages WHERE isDisappearing = 1 AND expiresAt <= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, currentTime);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MessageEntity>>() {
      @Override
      @NonNull
      public List<MessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfChatId = CursorUtil.getColumnIndexOrThrow(_cursor, "chatId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfReplyTo = CursorUtil.getColumnIndexOrThrow(_cursor, "replyTo");
          final int _cursorIndexOfIsEncrypted = CursorUtil.getColumnIndexOrThrow(_cursor, "isEncrypted");
          final int _cursorIndexOfDisappearingMessageTimer = CursorUtil.getColumnIndexOrThrow(_cursor, "disappearingMessageTimer");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfIsDisappearing = CursorUtil.getColumnIndexOrThrow(_cursor, "isDisappearing");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<MessageEntity> _result = new ArrayList<MessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpChatId;
            _tmpChatId = _cursor.getString(_cursorIndexOfChatId);
            final String _tmpSenderId;
            _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpReplyTo;
            if (_cursor.isNull(_cursorIndexOfReplyTo)) {
              _tmpReplyTo = null;
            } else {
              _tmpReplyTo = _cursor.getString(_cursorIndexOfReplyTo);
            }
            final boolean _tmpIsEncrypted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEncrypted);
            _tmpIsEncrypted = _tmp != 0;
            final Long _tmpDisappearingMessageTimer;
            if (_cursor.isNull(_cursorIndexOfDisappearingMessageTimer)) {
              _tmpDisappearingMessageTimer = null;
            } else {
              _tmpDisappearingMessageTimer = _cursor.getLong(_cursorIndexOfDisappearingMessageTimer);
            }
            final Long _tmpExpiresAt;
            if (_cursor.isNull(_cursorIndexOfExpiresAt)) {
              _tmpExpiresAt = null;
            } else {
              _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            }
            final boolean _tmpIsDisappearing;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDisappearing);
            _tmpIsDisappearing = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new MessageEntity(_tmpId,_tmpChatId,_tmpSenderId,_tmpContent,_tmpType,_tmpTimestamp,_tmpStatus,_tmpReplyTo,_tmpIsEncrypted,_tmpDisappearingMessageTimer,_tmpExpiresAt,_tmpIsDisappearing,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getMessagesExpiringBefore(final long time, final long currentTime,
      final Continuation<? super List<MessageEntity>> $completion) {
    final String _sql = "SELECT * FROM messages WHERE isDisappearing = 1 AND expiresAt <= ? AND expiresAt > ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, time);
    _argIndex = 2;
    _statement.bindLong(_argIndex, currentTime);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MessageEntity>>() {
      @Override
      @NonNull
      public List<MessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfChatId = CursorUtil.getColumnIndexOrThrow(_cursor, "chatId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfReplyTo = CursorUtil.getColumnIndexOrThrow(_cursor, "replyTo");
          final int _cursorIndexOfIsEncrypted = CursorUtil.getColumnIndexOrThrow(_cursor, "isEncrypted");
          final int _cursorIndexOfDisappearingMessageTimer = CursorUtil.getColumnIndexOrThrow(_cursor, "disappearingMessageTimer");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfIsDisappearing = CursorUtil.getColumnIndexOrThrow(_cursor, "isDisappearing");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<MessageEntity> _result = new ArrayList<MessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpChatId;
            _tmpChatId = _cursor.getString(_cursorIndexOfChatId);
            final String _tmpSenderId;
            _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpReplyTo;
            if (_cursor.isNull(_cursorIndexOfReplyTo)) {
              _tmpReplyTo = null;
            } else {
              _tmpReplyTo = _cursor.getString(_cursorIndexOfReplyTo);
            }
            final boolean _tmpIsEncrypted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEncrypted);
            _tmpIsEncrypted = _tmp != 0;
            final Long _tmpDisappearingMessageTimer;
            if (_cursor.isNull(_cursorIndexOfDisappearingMessageTimer)) {
              _tmpDisappearingMessageTimer = null;
            } else {
              _tmpDisappearingMessageTimer = _cursor.getLong(_cursorIndexOfDisappearingMessageTimer);
            }
            final Long _tmpExpiresAt;
            if (_cursor.isNull(_cursorIndexOfExpiresAt)) {
              _tmpExpiresAt = null;
            } else {
              _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            }
            final boolean _tmpIsDisappearing;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDisappearing);
            _tmpIsDisappearing = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new MessageEntity(_tmpId,_tmpChatId,_tmpSenderId,_tmpContent,_tmpType,_tmpTimestamp,_tmpStatus,_tmpReplyTo,_tmpIsEncrypted,_tmpDisappearingMessageTimer,_tmpExpiresAt,_tmpIsDisappearing,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Flow<List<MessageEntity>> observeDisappearingMessages() {
    final String _sql = "SELECT * FROM messages WHERE isDisappearing = 1 AND expiresAt IS NOT NULL ORDER BY expiresAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"messages"}, new Callable<List<MessageEntity>>() {
      @Override
      @NonNull
      public List<MessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfChatId = CursorUtil.getColumnIndexOrThrow(_cursor, "chatId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfReplyTo = CursorUtil.getColumnIndexOrThrow(_cursor, "replyTo");
          final int _cursorIndexOfIsEncrypted = CursorUtil.getColumnIndexOrThrow(_cursor, "isEncrypted");
          final int _cursorIndexOfDisappearingMessageTimer = CursorUtil.getColumnIndexOrThrow(_cursor, "disappearingMessageTimer");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfIsDisappearing = CursorUtil.getColumnIndexOrThrow(_cursor, "isDisappearing");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<MessageEntity> _result = new ArrayList<MessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpChatId;
            _tmpChatId = _cursor.getString(_cursorIndexOfChatId);
            final String _tmpSenderId;
            _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpReplyTo;
            if (_cursor.isNull(_cursorIndexOfReplyTo)) {
              _tmpReplyTo = null;
            } else {
              _tmpReplyTo = _cursor.getString(_cursorIndexOfReplyTo);
            }
            final boolean _tmpIsEncrypted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEncrypted);
            _tmpIsEncrypted = _tmp != 0;
            final Long _tmpDisappearingMessageTimer;
            if (_cursor.isNull(_cursorIndexOfDisappearingMessageTimer)) {
              _tmpDisappearingMessageTimer = null;
            } else {
              _tmpDisappearingMessageTimer = _cursor.getLong(_cursorIndexOfDisappearingMessageTimer);
            }
            final Long _tmpExpiresAt;
            if (_cursor.isNull(_cursorIndexOfExpiresAt)) {
              _tmpExpiresAt = null;
            } else {
              _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            }
            final boolean _tmpIsDisappearing;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDisappearing);
            _tmpIsDisappearing = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new MessageEntity(_tmpId,_tmpChatId,_tmpSenderId,_tmpContent,_tmpType,_tmpTimestamp,_tmpStatus,_tmpReplyTo,_tmpIsEncrypted,_tmpDisappearingMessageTimer,_tmpExpiresAt,_tmpIsDisappearing,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getRecentMessages(final int limit,
      final Continuation<? super List<MessageEntity>> $completion) {
    final String _sql = "SELECT * FROM messages ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MessageEntity>>() {
      @Override
      @NonNull
      public List<MessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfChatId = CursorUtil.getColumnIndexOrThrow(_cursor, "chatId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfReplyTo = CursorUtil.getColumnIndexOrThrow(_cursor, "replyTo");
          final int _cursorIndexOfIsEncrypted = CursorUtil.getColumnIndexOrThrow(_cursor, "isEncrypted");
          final int _cursorIndexOfDisappearingMessageTimer = CursorUtil.getColumnIndexOrThrow(_cursor, "disappearingMessageTimer");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfIsDisappearing = CursorUtil.getColumnIndexOrThrow(_cursor, "isDisappearing");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<MessageEntity> _result = new ArrayList<MessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpChatId;
            _tmpChatId = _cursor.getString(_cursorIndexOfChatId);
            final String _tmpSenderId;
            _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpReplyTo;
            if (_cursor.isNull(_cursorIndexOfReplyTo)) {
              _tmpReplyTo = null;
            } else {
              _tmpReplyTo = _cursor.getString(_cursorIndexOfReplyTo);
            }
            final boolean _tmpIsEncrypted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEncrypted);
            _tmpIsEncrypted = _tmp != 0;
            final Long _tmpDisappearingMessageTimer;
            if (_cursor.isNull(_cursorIndexOfDisappearingMessageTimer)) {
              _tmpDisappearingMessageTimer = null;
            } else {
              _tmpDisappearingMessageTimer = _cursor.getLong(_cursorIndexOfDisappearingMessageTimer);
            }
            final Long _tmpExpiresAt;
            if (_cursor.isNull(_cursorIndexOfExpiresAt)) {
              _tmpExpiresAt = null;
            } else {
              _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            }
            final boolean _tmpIsDisappearing;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDisappearing);
            _tmpIsDisappearing = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new MessageEntity(_tmpId,_tmpChatId,_tmpSenderId,_tmpContent,_tmpType,_tmpTimestamp,_tmpStatus,_tmpReplyTo,_tmpIsEncrypted,_tmpDisappearingMessageTimer,_tmpExpiresAt,_tmpIsDisappearing,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getAllMessages(final Continuation<? super List<MessageEntity>> $completion) {
    final String _sql = "SELECT * FROM messages";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MessageEntity>>() {
      @Override
      @NonNull
      public List<MessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfChatId = CursorUtil.getColumnIndexOrThrow(_cursor, "chatId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfReplyTo = CursorUtil.getColumnIndexOrThrow(_cursor, "replyTo");
          final int _cursorIndexOfIsEncrypted = CursorUtil.getColumnIndexOrThrow(_cursor, "isEncrypted");
          final int _cursorIndexOfDisappearingMessageTimer = CursorUtil.getColumnIndexOrThrow(_cursor, "disappearingMessageTimer");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfIsDisappearing = CursorUtil.getColumnIndexOrThrow(_cursor, "isDisappearing");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<MessageEntity> _result = new ArrayList<MessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpChatId;
            _tmpChatId = _cursor.getString(_cursorIndexOfChatId);
            final String _tmpSenderId;
            _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpReplyTo;
            if (_cursor.isNull(_cursorIndexOfReplyTo)) {
              _tmpReplyTo = null;
            } else {
              _tmpReplyTo = _cursor.getString(_cursorIndexOfReplyTo);
            }
            final boolean _tmpIsEncrypted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEncrypted);
            _tmpIsEncrypted = _tmp != 0;
            final Long _tmpDisappearingMessageTimer;
            if (_cursor.isNull(_cursorIndexOfDisappearingMessageTimer)) {
              _tmpDisappearingMessageTimer = null;
            } else {
              _tmpDisappearingMessageTimer = _cursor.getLong(_cursorIndexOfDisappearingMessageTimer);
            }
            final Long _tmpExpiresAt;
            if (_cursor.isNull(_cursorIndexOfExpiresAt)) {
              _tmpExpiresAt = null;
            } else {
              _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            }
            final boolean _tmpIsDisappearing;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDisappearing);
            _tmpIsDisappearing = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new MessageEntity(_tmpId,_tmpChatId,_tmpSenderId,_tmpContent,_tmpType,_tmpTimestamp,_tmpStatus,_tmpReplyTo,_tmpIsEncrypted,_tmpDisappearingMessageTimer,_tmpExpiresAt,_tmpIsDisappearing,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getMessagesSince(final long since,
      final Continuation<? super List<MessageEntity>> $completion) {
    final String _sql = "SELECT * FROM messages WHERE timestamp >= ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MessageEntity>>() {
      @Override
      @NonNull
      public List<MessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfChatId = CursorUtil.getColumnIndexOrThrow(_cursor, "chatId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfReplyTo = CursorUtil.getColumnIndexOrThrow(_cursor, "replyTo");
          final int _cursorIndexOfIsEncrypted = CursorUtil.getColumnIndexOrThrow(_cursor, "isEncrypted");
          final int _cursorIndexOfDisappearingMessageTimer = CursorUtil.getColumnIndexOrThrow(_cursor, "disappearingMessageTimer");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfIsDisappearing = CursorUtil.getColumnIndexOrThrow(_cursor, "isDisappearing");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<MessageEntity> _result = new ArrayList<MessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpChatId;
            _tmpChatId = _cursor.getString(_cursorIndexOfChatId);
            final String _tmpSenderId;
            _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpReplyTo;
            if (_cursor.isNull(_cursorIndexOfReplyTo)) {
              _tmpReplyTo = null;
            } else {
              _tmpReplyTo = _cursor.getString(_cursorIndexOfReplyTo);
            }
            final boolean _tmpIsEncrypted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEncrypted);
            _tmpIsEncrypted = _tmp != 0;
            final Long _tmpDisappearingMessageTimer;
            if (_cursor.isNull(_cursorIndexOfDisappearingMessageTimer)) {
              _tmpDisappearingMessageTimer = null;
            } else {
              _tmpDisappearingMessageTimer = _cursor.getLong(_cursorIndexOfDisappearingMessageTimer);
            }
            final Long _tmpExpiresAt;
            if (_cursor.isNull(_cursorIndexOfExpiresAt)) {
              _tmpExpiresAt = null;
            } else {
              _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            }
            final boolean _tmpIsDisappearing;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDisappearing);
            _tmpIsDisappearing = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new MessageEntity(_tmpId,_tmpChatId,_tmpSenderId,_tmpContent,_tmpType,_tmpTimestamp,_tmpStatus,_tmpReplyTo,_tmpIsEncrypted,_tmpDisappearingMessageTimer,_tmpExpiresAt,_tmpIsDisappearing,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getMessagesWithReactionsByChatId(final String chatId, final int limit,
      final int offset, final Continuation<? super List<MessageWithReactions>> $completion) {
    final String _sql = "SELECT * FROM messages WHERE chatId = ? ORDER BY timestamp DESC LIMIT ? OFFSET ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, chatId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    _argIndex = 3;
    _statement.bindLong(_argIndex, offset);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, true, _cancellationSignal, new Callable<List<MessageWithReactions>>() {
      @Override
      @NonNull
      public List<MessageWithReactions> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfChatId = CursorUtil.getColumnIndexOrThrow(_cursor, "chatId");
            final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
            final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
            final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
            final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
            final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
            final int _cursorIndexOfReplyTo = CursorUtil.getColumnIndexOrThrow(_cursor, "replyTo");
            final int _cursorIndexOfIsEncrypted = CursorUtil.getColumnIndexOrThrow(_cursor, "isEncrypted");
            final int _cursorIndexOfDisappearingMessageTimer = CursorUtil.getColumnIndexOrThrow(_cursor, "disappearingMessageTimer");
            final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
            final int _cursorIndexOfIsDisappearing = CursorUtil.getColumnIndexOrThrow(_cursor, "isDisappearing");
            final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
            final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
            final ArrayMap<String, ArrayList<ReactionEntity>> _collectionReactions = new ArrayMap<String, ArrayList<ReactionEntity>>();
            while (_cursor.moveToNext()) {
              final String _tmpKey;
              _tmpKey = _cursor.getString(_cursorIndexOfId);
              if (!_collectionReactions.containsKey(_tmpKey)) {
                _collectionReactions.put(_tmpKey, new ArrayList<ReactionEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipreactionsAscomChainMessagingDataLocalEntityReactionEntity(_collectionReactions);
            final List<MessageWithReactions> _result = new ArrayList<MessageWithReactions>(_cursor.getCount());
            while (_cursor.moveToNext()) {
              final MessageWithReactions _item;
              final MessageEntity _tmpMessage;
              final String _tmpId;
              _tmpId = _cursor.getString(_cursorIndexOfId);
              final String _tmpChatId;
              _tmpChatId = _cursor.getString(_cursorIndexOfChatId);
              final String _tmpSenderId;
              _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
              final String _tmpContent;
              _tmpContent = _cursor.getString(_cursorIndexOfContent);
              final String _tmpType;
              _tmpType = _cursor.getString(_cursorIndexOfType);
              final long _tmpTimestamp;
              _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
              final String _tmpStatus;
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
              final String _tmpReplyTo;
              if (_cursor.isNull(_cursorIndexOfReplyTo)) {
                _tmpReplyTo = null;
              } else {
                _tmpReplyTo = _cursor.getString(_cursorIndexOfReplyTo);
              }
              final boolean _tmpIsEncrypted;
              final int _tmp;
              _tmp = _cursor.getInt(_cursorIndexOfIsEncrypted);
              _tmpIsEncrypted = _tmp != 0;
              final Long _tmpDisappearingMessageTimer;
              if (_cursor.isNull(_cursorIndexOfDisappearingMessageTimer)) {
                _tmpDisappearingMessageTimer = null;
              } else {
                _tmpDisappearingMessageTimer = _cursor.getLong(_cursorIndexOfDisappearingMessageTimer);
              }
              final Long _tmpExpiresAt;
              if (_cursor.isNull(_cursorIndexOfExpiresAt)) {
                _tmpExpiresAt = null;
              } else {
                _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
              }
              final boolean _tmpIsDisappearing;
              final int _tmp_1;
              _tmp_1 = _cursor.getInt(_cursorIndexOfIsDisappearing);
              _tmpIsDisappearing = _tmp_1 != 0;
              final long _tmpCreatedAt;
              _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
              final long _tmpUpdatedAt;
              _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
              _tmpMessage = new MessageEntity(_tmpId,_tmpChatId,_tmpSenderId,_tmpContent,_tmpType,_tmpTimestamp,_tmpStatus,_tmpReplyTo,_tmpIsEncrypted,_tmpDisappearingMessageTimer,_tmpExpiresAt,_tmpIsDisappearing,_tmpCreatedAt,_tmpUpdatedAt);
              final ArrayList<ReactionEntity> _tmpReactionsCollection;
              final String _tmpKey_1;
              _tmpKey_1 = _cursor.getString(_cursorIndexOfId);
              _tmpReactionsCollection = _collectionReactions.get(_tmpKey_1);
              _item = new MessageWithReactions(_tmpMessage,_tmpReactionsCollection);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
            _statement.release();
          }
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getMessageWithReactionsById(final String messageId,
      final Continuation<? super MessageWithReactions> $completion) {
    final String _sql = "SELECT * FROM messages WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, messageId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, true, _cancellationSignal, new Callable<MessageWithReactions>() {
      @Override
      @Nullable
      public MessageWithReactions call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfChatId = CursorUtil.getColumnIndexOrThrow(_cursor, "chatId");
            final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
            final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
            final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
            final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
            final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
            final int _cursorIndexOfReplyTo = CursorUtil.getColumnIndexOrThrow(_cursor, "replyTo");
            final int _cursorIndexOfIsEncrypted = CursorUtil.getColumnIndexOrThrow(_cursor, "isEncrypted");
            final int _cursorIndexOfDisappearingMessageTimer = CursorUtil.getColumnIndexOrThrow(_cursor, "disappearingMessageTimer");
            final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
            final int _cursorIndexOfIsDisappearing = CursorUtil.getColumnIndexOrThrow(_cursor, "isDisappearing");
            final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
            final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
            final ArrayMap<String, ArrayList<ReactionEntity>> _collectionReactions = new ArrayMap<String, ArrayList<ReactionEntity>>();
            while (_cursor.moveToNext()) {
              final String _tmpKey;
              _tmpKey = _cursor.getString(_cursorIndexOfId);
              if (!_collectionReactions.containsKey(_tmpKey)) {
                _collectionReactions.put(_tmpKey, new ArrayList<ReactionEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipreactionsAscomChainMessagingDataLocalEntityReactionEntity(_collectionReactions);
            final MessageWithReactions _result;
            if (_cursor.moveToFirst()) {
              final MessageEntity _tmpMessage;
              final String _tmpId;
              _tmpId = _cursor.getString(_cursorIndexOfId);
              final String _tmpChatId;
              _tmpChatId = _cursor.getString(_cursorIndexOfChatId);
              final String _tmpSenderId;
              _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
              final String _tmpContent;
              _tmpContent = _cursor.getString(_cursorIndexOfContent);
              final String _tmpType;
              _tmpType = _cursor.getString(_cursorIndexOfType);
              final long _tmpTimestamp;
              _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
              final String _tmpStatus;
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
              final String _tmpReplyTo;
              if (_cursor.isNull(_cursorIndexOfReplyTo)) {
                _tmpReplyTo = null;
              } else {
                _tmpReplyTo = _cursor.getString(_cursorIndexOfReplyTo);
              }
              final boolean _tmpIsEncrypted;
              final int _tmp;
              _tmp = _cursor.getInt(_cursorIndexOfIsEncrypted);
              _tmpIsEncrypted = _tmp != 0;
              final Long _tmpDisappearingMessageTimer;
              if (_cursor.isNull(_cursorIndexOfDisappearingMessageTimer)) {
                _tmpDisappearingMessageTimer = null;
              } else {
                _tmpDisappearingMessageTimer = _cursor.getLong(_cursorIndexOfDisappearingMessageTimer);
              }
              final Long _tmpExpiresAt;
              if (_cursor.isNull(_cursorIndexOfExpiresAt)) {
                _tmpExpiresAt = null;
              } else {
                _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
              }
              final boolean _tmpIsDisappearing;
              final int _tmp_1;
              _tmp_1 = _cursor.getInt(_cursorIndexOfIsDisappearing);
              _tmpIsDisappearing = _tmp_1 != 0;
              final long _tmpCreatedAt;
              _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
              final long _tmpUpdatedAt;
              _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
              _tmpMessage = new MessageEntity(_tmpId,_tmpChatId,_tmpSenderId,_tmpContent,_tmpType,_tmpTimestamp,_tmpStatus,_tmpReplyTo,_tmpIsEncrypted,_tmpDisappearingMessageTimer,_tmpExpiresAt,_tmpIsDisappearing,_tmpCreatedAt,_tmpUpdatedAt);
              final ArrayList<ReactionEntity> _tmpReactionsCollection;
              final String _tmpKey_1;
              _tmpKey_1 = _cursor.getString(_cursorIndexOfId);
              _tmpReactionsCollection = _collectionReactions.get(_tmpKey_1);
              _result = new MessageWithReactions(_tmpMessage,_tmpReactionsCollection);
            } else {
              _result = null;
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
            _statement.release();
          }
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MessageWithReactions>> observeMessagesWithReactionsByChatId(
      final String chatId) {
    final String _sql = "SELECT * FROM messages WHERE chatId = ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, chatId);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"reactions",
        "messages"}, new Callable<List<MessageWithReactions>>() {
      @Override
      @NonNull
      public List<MessageWithReactions> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfChatId = CursorUtil.getColumnIndexOrThrow(_cursor, "chatId");
            final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
            final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
            final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
            final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
            final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
            final int _cursorIndexOfReplyTo = CursorUtil.getColumnIndexOrThrow(_cursor, "replyTo");
            final int _cursorIndexOfIsEncrypted = CursorUtil.getColumnIndexOrThrow(_cursor, "isEncrypted");
            final int _cursorIndexOfDisappearingMessageTimer = CursorUtil.getColumnIndexOrThrow(_cursor, "disappearingMessageTimer");
            final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
            final int _cursorIndexOfIsDisappearing = CursorUtil.getColumnIndexOrThrow(_cursor, "isDisappearing");
            final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
            final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
            final ArrayMap<String, ArrayList<ReactionEntity>> _collectionReactions = new ArrayMap<String, ArrayList<ReactionEntity>>();
            while (_cursor.moveToNext()) {
              final String _tmpKey;
              _tmpKey = _cursor.getString(_cursorIndexOfId);
              if (!_collectionReactions.containsKey(_tmpKey)) {
                _collectionReactions.put(_tmpKey, new ArrayList<ReactionEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipreactionsAscomChainMessagingDataLocalEntityReactionEntity(_collectionReactions);
            final List<MessageWithReactions> _result = new ArrayList<MessageWithReactions>(_cursor.getCount());
            while (_cursor.moveToNext()) {
              final MessageWithReactions _item;
              final MessageEntity _tmpMessage;
              final String _tmpId;
              _tmpId = _cursor.getString(_cursorIndexOfId);
              final String _tmpChatId;
              _tmpChatId = _cursor.getString(_cursorIndexOfChatId);
              final String _tmpSenderId;
              _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
              final String _tmpContent;
              _tmpContent = _cursor.getString(_cursorIndexOfContent);
              final String _tmpType;
              _tmpType = _cursor.getString(_cursorIndexOfType);
              final long _tmpTimestamp;
              _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
              final String _tmpStatus;
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
              final String _tmpReplyTo;
              if (_cursor.isNull(_cursorIndexOfReplyTo)) {
                _tmpReplyTo = null;
              } else {
                _tmpReplyTo = _cursor.getString(_cursorIndexOfReplyTo);
              }
              final boolean _tmpIsEncrypted;
              final int _tmp;
              _tmp = _cursor.getInt(_cursorIndexOfIsEncrypted);
              _tmpIsEncrypted = _tmp != 0;
              final Long _tmpDisappearingMessageTimer;
              if (_cursor.isNull(_cursorIndexOfDisappearingMessageTimer)) {
                _tmpDisappearingMessageTimer = null;
              } else {
                _tmpDisappearingMessageTimer = _cursor.getLong(_cursorIndexOfDisappearingMessageTimer);
              }
              final Long _tmpExpiresAt;
              if (_cursor.isNull(_cursorIndexOfExpiresAt)) {
                _tmpExpiresAt = null;
              } else {
                _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
              }
              final boolean _tmpIsDisappearing;
              final int _tmp_1;
              _tmp_1 = _cursor.getInt(_cursorIndexOfIsDisappearing);
              _tmpIsDisappearing = _tmp_1 != 0;
              final long _tmpCreatedAt;
              _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
              final long _tmpUpdatedAt;
              _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
              _tmpMessage = new MessageEntity(_tmpId,_tmpChatId,_tmpSenderId,_tmpContent,_tmpType,_tmpTimestamp,_tmpStatus,_tmpReplyTo,_tmpIsEncrypted,_tmpDisappearingMessageTimer,_tmpExpiresAt,_tmpIsDisappearing,_tmpCreatedAt,_tmpUpdatedAt);
              final ArrayList<ReactionEntity> _tmpReactionsCollection;
              final String _tmpKey_1;
              _tmpKey_1 = _cursor.getString(_cursorIndexOfId);
              _tmpReactionsCollection = _collectionReactions.get(_tmpKey_1);
              _item = new MessageWithReactions(_tmpMessage,_tmpReactionsCollection);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object searchMessagesWithReactions(final String query,
      final Continuation<? super List<MessageWithReactions>> $completion) {
    final String _sql = "SELECT * FROM messages WHERE content LIKE '%' || ? || '%' ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, true, _cancellationSignal, new Callable<List<MessageWithReactions>>() {
      @Override
      @NonNull
      public List<MessageWithReactions> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfChatId = CursorUtil.getColumnIndexOrThrow(_cursor, "chatId");
            final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
            final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
            final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
            final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
            final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
            final int _cursorIndexOfReplyTo = CursorUtil.getColumnIndexOrThrow(_cursor, "replyTo");
            final int _cursorIndexOfIsEncrypted = CursorUtil.getColumnIndexOrThrow(_cursor, "isEncrypted");
            final int _cursorIndexOfDisappearingMessageTimer = CursorUtil.getColumnIndexOrThrow(_cursor, "disappearingMessageTimer");
            final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
            final int _cursorIndexOfIsDisappearing = CursorUtil.getColumnIndexOrThrow(_cursor, "isDisappearing");
            final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
            final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
            final ArrayMap<String, ArrayList<ReactionEntity>> _collectionReactions = new ArrayMap<String, ArrayList<ReactionEntity>>();
            while (_cursor.moveToNext()) {
              final String _tmpKey;
              _tmpKey = _cursor.getString(_cursorIndexOfId);
              if (!_collectionReactions.containsKey(_tmpKey)) {
                _collectionReactions.put(_tmpKey, new ArrayList<ReactionEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipreactionsAscomChainMessagingDataLocalEntityReactionEntity(_collectionReactions);
            final List<MessageWithReactions> _result = new ArrayList<MessageWithReactions>(_cursor.getCount());
            while (_cursor.moveToNext()) {
              final MessageWithReactions _item;
              final MessageEntity _tmpMessage;
              final String _tmpId;
              _tmpId = _cursor.getString(_cursorIndexOfId);
              final String _tmpChatId;
              _tmpChatId = _cursor.getString(_cursorIndexOfChatId);
              final String _tmpSenderId;
              _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
              final String _tmpContent;
              _tmpContent = _cursor.getString(_cursorIndexOfContent);
              final String _tmpType;
              _tmpType = _cursor.getString(_cursorIndexOfType);
              final long _tmpTimestamp;
              _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
              final String _tmpStatus;
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
              final String _tmpReplyTo;
              if (_cursor.isNull(_cursorIndexOfReplyTo)) {
                _tmpReplyTo = null;
              } else {
                _tmpReplyTo = _cursor.getString(_cursorIndexOfReplyTo);
              }
              final boolean _tmpIsEncrypted;
              final int _tmp;
              _tmp = _cursor.getInt(_cursorIndexOfIsEncrypted);
              _tmpIsEncrypted = _tmp != 0;
              final Long _tmpDisappearingMessageTimer;
              if (_cursor.isNull(_cursorIndexOfDisappearingMessageTimer)) {
                _tmpDisappearingMessageTimer = null;
              } else {
                _tmpDisappearingMessageTimer = _cursor.getLong(_cursorIndexOfDisappearingMessageTimer);
              }
              final Long _tmpExpiresAt;
              if (_cursor.isNull(_cursorIndexOfExpiresAt)) {
                _tmpExpiresAt = null;
              } else {
                _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
              }
              final boolean _tmpIsDisappearing;
              final int _tmp_1;
              _tmp_1 = _cursor.getInt(_cursorIndexOfIsDisappearing);
              _tmpIsDisappearing = _tmp_1 != 0;
              final long _tmpCreatedAt;
              _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
              final long _tmpUpdatedAt;
              _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
              _tmpMessage = new MessageEntity(_tmpId,_tmpChatId,_tmpSenderId,_tmpContent,_tmpType,_tmpTimestamp,_tmpStatus,_tmpReplyTo,_tmpIsEncrypted,_tmpDisappearingMessageTimer,_tmpExpiresAt,_tmpIsDisappearing,_tmpCreatedAt,_tmpUpdatedAt);
              final ArrayList<ReactionEntity> _tmpReactionsCollection;
              final String _tmpKey_1;
              _tmpKey_1 = _cursor.getString(_cursorIndexOfId);
              _tmpReactionsCollection = _collectionReactions.get(_tmpKey_1);
              _item = new MessageWithReactions(_tmpMessage,_tmpReactionsCollection);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
            _statement.release();
          }
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMessageStatus(final List<String> messageIds, final String status,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("UPDATE messages SET status = ");
        _stringBuilder.append("?");
        _stringBuilder.append(" WHERE id IN (");
        final int _inputSize = messageIds.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        _stmt.bindString(_argIndex, status);
        _argIndex = 2;
        for (String _item : messageIds) {
          _stmt.bindString(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMessagesByIds(final List<String> messageIds,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM messages WHERE id IN (");
        final int _inputSize = messageIds.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (String _item : messageIds) {
          _stmt.bindString(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private void __fetchRelationshipreactionsAscomChainMessagingDataLocalEntityReactionEntity(
      @NonNull final ArrayMap<String, ArrayList<ReactionEntity>> _map) {
    final Set<String> __mapKeySet = _map.keySet();
    if (__mapKeySet.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchArrayMap(_map, true, (map) -> {
        __fetchRelationshipreactionsAscomChainMessagingDataLocalEntityReactionEntity(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `id`,`messageId`,`userId`,`emoji`,`timestamp`,`createdAt` FROM `reactions` WHERE `messageId` IN (");
    final int _inputSize = __mapKeySet.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (String _item : __mapKeySet) {
      _stmt.bindString(_argIndex, _item);
      _argIndex++;
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      final int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "messageId");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfMessageId = 1;
      final int _cursorIndexOfUserId = 2;
      final int _cursorIndexOfEmoji = 3;
      final int _cursorIndexOfTimestamp = 4;
      final int _cursorIndexOfCreatedAt = 5;
      while (_cursor.moveToNext()) {
        final String _tmpKey;
        _tmpKey = _cursor.getString(_itemKeyIndex);
        final ArrayList<ReactionEntity> _tmpRelation = _map.get(_tmpKey);
        if (_tmpRelation != null) {
          final ReactionEntity _item_1;
          final String _tmpId;
          _tmpId = _cursor.getString(_cursorIndexOfId);
          final String _tmpMessageId;
          _tmpMessageId = _cursor.getString(_cursorIndexOfMessageId);
          final String _tmpUserId;
          _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
          final String _tmpEmoji;
          _tmpEmoji = _cursor.getString(_cursorIndexOfEmoji);
          final long _tmpTimestamp;
          _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
          final long _tmpCreatedAt;
          _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
          _item_1 = new ReactionEntity(_tmpId,_tmpMessageId,_tmpUserId,_tmpEmoji,_tmpTimestamp,_tmpCreatedAt);
          _tmpRelation.add(_item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }
}
