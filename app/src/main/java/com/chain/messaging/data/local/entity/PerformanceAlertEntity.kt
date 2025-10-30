package com.chain.messaging.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing performance alerts in the database
 */
@Entity(tableName = "performance_alerts")
data class PerformanceAlertEntity(
    @PrimaryKey
    val id: String,
    val type: String,
    val severity: String,
    val message: String,
    val timestamp: Long,
    val metricsJson: String
)