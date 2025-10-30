package com.chain.messaging.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.chain.messaging.data.local.Converters;
import com.chain.messaging.data.local.entity.SecurityEventEntity;
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

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SecurityEventDao_Impl implements SecurityEventDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SecurityEventEntity> __insertionAdapterOfSecurityEventEntity;

  private final Converters __converters = new Converters();

  private final SharedSQLiteStatement __preparedStmtOfAcknowledgeEvent;

  private final SharedSQLiteStatement __preparedStmtOfDeleteEventsOlderThan;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllEvents;

  public SecurityEventDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSecurityEventEntity = new EntityInsertionAdapter<SecurityEventEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `security_events` (`id`,`type`,`timestamp`,`severity`,`description`,`metadata`,`userId`,`deviceId`,`ipAddress`,`isAcknowledged`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SecurityEventEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getType());
        final String _tmp = __converters.fromLocalDateTime(entity.getTimestamp());
        if (_tmp == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, _tmp);
        }
        statement.bindString(4, entity.getSeverity());
        statement.bindString(5, entity.getDescription());
        statement.bindString(6, entity.getMetadata());
        if (entity.getUserId() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getUserId());
        }
        if (entity.getDeviceId() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getDeviceId());
        }
        if (entity.getIpAddress() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getIpAddress());
        }
        final int _tmp_1 = entity.isAcknowledged() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
      }
    };
    this.__preparedStmtOfAcknowledgeEvent = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE security_events SET isAcknowledged = 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteEventsOlderThan = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM security_events WHERE timestamp < ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllEvents = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM security_events";
        return _query;
      }
    };
  }

  @Override
  public Object insertEvent(final SecurityEventEntity event,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSecurityEventEntity.insert(event);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object acknowledgeEvent(final String eventId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfAcknowledgeEvent.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, eventId);
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
          __preparedStmtOfAcknowledgeEvent.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteEventsOlderThan(final LocalDateTime cutoffDate,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteEventsOlderThan.acquire();
        int _argIndex = 1;
        final String _tmp = __converters.fromLocalDateTime(cutoffDate);
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
          __preparedStmtOfDeleteEventsOlderThan.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllEvents(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllEvents.acquire();
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
          __preparedStmtOfDeleteAllEvents.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllEvents(final Continuation<? super List<SecurityEventEntity>> $completion) {
    final String _sql = "SELECT * FROM security_events ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SecurityEventEntity>>() {
      @Override
      @NonNull
      public List<SecurityEventEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfSeverity = CursorUtil.getColumnIndexOrThrow(_cursor, "severity");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfMetadata = CursorUtil.getColumnIndexOrThrow(_cursor, "metadata");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfIpAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "ipAddress");
          final int _cursorIndexOfIsAcknowledged = CursorUtil.getColumnIndexOrThrow(_cursor, "isAcknowledged");
          final List<SecurityEventEntity> _result = new ArrayList<SecurityEventEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SecurityEventEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final LocalDateTime _tmpTimestamp;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfTimestamp)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfTimestamp);
            }
            final LocalDateTime _tmp_1 = __converters.toLocalDateTime(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpTimestamp = _tmp_1;
            }
            final String _tmpSeverity;
            _tmpSeverity = _cursor.getString(_cursorIndexOfSeverity);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpMetadata;
            _tmpMetadata = _cursor.getString(_cursorIndexOfMetadata);
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final String _tmpDeviceId;
            if (_cursor.isNull(_cursorIndexOfDeviceId)) {
              _tmpDeviceId = null;
            } else {
              _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            }
            final String _tmpIpAddress;
            if (_cursor.isNull(_cursorIndexOfIpAddress)) {
              _tmpIpAddress = null;
            } else {
              _tmpIpAddress = _cursor.getString(_cursorIndexOfIpAddress);
            }
            final boolean _tmpIsAcknowledged;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsAcknowledged);
            _tmpIsAcknowledged = _tmp_2 != 0;
            _item = new SecurityEventEntity(_tmpId,_tmpType,_tmpTimestamp,_tmpSeverity,_tmpDescription,_tmpMetadata,_tmpUserId,_tmpDeviceId,_tmpIpAddress,_tmpIsAcknowledged);
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
  public Object getEventsInTimeRange(final LocalDateTime start, final LocalDateTime end,
      final Continuation<? super List<SecurityEventEntity>> $completion) {
    final String _sql = "SELECT * FROM security_events WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    final String _tmp = __converters.fromLocalDateTime(start);
    if (_tmp == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, _tmp);
    }
    _argIndex = 2;
    final String _tmp_1 = __converters.fromLocalDateTime(end);
    if (_tmp_1 == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, _tmp_1);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SecurityEventEntity>>() {
      @Override
      @NonNull
      public List<SecurityEventEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfSeverity = CursorUtil.getColumnIndexOrThrow(_cursor, "severity");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfMetadata = CursorUtil.getColumnIndexOrThrow(_cursor, "metadata");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfIpAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "ipAddress");
          final int _cursorIndexOfIsAcknowledged = CursorUtil.getColumnIndexOrThrow(_cursor, "isAcknowledged");
          final List<SecurityEventEntity> _result = new ArrayList<SecurityEventEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SecurityEventEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final LocalDateTime _tmpTimestamp;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfTimestamp)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfTimestamp);
            }
            final LocalDateTime _tmp_3 = __converters.toLocalDateTime(_tmp_2);
            if (_tmp_3 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpTimestamp = _tmp_3;
            }
            final String _tmpSeverity;
            _tmpSeverity = _cursor.getString(_cursorIndexOfSeverity);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpMetadata;
            _tmpMetadata = _cursor.getString(_cursorIndexOfMetadata);
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final String _tmpDeviceId;
            if (_cursor.isNull(_cursorIndexOfDeviceId)) {
              _tmpDeviceId = null;
            } else {
              _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            }
            final String _tmpIpAddress;
            if (_cursor.isNull(_cursorIndexOfIpAddress)) {
              _tmpIpAddress = null;
            } else {
              _tmpIpAddress = _cursor.getString(_cursorIndexOfIpAddress);
            }
            final boolean _tmpIsAcknowledged;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsAcknowledged);
            _tmpIsAcknowledged = _tmp_4 != 0;
            _item = new SecurityEventEntity(_tmpId,_tmpType,_tmpTimestamp,_tmpSeverity,_tmpDescription,_tmpMetadata,_tmpUserId,_tmpDeviceId,_tmpIpAddress,_tmpIsAcknowledged);
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
  public Object getEventsByType(final String type,
      final Continuation<? super List<SecurityEventEntity>> $completion) {
    final String _sql = "SELECT * FROM security_events WHERE type = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, type);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SecurityEventEntity>>() {
      @Override
      @NonNull
      public List<SecurityEventEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfSeverity = CursorUtil.getColumnIndexOrThrow(_cursor, "severity");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfMetadata = CursorUtil.getColumnIndexOrThrow(_cursor, "metadata");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfIpAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "ipAddress");
          final int _cursorIndexOfIsAcknowledged = CursorUtil.getColumnIndexOrThrow(_cursor, "isAcknowledged");
          final List<SecurityEventEntity> _result = new ArrayList<SecurityEventEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SecurityEventEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final LocalDateTime _tmpTimestamp;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfTimestamp)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfTimestamp);
            }
            final LocalDateTime _tmp_1 = __converters.toLocalDateTime(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpTimestamp = _tmp_1;
            }
            final String _tmpSeverity;
            _tmpSeverity = _cursor.getString(_cursorIndexOfSeverity);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpMetadata;
            _tmpMetadata = _cursor.getString(_cursorIndexOfMetadata);
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final String _tmpDeviceId;
            if (_cursor.isNull(_cursorIndexOfDeviceId)) {
              _tmpDeviceId = null;
            } else {
              _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            }
            final String _tmpIpAddress;
            if (_cursor.isNull(_cursorIndexOfIpAddress)) {
              _tmpIpAddress = null;
            } else {
              _tmpIpAddress = _cursor.getString(_cursorIndexOfIpAddress);
            }
            final boolean _tmpIsAcknowledged;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsAcknowledged);
            _tmpIsAcknowledged = _tmp_2 != 0;
            _item = new SecurityEventEntity(_tmpId,_tmpType,_tmpTimestamp,_tmpSeverity,_tmpDescription,_tmpMetadata,_tmpUserId,_tmpDeviceId,_tmpIpAddress,_tmpIsAcknowledged);
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
  public Object getEventsBySeverity(final String severity,
      final Continuation<? super List<SecurityEventEntity>> $completion) {
    final String _sql = "SELECT * FROM security_events WHERE severity = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, severity);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SecurityEventEntity>>() {
      @Override
      @NonNull
      public List<SecurityEventEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfSeverity = CursorUtil.getColumnIndexOrThrow(_cursor, "severity");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfMetadata = CursorUtil.getColumnIndexOrThrow(_cursor, "metadata");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfIpAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "ipAddress");
          final int _cursorIndexOfIsAcknowledged = CursorUtil.getColumnIndexOrThrow(_cursor, "isAcknowledged");
          final List<SecurityEventEntity> _result = new ArrayList<SecurityEventEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SecurityEventEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final LocalDateTime _tmpTimestamp;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfTimestamp)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfTimestamp);
            }
            final LocalDateTime _tmp_1 = __converters.toLocalDateTime(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpTimestamp = _tmp_1;
            }
            final String _tmpSeverity;
            _tmpSeverity = _cursor.getString(_cursorIndexOfSeverity);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpMetadata;
            _tmpMetadata = _cursor.getString(_cursorIndexOfMetadata);
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final String _tmpDeviceId;
            if (_cursor.isNull(_cursorIndexOfDeviceId)) {
              _tmpDeviceId = null;
            } else {
              _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            }
            final String _tmpIpAddress;
            if (_cursor.isNull(_cursorIndexOfIpAddress)) {
              _tmpIpAddress = null;
            } else {
              _tmpIpAddress = _cursor.getString(_cursorIndexOfIpAddress);
            }
            final boolean _tmpIsAcknowledged;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsAcknowledged);
            _tmpIsAcknowledged = _tmp_2 != 0;
            _item = new SecurityEventEntity(_tmpId,_tmpType,_tmpTimestamp,_tmpSeverity,_tmpDescription,_tmpMetadata,_tmpUserId,_tmpDeviceId,_tmpIpAddress,_tmpIsAcknowledged);
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
  public Object getEventCountByType(final Continuation<? super List<EventTypeCount>> $completion) {
    final String _sql = "SELECT type, COUNT(*) as count FROM security_events GROUP BY type";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<EventTypeCount>>() {
      @Override
      @NonNull
      public List<EventTypeCount> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfType = 0;
          final int _cursorIndexOfCount = 1;
          final List<EventTypeCount> _result = new ArrayList<EventTypeCount>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EventTypeCount _item;
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final int _tmpCount;
            _tmpCount = _cursor.getInt(_cursorIndexOfCount);
            _item = new EventTypeCount(_tmpType,_tmpCount);
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
  public Object getActiveHighSeverityEvents(
      final Continuation<? super List<SecurityEventEntity>> $completion) {
    final String _sql = "SELECT * FROM security_events WHERE severity IN ('HIGH', 'CRITICAL') AND isAcknowledged = 0 ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SecurityEventEntity>>() {
      @Override
      @NonNull
      public List<SecurityEventEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfSeverity = CursorUtil.getColumnIndexOrThrow(_cursor, "severity");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfMetadata = CursorUtil.getColumnIndexOrThrow(_cursor, "metadata");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfIpAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "ipAddress");
          final int _cursorIndexOfIsAcknowledged = CursorUtil.getColumnIndexOrThrow(_cursor, "isAcknowledged");
          final List<SecurityEventEntity> _result = new ArrayList<SecurityEventEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SecurityEventEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final LocalDateTime _tmpTimestamp;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfTimestamp)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfTimestamp);
            }
            final LocalDateTime _tmp_1 = __converters.toLocalDateTime(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpTimestamp = _tmp_1;
            }
            final String _tmpSeverity;
            _tmpSeverity = _cursor.getString(_cursorIndexOfSeverity);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpMetadata;
            _tmpMetadata = _cursor.getString(_cursorIndexOfMetadata);
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final String _tmpDeviceId;
            if (_cursor.isNull(_cursorIndexOfDeviceId)) {
              _tmpDeviceId = null;
            } else {
              _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            }
            final String _tmpIpAddress;
            if (_cursor.isNull(_cursorIndexOfIpAddress)) {
              _tmpIpAddress = null;
            } else {
              _tmpIpAddress = _cursor.getString(_cursorIndexOfIpAddress);
            }
            final boolean _tmpIsAcknowledged;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsAcknowledged);
            _tmpIsAcknowledged = _tmp_2 != 0;
            _item = new SecurityEventEntity(_tmpId,_tmpType,_tmpTimestamp,_tmpSeverity,_tmpDescription,_tmpMetadata,_tmpUserId,_tmpDeviceId,_tmpIpAddress,_tmpIsAcknowledged);
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
  public Object getEventCountSince(final LocalDateTime since,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM security_events WHERE timestamp > ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    final String _tmp = __converters.fromLocalDateTime(since);
    if (_tmp == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, _tmp);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(0);
            _result = _tmp_1;
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
