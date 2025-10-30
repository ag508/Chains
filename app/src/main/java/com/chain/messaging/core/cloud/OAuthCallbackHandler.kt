package com.chain.messaging.core.cloud

import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles OAuth callback results from deep links
 */
@Singleton
class OAuthCallbackHandler @Inject constructor() {
    
    private val _callbackChannel = Channel<OAuthCallback>(Channel.UNLIMITED)
    val callbackFlow: Flow<OAuthCallback> = _callbackChannel.receiveAsFlow()
    
    /**
     * Handle OAuth callback from deep link
     */
    fun handleCallback(intent: Intent): Boolean {
        val uri = intent.data ?: return false
        
        return when (uri.scheme) {
            "com.chain.messaging" -> {
                if (uri.host == "oauth" && uri.path == "/callback") {
                    processOAuthCallback(uri)
                    true
                } else {
                    false
                }
            }
            else -> false
        }
    }
    
    private fun processOAuthCallback(uri: Uri) {
        val code = uri.getQueryParameter("code")
        val error = uri.getQueryParameter("error")
        val state = uri.getQueryParameter("state")
        
        val callback = when {
            error != null -> {
                val errorDescription = uri.getQueryParameter("error_description") ?: error
                OAuthCallback.Error(error, errorDescription)
            }
            code != null -> {
                OAuthCallback.Success(code, state)
            }
            else -> {
                OAuthCallback.Error("unknown_error", "No code or error in callback")
            }
        }
        
        _callbackChannel.trySend(callback)
    }
}

/**
 * Represents the result of an OAuth callback
 */
sealed class OAuthCallback {
    data class Success(val code: String, val state: String?) : OAuthCallback()
    data class Error(val error: String, val description: String) : OAuthCallback()
    object Cancelled : OAuthCallback()
}