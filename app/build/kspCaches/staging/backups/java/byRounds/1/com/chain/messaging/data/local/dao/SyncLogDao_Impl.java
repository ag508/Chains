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
import com.chain.messaging.data.local.entity.SyncLogEntity;
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
public final class SyncLogDao_Impl implements SyncLogDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SyncLogEntity> __insertionAdapterOfSyncLogEntity;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<SyncLogEntity> __updateAdapterOfSyncLogEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOldSyncLogs;

  public SyncLogDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSyncLogEntity = new EntityInsertionAdapter<SyncLogEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `sync_logs` (`id`,`deviceId`,`syncType`,`status`,`startTime`,`endTime`,`messagesSynced`,`keysSynced`,`settingsSynced`,`errorMessage`,`retryCount`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SyncLogEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getDeviceId());
        statement.bindString(3, entity.getSyncType());
        statement.bindString(4, entity.getStatus());
        final String _tmp = __converters.fromLocalDateTime(entity.getStartTime());
        if (_tmp == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, _tmp);
        }
        final String _tmp_1 = __converters.fromLocalDateTime(entity.getEndTime());
        if (_tmp_1 == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, _tmp_1);
        }
        statement.bindLong(7, entity.getMessagesSynced());
        statement.bindLong(8, entity.getKeysSynced());
        statement.bindLong(9, entity.getSettingsSynced());
        if (entity.getErrorMessage() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getErrorMessage());
        }
        statement.bindLong(11, entity.getRetryCount());
      }
    };
    this.__updateAdapterOfSyncLogEntity = new EntityDeletionOrUpdateAdapter<SyncLogEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `sync_logs` SET `id` = ?,`deviceId` = ?,`syncType` = ?,`status` = ?,`startTime` = ?,`endTime` = ?,`messagesSynced` = ?,`keysSynced` = ?,`settingsSynced` = ?,`errorMessage` = ?,`retryCount` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SyncLogEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getDeviceId());
        statement.bindString(3, entity.getSyncType());
        statement.bindString(4, entity.getStatus());
        final String _tmp = __converters.fromLocalDateTime(entity.getStartTime());
        if (_tmp == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, _tmp);
        }
        final String _tmp_1 = __converters.fromLocalDateTime(entity.getEndTime());
        if (_tmp_1 == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, _tmp_1);
        }
        statement.bindLong(7, entity.getMessagesSynced());
        statement.bindLong(8, entity.getKeysSynced());
        statement.bindLong(9, entity.getSettingsSynced());
        if (entity.getErrorMessage() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getErrorMessage());
        }
        statement.bindLong(11, entity.getRetryCount());
        statement.bindString(12, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteOldSyncLogs = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM sync_logs WHERE startTime < ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertSyncLog(final SyncLogEntity syncLog,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSyncLogEntity.insert(syncLog);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSyncLog(final SyncLogEntity syncLog,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSyncLogEntity.handle(syncLog);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteOldSyncLogs(final LocalDateTime cutoffTime,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOldSyncLogs.acquire();
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
          __preparedStmtOfDeleteOldSyncLogs.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getRecentSyncLogs(final int limit,
      final Continuation<? super List<SyncLogEntity>> $completion) {
    final String _sql = "SELECT * FROM sync_logs ORDER BY startTime DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SyncLogEntity>>() {
      @Override
      @NonNull
      public List<SyncLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfSyncType = CursorUtil.getColumnIndexOrThrow(_cursor, "syncType");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfMessagesSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "messagesSynced");
          final int _cursorIndexOfKeysSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "keysSynced");
          final int _cursorIndexOfSettingsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "settingsSynced");
          final int _cursorIndexOfErrorMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "errorMessage");
          final int _cursorIndexOfRetryCount = CursorUtil.getColumnIndexOrThrow(_cursor, "retryCount");
          final List<SyncLogEntity> _result = new ArrayList<SyncLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SyncLogEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpDeviceId;
            _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            final String _tmpSyncType;
            _tmpSyncType = _cursor.getString(_cursorIndexOfSyncType);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final LocalDateTime _tmpStartTime;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfStartTime)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfStartTime);
            }
            final LocalDateTime _tmp_1 = __converters.toLocalDateTime(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpStartTime = _tmp_1;
            }
            final LocalDateTime _tmpEndTime;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfEndTime);
            }
            _tmpEndTime = __converters.toLocalDateTime(_tmp_2);
            final int _tmpMessagesSynced;
            _tmpMessagesSynced = _cursor.getInt(_cursorIndexOfMessagesSynced);
            final int _tmpKeysSynced;
            _tmpKeysSynced = _cursor.getInt(_cursorIndexOfKeysSynced);
            final int _tmpSettingsSynced;
            _tmpSettingsSynced = _cursor.getInt(_cursorIndexOfSettingsSynced);
            final String _tmpErrorMessage;
            if (_cursor.isNull(_cursorIndexOfErrorMessage)) {
              _tmpErrorMessage = null;
            } else {
              _tmpErrorMessage = _cursor.getString(_cursorIndexOfErrorMessage);
            }
            final int _tmpRetryCount;
            _tmpRetryCount = _cursor.getInt(_cursorIndexOfRetryCount);
            _item = new SyncLogEntity(_tmpId,_tmpDeviceId,_tmpSyncType,_tmpStatus,_tmpStartTime,_tmpEndTime,_tmpMessagesSynced,_tmpKeysSynced,_tmpSettingsSynced,_tmpErrorMessage,_tmpRetryCount);
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
  public Object getSyncLogsForDevice(final String deviceId,
      final Continuation<? super List<SyncLogEntity>> $completion) {
    final String _sql = "SELECT * FROM sync_logs WHERE deviceId = ? ORDER BY startTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, deviceId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SyncLogEntity>>() {
      @Override
      @NonNull
      public List<SyncLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfSyncType = CursorUtil.getColumnIndexOrThrow(_cursor, "syncType");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfMessagesSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "messagesSynced");
          final int _cursorIndexOfKeysSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "keysSynced");
          final int _cursorIndexOfSettingsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "settingsSynced");
          final int _cursorIndexOfErrorMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "errorMessage");
          final int _cursorIndexOfRetryCount = CursorUtil.getColumnIndexOrThrow(_cursor, "retryCount");
          final List<SyncLogEntity> _result = new ArrayList<SyncLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SyncLogEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpDeviceId;
            _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            final String _tmpSyncType;
            _tmpSyncType = _cursor.getString(_cursorIndexOfSyncType);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final LocalDateTime _tmpStartTime;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfStartTime)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfStartTime);
            }
            final LocalDateTime _tmp_1 = __converters.toLocalDateTime(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpStartTime = _tmp_1;
            }
            final LocalDateTime _tmpEndTime;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfEndTime);
            }
            _tmpEndTime = __converters.toLocalDateTime(_tmp_2);
            final int _tmpMessagesSynced;
            _tmpMessagesSynced = _cursor.getInt(_cursorIndexOfMessagesSynced);
            final int _tmpKeysSynced;
            _tmpKeysSynced = _cursor.getInt(_cursorIndexOfKeysSynced);
            final int _tmpSettingsSynced;
            _tmpSettingsSynced = _cursor.getInt(_cursorIndexOfSettingsSynced);
            final String _tmpErrorMessage;
            if (_cursor.isNull(_cursorIndexOfErrorMessage)) {
              _tmpErrorMessage = null;
            } else {
              _tmpErrorMessage = _cursor.getString(_cursorIndexOfErrorMessage);
            }
            final int _tmpRetryCount;
            _tmpRetryCount = _cursor.getInt(_cursorIndexOfRetryCount);
            _item = new SyncLogEntity(_tmpId,_tmpDeviceId,_tmpSyncType,_tmpStatus,_tmpStartTime,_tmpEndTime,_tmpMessagesSynced,_tmpKeysSynced,_tmpSettingsSynced,_tmpErrorMessage,_tmpRetryCount);
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
  public Object getFailedSyncs(final Continuation<? super List<SyncLogEntity>> $completion) {
    final String _sql = "SELECT * FROM sync_logs WHERE status = 'ERROR' ORDER BY startTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SyncLogEntity>>() {
      @Override
      @NonNull
      public List<SyncLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfSyncType = CursorUtil.getColumnIndexOrThrow(_cursor, "syncType");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfMessagesSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "messagesSynced");
          final int _cursorIndexOfKeysSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "keysSynced");
          final int _cursorIndexOfSettingsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "settingsSynced");
          final int _cursorIndexOfErrorMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "errorMessage");
          final int _cursorIndexOfRetryCount = CursorUtil.getColumnIndexOrThrow(_cursor, "retryCount");
          final List<SyncLogEntity> _result = new ArrayList<SyncLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SyncLogEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpDeviceId;
            _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            final String _tmpSyncType;
            _tmpSyncType = _cursor.getString(_cursorIndexOfSyncType);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final LocalDateTime _tmpStartTime;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfStartTime)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfStartTime);
            }
            final LocalDateTime _tmp_1 = __converters.toLocalDateTime(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpStartTime = _tmp_1;
            }
            final LocalDateTime _tmpEndTime;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfEndTime);
            }
            _tmpEndTime = __converters.toLocalDateTime(_tmp_2);
            final int _tmpMessagesSynced;
            _tmpMessagesSynced = _cursor.getInt(_cursorIndexOfMessagesSynced);
            final int _tmpKeysSynced;
            _tmpKeysSynced = _cursor.getInt(_cursorIndexOfKeysSynced);
            final int _tmpSettingsSynced;
            _tmpSettingsSynced = _cursor.getInt(_cursorIndexOfSettingsSynced);
            final String _tmpErrorMessage;
            if (_cursor.isNull(_cursorIndexOfErrorMessage)) {
              _tmpErrorMessage = null;
            } else {
              _tmpErrorMessage = _cursor.getString(_cursorIndexOfErrorMessage);
            }
            final int _tmpRetryCount;
            _tmpRetryCount = _cursor.getInt(_cursorIndexOfRetryCount);
            _item = new SyncLogEntity(_tmpId,_tmpDeviceId,_tmpSyncType,_tmpStatus,_tmpStartTime,_tmpEndTime,_tmpMessagesSynced,_tmpKeysSynced,_tmpSettingsSynced,_tmpErrorMessage,_tmpRetryCount);
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
  public Object getActiveSyncs(final Continuation<? super List<SyncLogEntity>> $completion) {
    final String _sql = "SELECT * FROM sync_logs WHERE status = 'IN_PROGRESS'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SyncLogEntity>>() {
      @Override
      @NonNull
      public List<SyncLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfSyncType = CursorUtil.getColumnIndexOrThrow(_cursor, "syncType");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfMessagesSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "messagesSynced");
          final int _cursorIndexOfKeysSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "keysSynced");
          final int _cursorIndexOfSettingsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "settingsSynced");
          final int _cursorIndexOfErrorMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "errorMessage");
          final int _cursorIndexOfRetryCount = CursorUtil.getColumnIndexOrThrow(_cursor, "retryCount");
          final List<SyncLogEntity> _result = new ArrayList<SyncLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SyncLogEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpDeviceId;
            _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            final String _tmpSyncType;
            _tmpSyncType = _cursor.getString(_cursorIndexOfSyncType);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final LocalDateTime _tmpStartTime;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfStartTime)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfStartTime);
            }
            final LocalDateTime _tmp_1 = __converters.toLocalDateTime(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpStartTime = _tmp_1;
            }
            final LocalDateTime _tmpEndTime;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfEndTime);
            }
            _tmpEndTime = __converters.toLocalDateTime(_tmp_2);
            final int _tmpMessagesSynced;
            _tmpMessagesSynced = _cursor.getInt(_cursorIndexOfMessagesSynced);
            final int _tmpKeysSynced;
            _tmpKeysSynced = _cursor.getInt(_cursorIndexOfKeysSynced);
            final int _tmpSettingsSynced;
            _tmpSettingsSynced = _cursor.getInt(_cursorIndexOfSettingsSynced);
            final String _tmpErrorMessage;
            if (_cursor.isNull(_cursorIndexOfErrorMessage)) {
              _tmpErrorMessage = null;
            } else {
              _tmpErrorMessage = _cursor.getString(_cursorIndexOfErrorMessage);
            }
            final int _tmpRetryCount;
            _tmpRetryCount = _cursor.getInt(_cursorIndexOfRetryCount);
            _item = new SyncLogEntity(_tmpId,_tmpDeviceId,_tmpSyncType,_tmpStatus,_tmpStartTime,_tmpEndTime,_tmpMessagesSynced,_tmpKeysSynced,_tmpSettingsSynced,_tmpErrorMessage,_tmpRetryCount);
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
  public Object getSuccessfulSyncCount(final String deviceId, final LocalDateTime since,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM sync_logs WHERE deviceId = ? AND status = 'SUCCESS' AND startTime > ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, deviceId);
    _argIndex = 2;
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

  @Override
  public Object getLastSuccessfulSync(final String deviceId,
      final Continuation<? super SyncLogEntity> $completion) {
    final String _sql = "SELECT * FROM sync_logs WHERE deviceId = ? AND status = 'SUCCESS' ORDER BY startTime DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, deviceId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SyncLogEntity>() {
      @Override
      @Nullable
      public SyncLogEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfSyncType = CursorUtil.getColumnIndexOrThrow(_cursor, "syncType");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfMessagesSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "messagesSynced");
          final int _cursorIndexOfKeysSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "keysSynced");
          final int _cursorIndexOfSettingsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "settingsSynced");
          final int _cursorIndexOfErrorMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "errorMessage");
          final int _cursorIndexOfRetryCount = CursorUtil.getColumnIndexOrThrow(_cursor, "retryCount");
          final SyncLogEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpDeviceId;
            _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            final String _tmpSyncType;
            _tmpSyncType = _cursor.getString(_cursorIndexOfSyncType);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final LocalDateTime _tmpStartTime;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfStartTime)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfStartTime);
            }
            final LocalDateTime _tmp_1 = __converters.toLocalDateTime(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpStartTime = _tmp_1;
            }
            final LocalDateTime _tmpEndTime;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfEndTime);
            }
            _tmpEndTime = __converters.toLocalDateTime(_tmp_2);
            final int _tmpMessagesSynced;
            _tmpMessagesSynced = _cursor.getInt(_cursorIndexOfMessagesSynced);
            final int _tmpKeysSynced;
            _tmpKeysSynced = _cursor.getInt(_cursorIndexOfKeysSynced);
            final int _tmpSettingsSynced;
            _tmpSettingsSynced = _cursor.getInt(_cursorIndexOfSettingsSynced);
            final String _tmpErrorMessage;
            if (_cursor.isNull(_cursorIndexOfErrorMessage)) {
              _tmpErrorMessage = null;
            } else {
              _tmpErrorMessage = _cursor.getString(_cursorIndexOfErrorMessage);
            }
            final int _tmpRetryCount;
            _tmpRetryCount = _cursor.getInt(_cursorIndexOfRetryCount);
            _result = new SyncLogEntity(_tmpId,_tmpDeviceId,_tmpSyncType,_tmpStatus,_tmpStartTime,_tmpEndTime,_tmpMessagesSynced,_tmpKeysSynced,_tmpSettingsSynced,_tmpErrorMessage,_tmpRetryCount);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
