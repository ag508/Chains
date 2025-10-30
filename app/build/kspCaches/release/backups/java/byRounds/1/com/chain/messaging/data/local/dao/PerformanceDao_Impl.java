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
import com.chain.messaging.data.local.entity.PerformanceAlertEntity;
import com.chain.messaging.data.local.entity.PerformanceMetricsEntity;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Float;
import java.lang.Integer;
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

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PerformanceDao_Impl implements PerformanceDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PerformanceMetricsEntity> __insertionAdapterOfPerformanceMetricsEntity;

  private final EntityInsertionAdapter<PerformanceAlertEntity> __insertionAdapterOfPerformanceAlertEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOldMetrics;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOldAlerts;

  public PerformanceDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPerformanceMetricsEntity = new EntityInsertionAdapter<PerformanceMetricsEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `performance_metrics` (`timestamp`,`messagesPerSecond`,`averageLatencyMs`,`totalMessages`,`usedMemoryMb`,`totalMemoryMb`,`memoryUsagePercentage`,`gcCount`,`gcTimeMs`,`batteryLevel`,`isCharging`,`batteryDrainRate`,`networkLatencyMs`,`networkThroughputKbps`,`networkQuality`,`cpuUsagePercentage`,`threadCount`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PerformanceMetricsEntity entity) {
        statement.bindLong(1, entity.getTimestamp());
        statement.bindDouble(2, entity.getMessagesPerSecond());
        statement.bindLong(3, entity.getAverageLatencyMs());
        statement.bindLong(4, entity.getTotalMessages());
        statement.bindLong(5, entity.getUsedMemoryMb());
        statement.bindLong(6, entity.getTotalMemoryMb());
        statement.bindDouble(7, entity.getMemoryUsagePercentage());
        statement.bindLong(8, entity.getGcCount());
        statement.bindLong(9, entity.getGcTimeMs());
        statement.bindDouble(10, entity.getBatteryLevel());
        final int _tmp = entity.isCharging() ? 1 : 0;
        statement.bindLong(11, _tmp);
        statement.bindDouble(12, entity.getBatteryDrainRate());
        statement.bindLong(13, entity.getNetworkLatencyMs());
        statement.bindLong(14, entity.getNetworkThroughputKbps());
        statement.bindString(15, entity.getNetworkQuality());
        statement.bindDouble(16, entity.getCpuUsagePercentage());
        statement.bindLong(17, entity.getThreadCount());
      }
    };
    this.__insertionAdapterOfPerformanceAlertEntity = new EntityInsertionAdapter<PerformanceAlertEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `performance_alerts` (`id`,`type`,`severity`,`message`,`timestamp`,`metricsJson`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PerformanceAlertEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getType());
        statement.bindString(3, entity.getSeverity());
        statement.bindString(4, entity.getMessage());
        statement.bindLong(5, entity.getTimestamp());
        statement.bindString(6, entity.getMetricsJson());
      }
    };
    this.__preparedStmtOfDeleteOldMetrics = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM performance_metrics WHERE timestamp < ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteOldAlerts = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM performance_alerts WHERE timestamp < ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertMetrics(final PerformanceMetricsEntity metrics,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPerformanceMetricsEntity.insert(metrics);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAlert(final PerformanceAlertEntity alert,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPerformanceAlertEntity.insert(alert);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteOldMetrics(final long cutoffTime,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOldMetrics.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, cutoffTime);
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
          __preparedStmtOfDeleteOldMetrics.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteOldAlerts(final long cutoffTime,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOldAlerts.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, cutoffTime);
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
          __preparedStmtOfDeleteOldAlerts.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getMetrics(final long fromTimestamp, final long toTimestamp,
      final Continuation<? super List<PerformanceMetricsEntity>> $completion) {
    final String _sql = "SELECT * FROM performance_metrics WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, fromTimestamp);
    _argIndex = 2;
    _statement.bindLong(_argIndex, toTimestamp);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PerformanceMetricsEntity>>() {
      @Override
      @NonNull
      public List<PerformanceMetricsEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfMessagesPerSecond = CursorUtil.getColumnIndexOrThrow(_cursor, "messagesPerSecond");
          final int _cursorIndexOfAverageLatencyMs = CursorUtil.getColumnIndexOrThrow(_cursor, "averageLatencyMs");
          final int _cursorIndexOfTotalMessages = CursorUtil.getColumnIndexOrThrow(_cursor, "totalMessages");
          final int _cursorIndexOfUsedMemoryMb = CursorUtil.getColumnIndexOrThrow(_cursor, "usedMemoryMb");
          final int _cursorIndexOfTotalMemoryMb = CursorUtil.getColumnIndexOrThrow(_cursor, "totalMemoryMb");
          final int _cursorIndexOfMemoryUsagePercentage = CursorUtil.getColumnIndexOrThrow(_cursor, "memoryUsagePercentage");
          final int _cursorIndexOfGcCount = CursorUtil.getColumnIndexOrThrow(_cursor, "gcCount");
          final int _cursorIndexOfGcTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "gcTimeMs");
          final int _cursorIndexOfBatteryLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "batteryLevel");
          final int _cursorIndexOfIsCharging = CursorUtil.getColumnIndexOrThrow(_cursor, "isCharging");
          final int _cursorIndexOfBatteryDrainRate = CursorUtil.getColumnIndexOrThrow(_cursor, "batteryDrainRate");
          final int _cursorIndexOfNetworkLatencyMs = CursorUtil.getColumnIndexOrThrow(_cursor, "networkLatencyMs");
          final int _cursorIndexOfNetworkThroughputKbps = CursorUtil.getColumnIndexOrThrow(_cursor, "networkThroughputKbps");
          final int _cursorIndexOfNetworkQuality = CursorUtil.getColumnIndexOrThrow(_cursor, "networkQuality");
          final int _cursorIndexOfCpuUsagePercentage = CursorUtil.getColumnIndexOrThrow(_cursor, "cpuUsagePercentage");
          final int _cursorIndexOfThreadCount = CursorUtil.getColumnIndexOrThrow(_cursor, "threadCount");
          final List<PerformanceMetricsEntity> _result = new ArrayList<PerformanceMetricsEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PerformanceMetricsEntity _item;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final double _tmpMessagesPerSecond;
            _tmpMessagesPerSecond = _cursor.getDouble(_cursorIndexOfMessagesPerSecond);
            final long _tmpAverageLatencyMs;
            _tmpAverageLatencyMs = _cursor.getLong(_cursorIndexOfAverageLatencyMs);
            final long _tmpTotalMessages;
            _tmpTotalMessages = _cursor.getLong(_cursorIndexOfTotalMessages);
            final long _tmpUsedMemoryMb;
            _tmpUsedMemoryMb = _cursor.getLong(_cursorIndexOfUsedMemoryMb);
            final long _tmpTotalMemoryMb;
            _tmpTotalMemoryMb = _cursor.getLong(_cursorIndexOfTotalMemoryMb);
            final float _tmpMemoryUsagePercentage;
            _tmpMemoryUsagePercentage = _cursor.getFloat(_cursorIndexOfMemoryUsagePercentage);
            final int _tmpGcCount;
            _tmpGcCount = _cursor.getInt(_cursorIndexOfGcCount);
            final long _tmpGcTimeMs;
            _tmpGcTimeMs = _cursor.getLong(_cursorIndexOfGcTimeMs);
            final float _tmpBatteryLevel;
            _tmpBatteryLevel = _cursor.getFloat(_cursorIndexOfBatteryLevel);
            final boolean _tmpIsCharging;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCharging);
            _tmpIsCharging = _tmp != 0;
            final float _tmpBatteryDrainRate;
            _tmpBatteryDrainRate = _cursor.getFloat(_cursorIndexOfBatteryDrainRate);
            final long _tmpNetworkLatencyMs;
            _tmpNetworkLatencyMs = _cursor.getLong(_cursorIndexOfNetworkLatencyMs);
            final long _tmpNetworkThroughputKbps;
            _tmpNetworkThroughputKbps = _cursor.getLong(_cursorIndexOfNetworkThroughputKbps);
            final String _tmpNetworkQuality;
            _tmpNetworkQuality = _cursor.getString(_cursorIndexOfNetworkQuality);
            final float _tmpCpuUsagePercentage;
            _tmpCpuUsagePercentage = _cursor.getFloat(_cursorIndexOfCpuUsagePercentage);
            final int _tmpThreadCount;
            _tmpThreadCount = _cursor.getInt(_cursorIndexOfThreadCount);
            _item = new PerformanceMetricsEntity(_tmpTimestamp,_tmpMessagesPerSecond,_tmpAverageLatencyMs,_tmpTotalMessages,_tmpUsedMemoryMb,_tmpTotalMemoryMb,_tmpMemoryUsagePercentage,_tmpGcCount,_tmpGcTimeMs,_tmpBatteryLevel,_tmpIsCharging,_tmpBatteryDrainRate,_tmpNetworkLatencyMs,_tmpNetworkThroughputKbps,_tmpNetworkQuality,_tmpCpuUsagePercentage,_tmpThreadCount);
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
  public Object getAlerts(final long fromTimestamp, final long toTimestamp,
      final Continuation<? super List<PerformanceAlertEntity>> $completion) {
    final String _sql = "SELECT * FROM performance_alerts WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, fromTimestamp);
    _argIndex = 2;
    _statement.bindLong(_argIndex, toTimestamp);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PerformanceAlertEntity>>() {
      @Override
      @NonNull
      public List<PerformanceAlertEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfSeverity = CursorUtil.getColumnIndexOrThrow(_cursor, "severity");
          final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfMetricsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "metricsJson");
          final List<PerformanceAlertEntity> _result = new ArrayList<PerformanceAlertEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PerformanceAlertEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpSeverity;
            _tmpSeverity = _cursor.getString(_cursorIndexOfSeverity);
            final String _tmpMessage;
            _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpMetricsJson;
            _tmpMetricsJson = _cursor.getString(_cursorIndexOfMetricsJson);
            _item = new PerformanceAlertEntity(_tmpId,_tmpType,_tmpSeverity,_tmpMessage,_tmpTimestamp,_tmpMetricsJson);
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
  public Object getRecentMetrics(final int limit,
      final Continuation<? super List<PerformanceMetricsEntity>> $completion) {
    final String _sql = "SELECT * FROM performance_metrics ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PerformanceMetricsEntity>>() {
      @Override
      @NonNull
      public List<PerformanceMetricsEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfMessagesPerSecond = CursorUtil.getColumnIndexOrThrow(_cursor, "messagesPerSecond");
          final int _cursorIndexOfAverageLatencyMs = CursorUtil.getColumnIndexOrThrow(_cursor, "averageLatencyMs");
          final int _cursorIndexOfTotalMessages = CursorUtil.getColumnIndexOrThrow(_cursor, "totalMessages");
          final int _cursorIndexOfUsedMemoryMb = CursorUtil.getColumnIndexOrThrow(_cursor, "usedMemoryMb");
          final int _cursorIndexOfTotalMemoryMb = CursorUtil.getColumnIndexOrThrow(_cursor, "totalMemoryMb");
          final int _cursorIndexOfMemoryUsagePercentage = CursorUtil.getColumnIndexOrThrow(_cursor, "memoryUsagePercentage");
          final int _cursorIndexOfGcCount = CursorUtil.getColumnIndexOrThrow(_cursor, "gcCount");
          final int _cursorIndexOfGcTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "gcTimeMs");
          final int _cursorIndexOfBatteryLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "batteryLevel");
          final int _cursorIndexOfIsCharging = CursorUtil.getColumnIndexOrThrow(_cursor, "isCharging");
          final int _cursorIndexOfBatteryDrainRate = CursorUtil.getColumnIndexOrThrow(_cursor, "batteryDrainRate");
          final int _cursorIndexOfNetworkLatencyMs = CursorUtil.getColumnIndexOrThrow(_cursor, "networkLatencyMs");
          final int _cursorIndexOfNetworkThroughputKbps = CursorUtil.getColumnIndexOrThrow(_cursor, "networkThroughputKbps");
          final int _cursorIndexOfNetworkQuality = CursorUtil.getColumnIndexOrThrow(_cursor, "networkQuality");
          final int _cursorIndexOfCpuUsagePercentage = CursorUtil.getColumnIndexOrThrow(_cursor, "cpuUsagePercentage");
          final int _cursorIndexOfThreadCount = CursorUtil.getColumnIndexOrThrow(_cursor, "threadCount");
          final List<PerformanceMetricsEntity> _result = new ArrayList<PerformanceMetricsEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PerformanceMetricsEntity _item;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final double _tmpMessagesPerSecond;
            _tmpMessagesPerSecond = _cursor.getDouble(_cursorIndexOfMessagesPerSecond);
            final long _tmpAverageLatencyMs;
            _tmpAverageLatencyMs = _cursor.getLong(_cursorIndexOfAverageLatencyMs);
            final long _tmpTotalMessages;
            _tmpTotalMessages = _cursor.getLong(_cursorIndexOfTotalMessages);
            final long _tmpUsedMemoryMb;
            _tmpUsedMemoryMb = _cursor.getLong(_cursorIndexOfUsedMemoryMb);
            final long _tmpTotalMemoryMb;
            _tmpTotalMemoryMb = _cursor.getLong(_cursorIndexOfTotalMemoryMb);
            final float _tmpMemoryUsagePercentage;
            _tmpMemoryUsagePercentage = _cursor.getFloat(_cursorIndexOfMemoryUsagePercentage);
            final int _tmpGcCount;
            _tmpGcCount = _cursor.getInt(_cursorIndexOfGcCount);
            final long _tmpGcTimeMs;
            _tmpGcTimeMs = _cursor.getLong(_cursorIndexOfGcTimeMs);
            final float _tmpBatteryLevel;
            _tmpBatteryLevel = _cursor.getFloat(_cursorIndexOfBatteryLevel);
            final boolean _tmpIsCharging;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCharging);
            _tmpIsCharging = _tmp != 0;
            final float _tmpBatteryDrainRate;
            _tmpBatteryDrainRate = _cursor.getFloat(_cursorIndexOfBatteryDrainRate);
            final long _tmpNetworkLatencyMs;
            _tmpNetworkLatencyMs = _cursor.getLong(_cursorIndexOfNetworkLatencyMs);
            final long _tmpNetworkThroughputKbps;
            _tmpNetworkThroughputKbps = _cursor.getLong(_cursorIndexOfNetworkThroughputKbps);
            final String _tmpNetworkQuality;
            _tmpNetworkQuality = _cursor.getString(_cursorIndexOfNetworkQuality);
            final float _tmpCpuUsagePercentage;
            _tmpCpuUsagePercentage = _cursor.getFloat(_cursorIndexOfCpuUsagePercentage);
            final int _tmpThreadCount;
            _tmpThreadCount = _cursor.getInt(_cursorIndexOfThreadCount);
            _item = new PerformanceMetricsEntity(_tmpTimestamp,_tmpMessagesPerSecond,_tmpAverageLatencyMs,_tmpTotalMessages,_tmpUsedMemoryMb,_tmpTotalMemoryMb,_tmpMemoryUsagePercentage,_tmpGcCount,_tmpGcTimeMs,_tmpBatteryLevel,_tmpIsCharging,_tmpBatteryDrainRate,_tmpNetworkLatencyMs,_tmpNetworkThroughputKbps,_tmpNetworkQuality,_tmpCpuUsagePercentage,_tmpThreadCount);
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
  public Object getRecentAlerts(final int limit,
      final Continuation<? super List<PerformanceAlertEntity>> $completion) {
    final String _sql = "SELECT * FROM performance_alerts ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PerformanceAlertEntity>>() {
      @Override
      @NonNull
      public List<PerformanceAlertEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfSeverity = CursorUtil.getColumnIndexOrThrow(_cursor, "severity");
          final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfMetricsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "metricsJson");
          final List<PerformanceAlertEntity> _result = new ArrayList<PerformanceAlertEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PerformanceAlertEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpSeverity;
            _tmpSeverity = _cursor.getString(_cursorIndexOfSeverity);
            final String _tmpMessage;
            _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpMetricsJson;
            _tmpMetricsJson = _cursor.getString(_cursorIndexOfMetricsJson);
            _item = new PerformanceAlertEntity(_tmpId,_tmpType,_tmpSeverity,_tmpMessage,_tmpTimestamp,_tmpMetricsJson);
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
  public Object getMetricsCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM performance_metrics";
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
  public Object getAlertsCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM performance_alerts";
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
  public Object getAverageMemoryUsage(final long since,
      final Continuation<? super Float> $completion) {
    final String _sql = "SELECT AVG(memoryUsagePercentage) FROM performance_metrics WHERE timestamp > ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Float>() {
      @Override
      @Nullable
      public Float call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Float _result;
          if (_cursor.moveToFirst()) {
            final Float _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getFloat(0);
            }
            _result = _tmp;
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
  public Object getAverageCpuUsage(final long since,
      final Continuation<? super Float> $completion) {
    final String _sql = "SELECT AVG(cpuUsagePercentage) FROM performance_metrics WHERE timestamp > ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Float>() {
      @Override
      @Nullable
      public Float call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Float _result;
          if (_cursor.moveToFirst()) {
            final Float _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getFloat(0);
            }
            _result = _tmp;
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
  public Object getAverageMessageThroughput(final long since,
      final Continuation<? super Double> $completion) {
    final String _sql = "SELECT AVG(messagesPerSecond) FROM performance_metrics WHERE timestamp > ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
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
