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
import com.chain.messaging.data.local.entity.RegisteredDeviceEntity;
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
public final class DeviceDao_Impl implements DeviceDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RegisteredDeviceEntity> __insertionAdapterOfRegisteredDeviceEntity;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<RegisteredDeviceEntity> __updateAdapterOfRegisteredDeviceEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateDeviceTrustStatus;

  private final SharedSQLiteStatement __preparedStmtOfUpdateSyncStatus;

  private final SharedSQLiteStatement __preparedStmtOfUpdateLastSeen;

  private final SharedSQLiteStatement __preparedStmtOfDeleteDevice;

  private final SharedSQLiteStatement __preparedStmtOfDeleteUntrustedOldDevices;

  public DeviceDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRegisteredDeviceEntity = new EntityInsertionAdapter<RegisteredDeviceEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `registered_devices` (`deviceId`,`deviceName`,`deviceType`,`platform`,`platformVersion`,`appVersion`,`publicKey`,`lastSeen`,`registeredAt`,`isTrusted`,`lastSyncAt`,`syncStatus`,`isCurrentDevice`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RegisteredDeviceEntity entity) {
        statement.bindString(1, entity.getDeviceId());
        statement.bindString(2, entity.getDeviceName());
        statement.bindString(3, entity.getDeviceType());
        statement.bindString(4, entity.getPlatform());
        statement.bindString(5, entity.getPlatformVersion());
        statement.bindString(6, entity.getAppVersion());
        statement.bindString(7, entity.getPublicKey());
        final String _tmp = __converters.fromLocalDateTime(entity.getLastSeen());
        if (_tmp == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, _tmp);
        }
        final String _tmp_1 = __converters.fromLocalDateTime(entity.getRegisteredAt());
        if (_tmp_1 == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, _tmp_1);
        }
        final int _tmp_2 = entity.isTrusted() ? 1 : 0;
        statement.bindLong(10, _tmp_2);
        final String _tmp_3 = __converters.fromLocalDateTime(entity.getLastSyncAt());
        if (_tmp_3 == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, _tmp_3);
        }
        statement.bindString(12, entity.getSyncStatus());
        final int _tmp_4 = entity.isCurrentDevice() ? 1 : 0;
        statement.bindLong(13, _tmp_4);
      }
    };
    this.__updateAdapterOfRegisteredDeviceEntity = new EntityDeletionOrUpdateAdapter<RegisteredDeviceEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `registered_devices` SET `deviceId` = ?,`deviceName` = ?,`deviceType` = ?,`platform` = ?,`platformVersion` = ?,`appVersion` = ?,`publicKey` = ?,`lastSeen` = ?,`registeredAt` = ?,`isTrusted` = ?,`lastSyncAt` = ?,`syncStatus` = ?,`isCurrentDevice` = ? WHERE `deviceId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RegisteredDeviceEntity entity) {
        statement.bindString(1, entity.getDeviceId());
        statement.bindString(2, entity.getDeviceName());
        statement.bindString(3, entity.getDeviceType());
        statement.bindString(4, entity.getPlatform());
        statement.bindString(5, entity.getPlatformVersion());
        statement.bindString(6, entity.getAppVersion());
        statement.bindString(7, entity.getPublicKey());
        final String _tmp = __converters.fromLocalDateTime(entity.getLastSeen());
        if (_tmp == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, _tmp);
        }
        final String _tmp_1 = __converters.fromLocalDateTime(entity.getRegisteredAt());
        if (_tmp_1 == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, _tmp_1);
        }
        final int _tmp_2 = entity.isTrusted() ? 1 : 0;
        statement.bindLong(10, _tmp_2);
        final String _tmp_3 = __converters.fromLocalDateTime(entity.getLastSyncAt());
        if (_tmp_3 == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, _tmp_3);
        }
        statement.bindString(12, entity.getSyncStatus());
        final int _tmp_4 = entity.isCurrentDevice() ? 1 : 0;
        statement.bindLong(13, _tmp_4);
        statement.bindString(14, entity.getDeviceId());
      }
    };
    this.__preparedStmtOfUpdateDeviceTrustStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE registered_devices SET isTrusted = ? WHERE deviceId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateSyncStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE registered_devices SET syncStatus = ?, lastSyncAt = ? WHERE deviceId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateLastSeen = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE registered_devices SET lastSeen = ? WHERE deviceId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteDevice = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM registered_devices WHERE deviceId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteUntrustedOldDevices = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM registered_devices WHERE isTrusted = 0 AND lastSeen < ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertDevice(final RegisteredDeviceEntity device,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfRegisteredDeviceEntity.insert(device);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateDevice(final RegisteredDeviceEntity device,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfRegisteredDeviceEntity.handle(device);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateDeviceTrustStatus(final String deviceId, final boolean trusted,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateDeviceTrustStatus.acquire();
        int _argIndex = 1;
        final int _tmp = trusted ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindString(_argIndex, deviceId);
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
          __preparedStmtOfUpdateDeviceTrustStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSyncStatus(final String deviceId, final String status,
      final LocalDateTime syncTime, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateSyncStatus.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, status);
        _argIndex = 2;
        final String _tmp = __converters.fromLocalDateTime(syncTime);
        if (_tmp == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, _tmp);
        }
        _argIndex = 3;
        _stmt.bindString(_argIndex, deviceId);
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
          __preparedStmtOfUpdateSyncStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateLastSeen(final String deviceId, final LocalDateTime lastSeen,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateLastSeen.acquire();
        int _argIndex = 1;
        final String _tmp = __converters.fromLocalDateTime(lastSeen);
        if (_tmp == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, _tmp);
        }
        _argIndex = 2;
        _stmt.bindString(_argIndex, deviceId);
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
          __preparedStmtOfUpdateLastSeen.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteDevice(final String deviceId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteDevice.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, deviceId);
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
          __preparedStmtOfDeleteDevice.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteUntrustedOldDevices(final LocalDateTime cutoffTime,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteUntrustedOldDevices.acquire();
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
          __preparedStmtOfDeleteUntrustedOldDevices.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllDevices(
      final Continuation<? super List<RegisteredDeviceEntity>> $completion) {
    final String _sql = "SELECT * FROM registered_devices ORDER BY lastSeen DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RegisteredDeviceEntity>>() {
      @Override
      @NonNull
      public List<RegisteredDeviceEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfDeviceName = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceName");
          final int _cursorIndexOfDeviceType = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceType");
          final int _cursorIndexOfPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "platform");
          final int _cursorIndexOfPlatformVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "platformVersion");
          final int _cursorIndexOfAppVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "appVersion");
          final int _cursorIndexOfPublicKey = CursorUtil.getColumnIndexOrThrow(_cursor, "publicKey");
          final int _cursorIndexOfLastSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSeen");
          final int _cursorIndexOfRegisteredAt = CursorUtil.getColumnIndexOrThrow(_cursor, "registeredAt");
          final int _cursorIndexOfIsTrusted = CursorUtil.getColumnIndexOrThrow(_cursor, "isTrusted");
          final int _cursorIndexOfLastSyncAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncAt");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfIsCurrentDevice = CursorUtil.getColumnIndexOrThrow(_cursor, "isCurrentDevice");
          final List<RegisteredDeviceEntity> _result = new ArrayList<RegisteredDeviceEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RegisteredDeviceEntity _item;
            final String _tmpDeviceId;
            _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            final String _tmpDeviceName;
            _tmpDeviceName = _cursor.getString(_cursorIndexOfDeviceName);
            final String _tmpDeviceType;
            _tmpDeviceType = _cursor.getString(_cursorIndexOfDeviceType);
            final String _tmpPlatform;
            _tmpPlatform = _cursor.getString(_cursorIndexOfPlatform);
            final String _tmpPlatformVersion;
            _tmpPlatformVersion = _cursor.getString(_cursorIndexOfPlatformVersion);
            final String _tmpAppVersion;
            _tmpAppVersion = _cursor.getString(_cursorIndexOfAppVersion);
            final String _tmpPublicKey;
            _tmpPublicKey = _cursor.getString(_cursorIndexOfPublicKey);
            final LocalDateTime _tmpLastSeen;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfLastSeen)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfLastSeen);
            }
            final LocalDateTime _tmp_1 = __converters.toLocalDateTime(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpLastSeen = _tmp_1;
            }
            final LocalDateTime _tmpRegisteredAt;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfRegisteredAt)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfRegisteredAt);
            }
            final LocalDateTime _tmp_3 = __converters.toLocalDateTime(_tmp_2);
            if (_tmp_3 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpRegisteredAt = _tmp_3;
            }
            final boolean _tmpIsTrusted;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsTrusted);
            _tmpIsTrusted = _tmp_4 != 0;
            final LocalDateTime _tmpLastSyncAt;
            final String _tmp_5;
            if (_cursor.isNull(_cursorIndexOfLastSyncAt)) {
              _tmp_5 = null;
            } else {
              _tmp_5 = _cursor.getString(_cursorIndexOfLastSyncAt);
            }
            _tmpLastSyncAt = __converters.toLocalDateTime(_tmp_5);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final boolean _tmpIsCurrentDevice;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfIsCurrentDevice);
            _tmpIsCurrentDevice = _tmp_6 != 0;
            _item = new RegisteredDeviceEntity(_tmpDeviceId,_tmpDeviceName,_tmpDeviceType,_tmpPlatform,_tmpPlatformVersion,_tmpAppVersion,_tmpPublicKey,_tmpLastSeen,_tmpRegisteredAt,_tmpIsTrusted,_tmpLastSyncAt,_tmpSyncStatus,_tmpIsCurrentDevice);
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
  public Flow<List<RegisteredDeviceEntity>> observeAllDevices() {
    final String _sql = "SELECT * FROM registered_devices ORDER BY lastSeen DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"registered_devices"}, new Callable<List<RegisteredDeviceEntity>>() {
      @Override
      @NonNull
      public List<RegisteredDeviceEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfDeviceName = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceName");
          final int _cursorIndexOfDeviceType = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceType");
          final int _cursorIndexOfPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "platform");
          final int _cursorIndexOfPlatformVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "platformVersion");
          final int _cursorIndexOfAppVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "appVersion");
          final int _cursorIndexOfPublicKey = CursorUtil.getColumnIndexOrThrow(_cursor, "publicKey");
          final int _cursorIndexOfLastSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSeen");
          final int _cursorIndexOfRegisteredAt = CursorUtil.getColumnIndexOrThrow(_cursor, "registeredAt");
          final int _cursorIndexOfIsTrusted = CursorUtil.getColumnIndexOrThrow(_cursor, "isTrusted");
          final int _cursorIndexOfLastSyncAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncAt");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfIsCurrentDevice = CursorUtil.getColumnIndexOrThrow(_cursor, "isCurrentDevice");
          final List<RegisteredDeviceEntity> _result = new ArrayList<RegisteredDeviceEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RegisteredDeviceEntity _item;
            final String _tmpDeviceId;
            _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            final String _tmpDeviceName;
            _tmpDeviceName = _cursor.getString(_cursorIndexOfDeviceName);
            final String _tmpDeviceType;
            _tmpDeviceType = _cursor.getString(_cursorIndexOfDeviceType);
            final String _tmpPlatform;
            _tmpPlatform = _cursor.getString(_cursorIndexOfPlatform);
            final String _tmpPlatformVersion;
            _tmpPlatformVersion = _cursor.getString(_cursorIndexOfPlatformVersion);
            final String _tmpAppVersion;
            _tmpAppVersion = _cursor.getString(_cursorIndexOfAppVersion);
            final String _tmpPublicKey;
            _tmpPublicKey = _cursor.getString(_cursorIndexOfPublicKey);
            final LocalDateTime _tmpLastSeen;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfLastSeen)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfLastSeen);
            }
            final LocalDateTime _tmp_1 = __converters.toLocalDateTime(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpLastSeen = _tmp_1;
            }
            final LocalDateTime _tmpRegisteredAt;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfRegisteredAt)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfRegisteredAt);
            }
            final LocalDateTime _tmp_3 = __converters.toLocalDateTime(_tmp_2);
            if (_tmp_3 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpRegisteredAt = _tmp_3;
            }
            final boolean _tmpIsTrusted;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsTrusted);
            _tmpIsTrusted = _tmp_4 != 0;
            final LocalDateTime _tmpLastSyncAt;
            final String _tmp_5;
            if (_cursor.isNull(_cursorIndexOfLastSyncAt)) {
              _tmp_5 = null;
            } else {
              _tmp_5 = _cursor.getString(_cursorIndexOfLastSyncAt);
            }
            _tmpLastSyncAt = __converters.toLocalDateTime(_tmp_5);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final boolean _tmpIsCurrentDevice;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfIsCurrentDevice);
            _tmpIsCurrentDevice = _tmp_6 != 0;
            _item = new RegisteredDeviceEntity(_tmpDeviceId,_tmpDeviceName,_tmpDeviceType,_tmpPlatform,_tmpPlatformVersion,_tmpAppVersion,_tmpPublicKey,_tmpLastSeen,_tmpRegisteredAt,_tmpIsTrusted,_tmpLastSyncAt,_tmpSyncStatus,_tmpIsCurrentDevice);
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
  public Object getDeviceById(final String deviceId,
      final Continuation<? super RegisteredDeviceEntity> $completion) {
    final String _sql = "SELECT * FROM registered_devices WHERE deviceId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, deviceId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<RegisteredDeviceEntity>() {
      @Override
      @Nullable
      public RegisteredDeviceEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfDeviceName = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceName");
          final int _cursorIndexOfDeviceType = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceType");
          final int _cursorIndexOfPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "platform");
          final int _cursorIndexOfPlatformVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "platformVersion");
          final int _cursorIndexOfAppVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "appVersion");
          final int _cursorIndexOfPublicKey = CursorUtil.getColumnIndexOrThrow(_cursor, "publicKey");
          final int _cursorIndexOfLastSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSeen");
          final int _cursorIndexOfRegisteredAt = CursorUtil.getColumnIndexOrThrow(_cursor, "registeredAt");
          final int _cursorIndexOfIsTrusted = CursorUtil.getColumnIndexOrThrow(_cursor, "isTrusted");
          final int _cursorIndexOfLastSyncAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncAt");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfIsCurrentDevice = CursorUtil.getColumnIndexOrThrow(_cursor, "isCurrentDevice");
          final RegisteredDeviceEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpDeviceId;
            _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            final String _tmpDeviceName;
            _tmpDeviceName = _cursor.getString(_cursorIndexOfDeviceName);
            final String _tmpDeviceType;
            _tmpDeviceType = _cursor.getString(_cursorIndexOfDeviceType);
            final String _tmpPlatform;
            _tmpPlatform = _cursor.getString(_cursorIndexOfPlatform);
            final String _tmpPlatformVersion;
            _tmpPlatformVersion = _cursor.getString(_cursorIndexOfPlatformVersion);
            final String _tmpAppVersion;
            _tmpAppVersion = _cursor.getString(_cursorIndexOfAppVersion);
            final String _tmpPublicKey;
            _tmpPublicKey = _cursor.getString(_cursorIndexOfPublicKey);
            final LocalDateTime _tmpLastSeen;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfLastSeen)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfLastSeen);
            }
            final LocalDateTime _tmp_1 = __converters.toLocalDateTime(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpLastSeen = _tmp_1;
            }
            final LocalDateTime _tmpRegisteredAt;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfRegisteredAt)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfRegisteredAt);
            }
            final LocalDateTime _tmp_3 = __converters.toLocalDateTime(_tmp_2);
            if (_tmp_3 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpRegisteredAt = _tmp_3;
            }
            final boolean _tmpIsTrusted;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsTrusted);
            _tmpIsTrusted = _tmp_4 != 0;
            final LocalDateTime _tmpLastSyncAt;
            final String _tmp_5;
            if (_cursor.isNull(_cursorIndexOfLastSyncAt)) {
              _tmp_5 = null;
            } else {
              _tmp_5 = _cursor.getString(_cursorIndexOfLastSyncAt);
            }
            _tmpLastSyncAt = __converters.toLocalDateTime(_tmp_5);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final boolean _tmpIsCurrentDevice;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfIsCurrentDevice);
            _tmpIsCurrentDevice = _tmp_6 != 0;
            _result = new RegisteredDeviceEntity(_tmpDeviceId,_tmpDeviceName,_tmpDeviceType,_tmpPlatform,_tmpPlatformVersion,_tmpAppVersion,_tmpPublicKey,_tmpLastSeen,_tmpRegisteredAt,_tmpIsTrusted,_tmpLastSyncAt,_tmpSyncStatus,_tmpIsCurrentDevice);
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
  public Object getTrustedDevices(
      final Continuation<? super List<RegisteredDeviceEntity>> $completion) {
    final String _sql = "SELECT * FROM registered_devices WHERE isTrusted = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RegisteredDeviceEntity>>() {
      @Override
      @NonNull
      public List<RegisteredDeviceEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfDeviceName = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceName");
          final int _cursorIndexOfDeviceType = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceType");
          final int _cursorIndexOfPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "platform");
          final int _cursorIndexOfPlatformVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "platformVersion");
          final int _cursorIndexOfAppVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "appVersion");
          final int _cursorIndexOfPublicKey = CursorUtil.getColumnIndexOrThrow(_cursor, "publicKey");
          final int _cursorIndexOfLastSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSeen");
          final int _cursorIndexOfRegisteredAt = CursorUtil.getColumnIndexOrThrow(_cursor, "registeredAt");
          final int _cursorIndexOfIsTrusted = CursorUtil.getColumnIndexOrThrow(_cursor, "isTrusted");
          final int _cursorIndexOfLastSyncAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncAt");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfIsCurrentDevice = CursorUtil.getColumnIndexOrThrow(_cursor, "isCurrentDevice");
          final List<RegisteredDeviceEntity> _result = new ArrayList<RegisteredDeviceEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RegisteredDeviceEntity _item;
            final String _tmpDeviceId;
            _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            final String _tmpDeviceName;
            _tmpDeviceName = _cursor.getString(_cursorIndexOfDeviceName);
            final String _tmpDeviceType;
            _tmpDeviceType = _cursor.getString(_cursorIndexOfDeviceType);
            final String _tmpPlatform;
            _tmpPlatform = _cursor.getString(_cursorIndexOfPlatform);
            final String _tmpPlatformVersion;
            _tmpPlatformVersion = _cursor.getString(_cursorIndexOfPlatformVersion);
            final String _tmpAppVersion;
            _tmpAppVersion = _cursor.getString(_cursorIndexOfAppVersion);
            final String _tmpPublicKey;
            _tmpPublicKey = _cursor.getString(_cursorIndexOfPublicKey);
            final LocalDateTime _tmpLastSeen;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfLastSeen)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfLastSeen);
            }
            final LocalDateTime _tmp_1 = __converters.toLocalDateTime(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpLastSeen = _tmp_1;
            }
            final LocalDateTime _tmpRegisteredAt;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfRegisteredAt)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfRegisteredAt);
            }
            final LocalDateTime _tmp_3 = __converters.toLocalDateTime(_tmp_2);
            if (_tmp_3 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpRegisteredAt = _tmp_3;
            }
            final boolean _tmpIsTrusted;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsTrusted);
            _tmpIsTrusted = _tmp_4 != 0;
            final LocalDateTime _tmpLastSyncAt;
            final String _tmp_5;
            if (_cursor.isNull(_cursorIndexOfLastSyncAt)) {
              _tmp_5 = null;
            } else {
              _tmp_5 = _cursor.getString(_cursorIndexOfLastSyncAt);
            }
            _tmpLastSyncAt = __converters.toLocalDateTime(_tmp_5);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final boolean _tmpIsCurrentDevice;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfIsCurrentDevice);
            _tmpIsCurrentDevice = _tmp_6 != 0;
            _item = new RegisteredDeviceEntity(_tmpDeviceId,_tmpDeviceName,_tmpDeviceType,_tmpPlatform,_tmpPlatformVersion,_tmpAppVersion,_tmpPublicKey,_tmpLastSeen,_tmpRegisteredAt,_tmpIsTrusted,_tmpLastSyncAt,_tmpSyncStatus,_tmpIsCurrentDevice);
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
  public Object getDevicesNeedingSync(
      final Continuation<? super List<RegisteredDeviceEntity>> $completion) {
    final String _sql = "SELECT * FROM registered_devices WHERE syncStatus = 'PENDING' OR syncStatus = 'ERROR'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RegisteredDeviceEntity>>() {
      @Override
      @NonNull
      public List<RegisteredDeviceEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfDeviceName = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceName");
          final int _cursorIndexOfDeviceType = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceType");
          final int _cursorIndexOfPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "platform");
          final int _cursorIndexOfPlatformVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "platformVersion");
          final int _cursorIndexOfAppVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "appVersion");
          final int _cursorIndexOfPublicKey = CursorUtil.getColumnIndexOrThrow(_cursor, "publicKey");
          final int _cursorIndexOfLastSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSeen");
          final int _cursorIndexOfRegisteredAt = CursorUtil.getColumnIndexOrThrow(_cursor, "registeredAt");
          final int _cursorIndexOfIsTrusted = CursorUtil.getColumnIndexOrThrow(_cursor, "isTrusted");
          final int _cursorIndexOfLastSyncAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncAt");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfIsCurrentDevice = CursorUtil.getColumnIndexOrThrow(_cursor, "isCurrentDevice");
          final List<RegisteredDeviceEntity> _result = new ArrayList<RegisteredDeviceEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RegisteredDeviceEntity _item;
            final String _tmpDeviceId;
            _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            final String _tmpDeviceName;
            _tmpDeviceName = _cursor.getString(_cursorIndexOfDeviceName);
            final String _tmpDeviceType;
            _tmpDeviceType = _cursor.getString(_cursorIndexOfDeviceType);
            final String _tmpPlatform;
            _tmpPlatform = _cursor.getString(_cursorIndexOfPlatform);
            final String _tmpPlatformVersion;
            _tmpPlatformVersion = _cursor.getString(_cursorIndexOfPlatformVersion);
            final String _tmpAppVersion;
            _tmpAppVersion = _cursor.getString(_cursorIndexOfAppVersion);
            final String _tmpPublicKey;
            _tmpPublicKey = _cursor.getString(_cursorIndexOfPublicKey);
            final LocalDateTime _tmpLastSeen;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfLastSeen)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfLastSeen);
            }
            final LocalDateTime _tmp_1 = __converters.toLocalDateTime(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpLastSeen = _tmp_1;
            }
            final LocalDateTime _tmpRegisteredAt;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfRegisteredAt)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfRegisteredAt);
            }
            final LocalDateTime _tmp_3 = __converters.toLocalDateTime(_tmp_2);
            if (_tmp_3 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpRegisteredAt = _tmp_3;
            }
            final boolean _tmpIsTrusted;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsTrusted);
            _tmpIsTrusted = _tmp_4 != 0;
            final LocalDateTime _tmpLastSyncAt;
            final String _tmp_5;
            if (_cursor.isNull(_cursorIndexOfLastSyncAt)) {
              _tmp_5 = null;
            } else {
              _tmp_5 = _cursor.getString(_cursorIndexOfLastSyncAt);
            }
            _tmpLastSyncAt = __converters.toLocalDateTime(_tmp_5);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final boolean _tmpIsCurrentDevice;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfIsCurrentDevice);
            _tmpIsCurrentDevice = _tmp_6 != 0;
            _item = new RegisteredDeviceEntity(_tmpDeviceId,_tmpDeviceName,_tmpDeviceType,_tmpPlatform,_tmpPlatformVersion,_tmpAppVersion,_tmpPublicKey,_tmpLastSeen,_tmpRegisteredAt,_tmpIsTrusted,_tmpLastSyncAt,_tmpSyncStatus,_tmpIsCurrentDevice);
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
  public Object getCurrentDevice(final Continuation<? super RegisteredDeviceEntity> $completion) {
    final String _sql = "SELECT * FROM registered_devices WHERE isCurrentDevice = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<RegisteredDeviceEntity>() {
      @Override
      @Nullable
      public RegisteredDeviceEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfDeviceName = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceName");
          final int _cursorIndexOfDeviceType = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceType");
          final int _cursorIndexOfPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "platform");
          final int _cursorIndexOfPlatformVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "platformVersion");
          final int _cursorIndexOfAppVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "appVersion");
          final int _cursorIndexOfPublicKey = CursorUtil.getColumnIndexOrThrow(_cursor, "publicKey");
          final int _cursorIndexOfLastSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSeen");
          final int _cursorIndexOfRegisteredAt = CursorUtil.getColumnIndexOrThrow(_cursor, "registeredAt");
          final int _cursorIndexOfIsTrusted = CursorUtil.getColumnIndexOrThrow(_cursor, "isTrusted");
          final int _cursorIndexOfLastSyncAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncAt");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfIsCurrentDevice = CursorUtil.getColumnIndexOrThrow(_cursor, "isCurrentDevice");
          final RegisteredDeviceEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpDeviceId;
            _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            final String _tmpDeviceName;
            _tmpDeviceName = _cursor.getString(_cursorIndexOfDeviceName);
            final String _tmpDeviceType;
            _tmpDeviceType = _cursor.getString(_cursorIndexOfDeviceType);
            final String _tmpPlatform;
            _tmpPlatform = _cursor.getString(_cursorIndexOfPlatform);
            final String _tmpPlatformVersion;
            _tmpPlatformVersion = _cursor.getString(_cursorIndexOfPlatformVersion);
            final String _tmpAppVersion;
            _tmpAppVersion = _cursor.getString(_cursorIndexOfAppVersion);
            final String _tmpPublicKey;
            _tmpPublicKey = _cursor.getString(_cursorIndexOfPublicKey);
            final LocalDateTime _tmpLastSeen;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfLastSeen)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfLastSeen);
            }
            final LocalDateTime _tmp_1 = __converters.toLocalDateTime(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpLastSeen = _tmp_1;
            }
            final LocalDateTime _tmpRegisteredAt;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfRegisteredAt)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfRegisteredAt);
            }
            final LocalDateTime _tmp_3 = __converters.toLocalDateTime(_tmp_2);
            if (_tmp_3 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpRegisteredAt = _tmp_3;
            }
            final boolean _tmpIsTrusted;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsTrusted);
            _tmpIsTrusted = _tmp_4 != 0;
            final LocalDateTime _tmpLastSyncAt;
            final String _tmp_5;
            if (_cursor.isNull(_cursorIndexOfLastSyncAt)) {
              _tmp_5 = null;
            } else {
              _tmp_5 = _cursor.getString(_cursorIndexOfLastSyncAt);
            }
            _tmpLastSyncAt = __converters.toLocalDateTime(_tmp_5);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final boolean _tmpIsCurrentDevice;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfIsCurrentDevice);
            _tmpIsCurrentDevice = _tmp_6 != 0;
            _result = new RegisteredDeviceEntity(_tmpDeviceId,_tmpDeviceName,_tmpDeviceType,_tmpPlatform,_tmpPlatformVersion,_tmpAppVersion,_tmpPublicKey,_tmpLastSeen,_tmpRegisteredAt,_tmpIsTrusted,_tmpLastSyncAt,_tmpSyncStatus,_tmpIsCurrentDevice);
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
  public Object getTrustedDeviceCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM registered_devices WHERE isTrusted = 1";
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
  public Object getPendingSyncCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM registered_devices WHERE syncStatus = 'PENDING'";
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
