package com.chain.messaging.core.security

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates QR codes for identity verification
 */
@Singleton
class QRCodeGenerator @Inject constructor() {
    
    companion object {
        private const val DEFAULT_QR_SIZE = 512
        private const val DEFAULT_MARGIN = 1
    }
    
    /**
     * Generate a QR code bitmap from the given data
     */
    fun generateQRCode(
        data: String,
        size: Int = DEFAULT_QR_SIZE,
        margin: Int = DEFAULT_MARGIN
    ): Bitmap {
        return try {
            val hints = hashMapOf<EncodeHintType, Any>().apply {
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M)
                put(EncodeHintType.MARGIN, margin)
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
            }
            
            val writer = MultiFormatWriter()
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size, hints)
            
            createBitmapFromBitMatrix(bitMatrix)
        } catch (e: WriterException) {
            throw QRCodeException("Failed to generate QR code", e)
        }
    }
    
    /**
     * Generate a QR code with custom colors
     */
    fun generateQRCode(
        data: String,
        size: Int = DEFAULT_QR_SIZE,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE,
        margin: Int = DEFAULT_MARGIN
    ): Bitmap {
        return try {
            val hints = hashMapOf<EncodeHintType, Any>().apply {
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M)
                put(EncodeHintType.MARGIN, margin)
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
            }
            
            val writer = MultiFormatWriter()
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size, hints)
            
            createBitmapFromBitMatrix(bitMatrix, foregroundColor, backgroundColor)
        } catch (e: WriterException) {
            throw QRCodeException("Failed to generate QR code", e)
        }
    }
    
    /**
     * Create a bitmap from a BitMatrix
     */
    private fun createBitmapFromBitMatrix(
        bitMatrix: BitMatrix,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE
    ): Bitmap {
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)
        
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix[x, y]) foregroundColor else backgroundColor
            }
        }
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }
    
    /**
     * Validate QR code data format
     */
    fun validateQRData(data: String): Boolean {
        return try {
            // Check if it's a valid Chain verification URL
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
     * Get optimal QR code size based on data length
     */
    fun getOptimalSize(dataLength: Int): Int {
        return when {
            dataLength < 100 -> 256
            dataLength < 200 -> 384
            dataLength < 300 -> 512
            else -> 768
        }
    }
}

/**
 * Exception for QR code generation errors
 */
class QRCodeException(message: String, cause: Throwable? = null) : Exception(message, cause)