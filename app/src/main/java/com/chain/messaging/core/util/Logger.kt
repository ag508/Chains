package com.chain.messaging.core.util

import android.util.Log
import com.chain.messaging.core.config.AppConfig

/**
 * Centralized logging utility for the Chain messaging app
 */
object Logger {
    
    private const val TAG = "ChainMessaging"
    private var debugEnabled: Boolean = AppConfig.DEBUG
    
    /**
     * Set debug logging enabled/disabled
     */
    fun setDebugEnabled(enabled: Boolean) {
        debugEnabled = enabled
    }
    
    /**
     * Check if debug logging is enabled
     */
    fun isDebugEnabled(): Boolean = debugEnabled
    
    fun v(message: String, throwable: Throwable? = null) {
        if (debugEnabled) {
            if (throwable != null) {
                Log.v(TAG, message, throwable)
            } else {
                Log.v(TAG, message)
            }
        }
    }
    
    fun d(message: String, throwable: Throwable? = null) {
        if (debugEnabled) {
            if (throwable != null) {
                Log.d(TAG, message, throwable)
            } else {
                Log.d(TAG, message)
            }
        }
    }
    
    fun i(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.i(TAG, message, throwable)
        } else {
            Log.i(TAG, message)
        }
    }
    
    fun w(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.w(TAG, message, throwable)
        } else {
            Log.w(TAG, message)
        }
    }
    
    fun e(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(TAG, message, throwable)
        } else {
            Log.e(TAG, message)
        }
    }
    
    fun wtf(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.wtf(TAG, message, throwable)
        } else {
            Log.wtf(TAG, message)
        }
    }
    
    /**
     * Log performance metrics
     */
    fun performance(operation: String, duration: Long, additionalInfo: String = "") {
        val message = "PERFORMANCE: $operation took ${duration}ms $additionalInfo".trim()
        if (debugEnabled) {
            Log.d("$TAG-Performance", message)
        }
    }
    
    /**
     * Log memory usage
     */
    fun memory(operation: String, beforeMb: Long, afterMb: Long) {
        val delta = afterMb - beforeMb
        val message = "MEMORY: $operation - Before: ${beforeMb}MB, After: ${afterMb}MB, Delta: ${delta}MB"
        if (debugEnabled) {
            Log.d("$TAG-Memory", message)
        }
    }
    
    /**
     * Log network operations
     */
    fun network(operation: String, duration: Long, bytes: Long = 0) {
        val message = if (bytes > 0) {
            "NETWORK: $operation took ${duration}ms, transferred ${bytes} bytes"
        } else {
            "NETWORK: $operation took ${duration}ms"
        }
        if (debugEnabled) {
            Log.d("$TAG-Network", message)
        }
    }
}