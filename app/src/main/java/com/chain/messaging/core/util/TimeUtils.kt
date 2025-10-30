package com.chain.messaging.core.util

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date

/**
 * Centralized utility object for consistent time and date conversions throughout the application.
 * 
 * This utility provides standardized methods for converting between different time representations:
 * - Date objects (used in domain models)
 * - Long timestamps (used in database entities)
 * - LocalDateTime objects (used in some services)
 */
object TimeUtils {
    
    /**
     * Converts a Date object to Long timestamp (milliseconds since epoch).
     * 
     * @param date The Date object to convert
     * @return Long timestamp in milliseconds
     */
    fun dateToLong(date: Date): Long = date.time
    
    /**
     * Converts a LocalDateTime to Long timestamp (seconds since epoch converted to milliseconds).
     * Uses UTC timezone for consistent timestamp handling across different devices.
     * 
     * @param dateTime The LocalDateTime object to convert
     * @return Long timestamp in milliseconds
     */
    fun localDateTimeToLong(dateTime: LocalDateTime): Long = 
        dateTime.toEpochSecond(ZoneOffset.UTC) * 1000
    
    /**
     * Converts a Long timestamp to Date object.
     * 
     * @param timestamp The timestamp in milliseconds since epoch
     * @return Date object representing the timestamp
     */
    fun longToDate(timestamp: Long): Date = Date(timestamp)
    
    /**
     * Converts a Long timestamp to LocalDateTime object.
     * Uses UTC timezone for consistent handling.
     * 
     * @param timestamp The timestamp in milliseconds since epoch
     * @return LocalDateTime object representing the timestamp in UTC
     */
    fun longToLocalDateTime(timestamp: Long): LocalDateTime = 
        LocalDateTime.ofEpochSecond(timestamp / 1000, 0, ZoneOffset.UTC)
    
    /**
     * Gets the current timestamp as Long (milliseconds since epoch).
     * 
     * @return Current timestamp in milliseconds
     */
    fun getCurrentTimestamp(): Long = System.currentTimeMillis()
    
    /**
     * Gets the current time as Date object.
     * 
     * @return Current Date object
     */
    fun getCurrentDate(): Date = Date()
    
    /**
     * Gets the current time as LocalDateTime in UTC.
     * 
     * @return Current LocalDateTime in UTC
     */
    fun getCurrentLocalDateTime(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)
    
    /**
     * Converts LocalDateTime to epoch milliseconds (alternative method name for compatibility).
     * 
     * @param dateTime The LocalDateTime object to convert
     * @return Long timestamp in milliseconds since epoch
     */
    fun toEpochMilli(dateTime: LocalDateTime): Long = localDateTimeToLong(dateTime)
    
    /**
     * Checks if the first date is after the second date.
     * 
     * @param date1 The first date to compare
     * @param date2 The second date to compare
     * @return true if date1 is after date2, false otherwise
     */
    fun isAfter(date1: Date, date2: Date): Boolean = date1.after(date2)
    
    /**
     * Checks if the first LocalDateTime is after the second LocalDateTime.
     * 
     * @param dateTime1 The first LocalDateTime to compare
     * @param dateTime2 The second LocalDateTime to compare
     * @return true if dateTime1 is after dateTime2, false otherwise
     */
    fun isAfter(dateTime1: LocalDateTime, dateTime2: LocalDateTime): Boolean = dateTime1.isAfter(dateTime2)
    
    /**
     * Checks if the first timestamp is after the second timestamp.
     * 
     * @param timestamp1 The first timestamp in milliseconds
     * @param timestamp2 The second timestamp in milliseconds
     * @return true if timestamp1 is after timestamp2, false otherwise
     */
    fun isAfter(timestamp1: Long, timestamp2: Long): Boolean = timestamp1 > timestamp2
}

/**
 * Extension functions for convenient time conversions.
 * These provide a more fluent API for common conversion operations.
 */

/**
 * Extension function to convert Date to Long timestamp.
 */
fun Date.toLong(): Long = TimeUtils.dateToLong(this)

/**
 * Extension function to convert LocalDateTime to Long timestamp.
 */
fun LocalDateTime.toLong(): Long = TimeUtils.localDateTimeToLong(this)

/**
 * Extension function to convert Long timestamp to Date.
 */
fun Long.toDate(): Date = TimeUtils.longToDate(this)

/**
 * Extension function to convert Long timestamp to LocalDateTime.
 */
fun Long.toLocalDateTime(): LocalDateTime = TimeUtils.longToLocalDateTime(this)

/**
 * Extension function to convert LocalDateTime to epoch milliseconds.
 */
fun LocalDateTime.toEpochMilli(): Long = TimeUtils.toEpochMilli(this)

/**
 * Extension function to check if this Date is after another Date.
 */
fun Date.isAfter(other: Date): Boolean = TimeUtils.isAfter(this, other)

/**
 * Extension function to check if this LocalDateTime is after another LocalDateTime.
 */
fun LocalDateTime.isAfter(other: LocalDateTime): Boolean = TimeUtils.isAfter(this, other)

/**
 * Extension function to check if this timestamp is after another timestamp.
 */
fun Long.isAfter(other: Long): Boolean = TimeUtils.isAfter(this, other)