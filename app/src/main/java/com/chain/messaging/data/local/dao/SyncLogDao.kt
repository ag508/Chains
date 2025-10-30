package com.chain.messaging.data.local.dao

import androidx.room.*
import com.chain.messaging.data.local.entity.SyncLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Data Access Object for synchronization logs
 */
@Dao
interface SyncLogDao {
    
    @Query("SELECT * FROM sync_logs ORDER BY startTime DESC LIMIT :limit")
    suspend fun getRecentSyncLogs(limit: Int = 50): List<SyncLogEntity>
    
    @Query("SELECT * FROM sync_logs WHERE deviceId = :deviceId ORDER BY startTime DESC")
    suspend fun getSyncLogsForDevice(deviceId: String): List<SyncLogEntity>
    
    @Query("SELECT * FROM sync_logs WHERE status = 'ERROR' ORDER BY startTime DESC")
    suspend fun getFailedSyncs(): List<SyncLogEntity>
    
    @Query("SELECT * FROM sync_logs WHERE status = 'IN_PROGRESS'")
    suspend fun getActiveSyncs(): List<SyncLogEntity>
    
    @Insert
    suspend fun insertSyncLog(syncLog: SyncLogEntity)
    
    @Update
    suspend fun updateSyncLog(syncLog: SyncLogEntity)
    
    @Query("DELETE FROM sync_logs WHERE startTime < :cutoffTime")
    suspend fun deleteOldSyncLogs(cutoffTime: LocalDateTime)
    
    @Query("SELECT COUNT(*) FROM sync_logs WHERE deviceId = :deviceId AND status = 'SUCCESS' AND startTime > :since")
    suspend fun getSuccessfulSyncCount(deviceId: String, since: LocalDateTime): Int
    
    @Query("SELECT * FROM sync_logs WHERE deviceId = :deviceId AND status = 'SUCCESS' ORDER BY startTime DESC LIMIT 1")
    suspend fun getLastSuccessfulSync(deviceId: String): SyncLogEntity?
}