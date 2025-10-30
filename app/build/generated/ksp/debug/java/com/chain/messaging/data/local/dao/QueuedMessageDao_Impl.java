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
import com.chain.messaging.data.local.Converters;
import com.chain.messaging.data.local.entity.QueuedMessageEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalStateException;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.time.LocalDateTime;
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
public final class QueuedMessageDao_Impl implements QueuedMessageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<QueuedMessageEntity> __insertionAdapterOfQueuedMessageEntity;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<QueuedMessageEntity> __updateAdapterOfQueuedMessageEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteQueuedMessage;

  private final SharedSQLiteStatement __preparedStmtOfDeleteQueuedMessagesByChatId;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllQueuedMessages;

  private final SharedSQLiteStatement __preparedStmtOfDeleteFailedMessages;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOldQueuedMessages;

  public QueuedMessageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfQueuedMessageEntity = new EntityInsertionAdapter<QueuedMessageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `queued_messages` (`id`,`messageId`,`chatId`,`senderId`,`content`,`messageType`,`queuedAt`,`retryCount`,`lastRetryAt`,`priority`,`maxRetries`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final QueuedMessageEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getMessageId());
        statement.bindString(3, entity.getChatId());
        statement.bindString(4, entity.getSenderId());
        statement.bindString(5, entity.getContent());
        statement.bindString(6, entity.getMessageType());
        final String _tmp = __converters.fromLocalDateTime(entity.getQueuedAt());
        if (_tmp == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, _tmp);
        }
        statement.bindLong(8, entity.getRetryCount());
        final String _tmp_1 = __converters.fromLocalDateTime(entity.getLastRetryAt());
        if (_tmp_1 == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, _tmp_1);
        }
        statement.bindString(10, entity.getPriority());
        statement.bindLong(11, entity.getMaxRetries());
      }
    };
    this.__updateAdapterOfQueuedMessageEntity = new EntityDeletionOrUpdateAdapter<QueuedMessageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `queued_messages` SET `id` = ?,`messageId` = ?,`chatId` = ?,`senderId` = ?,`content` = ?,`messageType` = ?,`queuedAt` = ?,`retryCount` = ?,`lastRetryAt` = ?,`priority` = ?,`maxRetries` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final QueuedMessageEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getMessageId());
        statement.bindString(3, entity.getChatId());
        statement.bindString(4, entity.getSenderId());
        statement.bindString(5, entity.getContent());
        statement.bindString(6, entity.getMessageType());
        final String _tmp = __converters.fromLocalDateTime(entity.getQueuedAt());
        if (_tmp == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, _tmp);
        }
        statement.bindLong(8, entity.getRetryCount());
        final String _tmp_1 = __converters.fromLocalDateTime(entity.getLastRetryAt());
        if (_tmp_1 == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, _tmp_1);
        }
        statement.bindString(10, entity.getPriority());
        statement.bindLong(11, entity.getMaxRetries());
        statement.bindString(12, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteQueuedMessage = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM queued_messages WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteQueuedMessagesByChatId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM queued_messages WHERE chatId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllQueuedMessages = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM queued_messages";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteFailedMessages = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM queued_messages WHERE retryCount >= maxRetries";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteOldQueuedMessages = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM queued_messages WHERE queuedAt < ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertQueuedMessage(final QueuedMessageEntity queuedMessage,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfQueuedMessageEntity.insert(queuedMessage);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertQueuedMessages(final List<QueuedMessageEntity> queuedMessages,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfQueuedMessageEntity.insert(queuedMessages);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateQueuedMessage(final QueuedMessageEntity queuedMessage,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfQueuedMessageEntity.handle(queuedMessage);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteQueuedMessage(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteQueuedMessage.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfDeleteQueuedMessage.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteQueuedMessagesByChatId(final String chatId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteQueuedMessagesByChatId.acquire();
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
          __preparedStmtOfDeleteQueuedMessagesByChatId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllQueuedMessages(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllQueuedMessages.acquire();
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
          __preparedStmtOfDeleteAllQueuedMessages.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteFailedMessages(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteFailedMessages.acquire();
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
          __preparedStmtOfDeleteFailedMessages.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteOldQueuedMessages(final LocalDateTime cutoffTime,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOldQueuedMessages.acquire();
        int _argIndex = 1;
        final String _tmp = __converters.fromLocalDateTime(cutoffTime);
        if (_tmp == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, _tmp);
        }
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
          __preparedStmtOfDeleteOldQueuedMessages.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllQueuedMessages(
      final Continuation<? super List<QueuedMessageEntity>> $completion) {
    final String _sql = "SELECT * FROM queued_messages ORDER BY priority ASC, queuedAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<QueuedMessageEntity>>() {
      @Override
      @NonNull
      public List<QueuedMessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "messageId");
          final int _cursorIndexOfChatId = CursorUtil.getColumnIndexOrThrow(_cursor, "chatId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfQueuedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "queuedAt");
          final int _cursorIndexOfRetryCount = CursorUtil.getColumnIndexOrThrow(_cursor, "retryCount");
          final int _cursorIndexOfLastRetryAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastRetryAt");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfMaxRetries = CursorUtil.getColumnIndexOrThrow(_cursor, "maxRetries");
          final List<QueuedMessageEntity> _result = new ArrayList<QueuedMessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final QueuedMessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpMessageId;
            _tmpMessageId = _cursor.getString(_cursorIndexOfMessageId);
            final String _tmpChatId;
            _tmpChatId = _cursor.getString(_cursorIndexOfChatId);
            final String _tmpSenderId;
            _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpMessageType;
            _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            final LocalDateTime _tmpQueuedAt;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfQueuedAt)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfQueuedAt);
            }
            final LocalDateTime _tmp_1 = __converters.toLocalDateTime(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpQueuedAt = _tmp_1;
            }
            final int _tmpRetryCount;
            _tmpRetryCount = _cursor.getInt(_cursorIndexOfRetryCount);
            final LocalDateTime _tmpLastRetryAt;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfLastRetryAt)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfLastRetryAt);
            }
            _tmpLastRetryAt = __converters.toLocalDateTime(_tmp_2);
            final String _tmpPriority;
            _tmpPriority = _cursor.getString(_cursorIndexOfPriority);
            final int _tmpMaxRetries;
            _tmpMaxRetries = _cursor.getInt(_cursorIndexOfMaxRetries);
            _item = new QueuedMessageEntity(_tmpId,_tmpMessageId,_tmpChatId,_tmpSenderId,_tmpContent,_tmpMessageType,_tmpQueuedAt,_tmpRetryCount,_tmpLastRetryAt,_tmpPriority,_tmpMaxRetries);
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
  public Flow<List<QueuedMessageEntity>> getAllQueuedMessagesFlow() {
    final String _sql = "SELECT * FROM queued_messages ORDER BY priority ASC, queuedAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"queued_messages"}, new Callable<List<QueuedMessageEntity>>() {
      @Override
      @NonNull
      public List<QueuedMessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "messageId");
          final int _cursorIndexOfChatId = CursorUtil.getColumnIndexOrThrow(_cursor, "chatId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfQueuedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "queuedAt");
          final int _cursorIndexOfRetryCount = CursorUtil.getColumnIndexOrThrow(_cursor, "retryCount");
          final int _cursorIndexOfLastRetryAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastRetryAt");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfMaxRetries = CursorUtil.getColumnIndexOrThrow(_cursor, "maxRetries");
          final List<QueuedMessageEntity> _result = new ArrayList<QueuedMessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final QueuedMessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpMessageId;
            _tmpMessageId = _cursor.getString(_cursorIndexOfMessageId);
            final String _tmpChatId;
            _tmpChatId = _cursor.getString(_cursorIndexOfChatId);
            final String _tmpSenderId;
            _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpMessageType;
            _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            final LocalDateTime _tmpQueuedAt;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfQueuedAt)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfQueuedAt);
            }
            final LocalDateTime _tmp_1 = __converters.toLocalDateTime(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpQueuedAt = _tmp_1;
            }
            final int _tmpRetryCount;
            _tmpRetryCount = _cursor.getInt(_cursorIndexOfRetryCount);
            final LocalDateTime _tmpLastRetryAt;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfLastRetryAt)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfLastRetryAt);
            }
            _tmpLastRetryAt = __converters.toLocalDateTime(_tmp_2);
            final String _tmpPriority;
            _tmpPriority = _cursor.getString(_cursorIndexOfPriority);
            final int _tmpMaxRetries;
            _tmpMaxRetries = _cursor.getInt(_cursorIndexOfMaxRetries);
            _item = new QueuedMessageEntity(_tmpId,_tmpMessageId,_tmpChatId,_tmpSenderId,_tmpContent,_tmpMessageType,_tmpQueuedAt,_tmpRetryCount,_tmpLastRetryAt,_tmpPriority,_tmpMaxRetries);
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
  public Object getQueuedMessageById(final String id,
      final Continuation<? super QueuedMessageEntity> $completion) {
    final String _sql = "SELECT * FROM queued_messages WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<QueuedMessageEntity>() {
      @Override
      @Nullable
      public QueuedMessageEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "messageId");
          final int _cursorIndexOfChatId = CursorUtil.getColumnIndexOrThrow(_cursor, "chatId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfQueuedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "queuedAt");
          final int _cursorIndexOfRetryCount = CursorUtil.getColumnIndexOrThrow(_cursor, "retryCount");
          final int _cursorIndexOfLastRetryAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastRetryAt");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfMaxRetries = CursorUtil.getColumnIndexOrThrow(_cursor, "maxRetries");
          final QueuedMessageEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpMessageId;
            _tmpMessageId = _cursor.getString(_cursorIndexOfMessageId);
            final String _tmpChatId;
            _tmpChatId = _cursor.getString(_cursorIndexOfChatId);
            final String _tmpSenderId;
            _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpMessageType;
            _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            final LocalDateTime _tmpQueuedAt;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfQueuedAt)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfQueuedAt);
            }
            final LocalDateTime _tmp_1 = __converters.toLocalDateTime(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpQueuedAt = _tmp_1;
            }
            final int _tmpRetryCount;
            _tmpRetryCount = _cursor.getInt(_cursorIndexOfRetryCount);
            final LocalDateTime _tmpLastRetryAt;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfLastRetryAt)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfLastRetryAt);
            }
            _tmpLastRetryAt = __converters.toLocalDateTime(_tmp_2);
            final String _tmpPriority;
            _tmpPriority = _cursor.getString(_cursorIndexOfPriority);
            final int _tmpMaxRetries;
            _tmpMaxRetries = _cursor.getInt(_cursorIndexOfMaxRetries);
            _result = new QueuedMessageEntity(_tmpId,_tmpMessageId,_tmpChatId,_tmpSenderId,_tmpContent,_tmpMessageType,_tmpQueuedAt,_tmpRetryCount,_tmpLastRetryAt,_tmpPriority,_tmpMaxRetries);
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
  public Object getQueuedMessagesByChatId(final String chatId,
      final Continuation<? super List<QueuedMessageEntity>> $completion) {
    final String _sql = "SELECT * FROM queued_messages WHERE chatId = ? ORDER BY queuedAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, chatId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<QueuedMessageEntity>>() {
      @Override
      @NonNull
      public List<QueuedMessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "messageId");
          final int _cursorIndexOfChatId = CursorUtil.getColumnIndexOrThrow(_cursor, "chatId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfQueuedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "queuedAt");
          final int _cursorIndexOfRetryCount = CursorUtil.getColumnIndexOrThrow(_cursor, "retryCount");
          final int _cursorIndexOfLastRetryAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastRetryAt");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfMaxRetries = CursorUtil.getColumnIndexOrThrow(_cursor, "maxRetries");
          final List<QueuedMessageEntity> _result = new ArrayList<QueuedMessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final QueuedMessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpMessageId;
            _tmpMessageId = _cursor.getString(_cursorIndexOfMessageId);
            final String _tmpChatId;
            _tmpChatId = _cursor.getString(_cursorIndexOfChatId);
            final String _tmpSenderId;
            _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpMessageType;
            _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            final LocalDateTime _tmpQueuedAt;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfQueuedAt)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfQueuedAt);
            }
            final LocalDateTime _tmp_1 = __converters.toLocalDateTime(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpQueuedAt = _tmp_1;
            }
            final int _tmpRetryCount;
            _tmpRetryCount = _cursor.getInt(_cursorIndexOfRetryCount);
            final LocalDateTime _tmpLastRetryAt;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfLastRetryAt)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfLastRetryAt);
            }
            _tmpLastRetryAt = __converters.toLocalDateTime(_tmp_2);
            final String _tmpPriority;
            _tmpPriority = _cursor.getString(_cursorIndexOfPriority);
            final int _tmpMaxRetries;
            _tmpMaxRetries = _cursor.getInt(_cursorIndexOfMaxRetries);
            _item = new QueuedMessageEntity(_tmpId,_tmpMessageId,_tmpChatId,_tmpSenderId,_tmpContent,_tmpMessageType,_tmpQueuedAt,_tmpRetryCount,_tmpLastRetryAt,_tmpPriority,_tmpMaxRetries);
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
  public Object getQueueSize(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM queued_messages";
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

  @Override
  public Object getFailedMessageCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM queued_messages WHERE retryCount >= maxRetries";
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
