package com.chain.messaging.core.security

import android.graphics.Bitmap
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scans QR codes for identity verification
 */
@Singleton
class QRCodeScanner @Inject constructor() {
    
    private val reader = MultiFormatReader().apply {
        val hints = hashMapOf<DecodeHintType, Any>().apply {
            put(DecodeHintType.POSSIBLE_FORMATS, listOf(com.google.zxing.BarcodeFormat.QR_CODE))
            put(DecodeHintType.TRY_HARDER, true)
            put(DecodeHintType.CHARACTER_SET, "UTF-8")
        }
        setHints(hints)
    }
    
    /**
     * Scan QR code from bitmap
     */
    fun scanQRCode(bitmap: Bitmap): Result<String> {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            
            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            
            val result = reader.decode(binaryBitmap)
            Result.success(result.text)
        } catch (e: Exception) {
            Result.failure(QRScanException("Failed to scan QR code", e))
        }
    }
    
    /**
     * Scan QR code from byte array (camera data)
     */
    fun scanQRCode(data: ByteArray, width: Int, height: Int): Result<String> {
        return try {
            // Convert YUV to RGB if needed (simplified implementation)
            val pixels = convertYuvToRgb(data, width, height)
            
            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            
            val result = reader.decode(binaryBitmap)
            Result.success(result.text)
        } catch (e: Exception) {
            Result.failure(QRScanException("Failed to scan QR code from camera data", e))
        }
    }
    
    /**
     * Validate if scanned data is a Chain verification QR code
     */
    fun isValidVerificationQR(data: String): Boolean {
        return try {
            val uri = android.net.Uri.parse(data)
            uri.scheme == "chain" && uri.host == "verify" &&
                    uri.getQueryParameter("user") != null &&
                    uri.getQueryParameter("name") != null &&
                    uri.getQueryParameter("key") != null &&
                    uri.getQueryParameter("ts") != null
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Extract verification data from QR code
     */
    fun extractVerificationData(qrData: String): Result<QRVerificationData> {
        return try {
            if (!isValidVerificationQR(qrData)) {
                return Result.failure(QRScanException("Invalid verification QR code format"))
            }
            
            val uri = android.net.Uri.parse(qrData)
            val userId = uri.getQueryParameter("user")!!
            val displayName = uri.getQueryParameter("name")!!
            val keyHash = uri.getQueryParameter("key")!!
            val timestamp = uri.getQueryParameter("ts")!!.toLong()
            
            val verificationData = QRVerificationData(
                userId = userId,
                displayName = displayName,
                keyHash = keyHash,
                timestamp = timestamp,
                rawData = qrData
            )
            
            Result.success(verificationData)
        } catch (e: Exception) {
            Result.failure(QRScanException("Failed to extract verification data", e))
        }
    }
    
    /**
     * Check if QR code is expired
     */
    fun isQRCodeExpired(timestamp: Long, maxAgeMillis: Long = 5 * 60 * 1000): Boolean {
        return System.currentTimeMillis() - timestamp > maxAgeMillis
    }
    
    /**
     * Reset the scanner for next scan
     */
    fun reset() {
        reader.reset()
    }
    
    // Private helper methods
    
    /**
     * Convert YUV camera data to RGB (simplified implementation)
     */
    private fun convertYuvToRgb(data: ByteArray, width: Int, height: Int): IntArray {
        val pixels = IntArray(width * height)
        
        // Simplified YUV to RGB conversion
        // In a real implementation, you'd use proper YUV conversion
        for (i in pixels.indices) {
            val y = data[i].toInt() and 0xFF
            val rgb = (y shl 16) or (y shl 8) or y
            pixels[i] = rgb or (0xFF shl 24) // Add alpha channel
        }
        
        return pixels
    }
}

/**
 * Data extracted from verification QR code
 */
data class QRVerificationData(
    val userId: String,
    val displayName: String,
    val keyHash: String,
    val timestamp: Long,
    val rawData: String
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() - timestamp > 5 * 60 * 1000 // 5 minutes
}

/**
 * Exception for QR scanning errors
 */
class QRScanException(message: String, cause: Throwable? = null) : Exception(message, cause)