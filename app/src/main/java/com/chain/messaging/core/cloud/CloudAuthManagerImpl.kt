package com.chain.messaging.core.cloud

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.chain.messaging.core.security.SecureStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudAuthManagerImpl @Inject constructor(
    private val context: Context,
    private val secureStorage: SecureStorage,
    private val httpClient: OkHttpClient,
    private val json: Json
) : CloudAuthManager {
    
    private val authStateMutex = Mutex()
    private val _authStateFlow = MutableStateFlow<Map<CloudService, Boolean>>(emptyMap())
    
    companion object {
        private const val TOKEN_STORAGE_PREFIX = "cloud_token_"
        private const val ACCOUNT_STORAGE_PREFIX = "cloud_account_"
        private const val REDIRECT_URI = "com.chain.messaging://oauth/callback"
    }
    
    override suspend fun initializeService(service: CloudService) {
        // Initialize service-specific configurations
        // This could include setting up service-specific parameters, 
        // validating client credentials, etc.
        // For now, this is a no-op as initialization is handled in other methods
    }
    
    override suspend fun authenticate(service: CloudService): AuthResult {
        return try {
            val authUrl = buildAuthUrl(service)
            val authCode = performOAuthFlow(authUrl)
            
            if (authCode != null) {
                exchangeCodeForToken(service, authCode)
            } else {
                AuthResult.Cancelled
            }
        } catch (e: Exception) {
            AuthResult.Error("Authentication failed: ${e.message}", e)
        }
    }
    
    override suspend fun refreshToken(service: CloudService): AuthResult {
        return try {
            val currentToken = getToken(service)
                ?: return AuthResult.Error("No token found for ${service.displayName}")
            
            if (!currentToken.canRefresh()) {
                return AuthResult.Error("Token cannot be refreshed")
            }
            
            val refreshedToken = performTokenRefresh(service, currentToken.refreshToken!!)
            if (refreshedToken != null) {
                storeToken(service, refreshedToken)
                updateAuthState(service, true)
                AuthResult.Success(refreshedToken)
            } else {
                AuthResult.Error("Failed to refresh token")
            }
        } catch (e: Exception) {
            AuthResult.Error("Token refresh failed: ${e.message}", e)
        }
    }
    
    override suspend fun signOut(service: CloudService): Boolean {
        return try {
            // Revoke token on server if possible
            val token = getToken(service)
            if (token != null) {
                revokeTokenOnServer(service, token)
            }
            
            // Clear local storage
            secureStorage.remove("${TOKEN_STORAGE_PREFIX}${service.name}")
            secureStorage.remove("${ACCOUNT_STORAGE_PREFIX}${service.name}")
            
            updateAuthState(service, false)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getToken(service: CloudService): AuthToken? {
        return try {
            val tokenJson = secureStorage.get("${TOKEN_STORAGE_PREFIX}${service.name}")
            if (tokenJson != null) {
                val token = json.decodeFromString<AuthToken>(tokenJson)
                
                // Auto-refresh if expired and possible
                if (token.isExpired() && token.canRefresh()) {
                    val refreshResult = refreshToken(service)
                    if (refreshResult is AuthResult.Success) {
                        return refreshResult.token
                    }
                }
                
                token
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getAccount(service: CloudService): CloudAccount? {
        return try {
            val accountJson = secureStorage.get("${ACCOUNT_STORAGE_PREFIX}${service.name}")
            accountJson?.let { json.decodeFromString<CloudAccount>(it) }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getAuthenticatedAccounts(): List<CloudAccount> {
        return CloudService.values().mapNotNull { service ->
            getAccount(service)
        }
    }
    
    override suspend fun isAuthenticated(service: CloudService): Boolean {
        val token = getToken(service)
        return token != null && !token.isExpired()
    }
    
    override fun observeAuthState(): Flow<Map<CloudService, Boolean>> {
        return _authStateFlow.asStateFlow()
    }
    
    override suspend fun revokeAllTokens() {
        CloudService.values().forEach { service ->
            signOut(service)
        }
    }
    
    private fun buildAuthUrl(service: CloudService): String {
        val state = UUID.randomUUID().toString()
        
        return when (service) {
            CloudService.GOOGLE_DRIVE -> {
                "https://accounts.google.com/o/oauth2/v2/auth?" +
                        "client_id=${service.clientId}&" +
                        "redirect_uri=$REDIRECT_URI&" +
                        "scope=${service.scopes.joinToString(" ")}&" +
                        "response_type=code&" +
                        "state=$state&" +
                        "access_type=offline&" +
                        "prompt=consent"
            }
            CloudService.ONEDRIVE -> {
                "https://login.microsoftonline.com/common/oauth2/v2.0/authorize?" +
                        "client_id=${service.clientId}&" +
                        "redirect_uri=$REDIRECT_URI&" +
                        "scope=${service.scopes.joinToString(" ")}&" +
                        "response_type=code&" +
                        "state=$state&" +
                        "response_mode=query"
            }
            CloudService.ICLOUD -> {
                // iCloud uses a different OAuth flow
                "https://appleid.apple.com/auth/authorize?" +
                        "client_id=${service.clientId}&" +
                        "redirect_uri=$REDIRECT_URI&" +
                        "scope=${service.scopes.joinToString(" ")}&" +
                        "response_type=code&" +
                        "state=$state&" +
                        "response_mode=form_post"
            }
            CloudService.DROPBOX -> {
                "https://www.dropbox.com/oauth2/authorize?" +
                        "client_id=${service.clientId}&" +
                        "redirect_uri=$REDIRECT_URI&" +
                        "response_type=code&" +
                        "state=$state"
            }
        }
    }
    
    private suspend fun performOAuthFlow(authUrl: String): String? {
        return try {
            // Launch Custom Tab for OAuth
            val intent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            
            intent.launchUrl(context, Uri.parse(authUrl))
            
            // In a real implementation, you would need to handle the callback
            // This is a simplified version - you'd typically use a callback mechanism
            // or deep link handling to capture the authorization code
            null // Placeholder - implement callback handling
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun exchangeCodeForToken(service: CloudService, authCode: String): AuthResult {
        return try {
            val tokenUrl = getTokenUrl(service)
            val requestBody = buildTokenRequest(service, authCode)
            
            val request = Request.Builder()
                .url(tokenUrl)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build()
            
            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val token = parseTokenResponse(responseBody)
                
                if (token != null) {
                    storeToken(service, token)
                    
                    // Fetch and store account info
                    val account = fetchAccountInfo(service, token)
                    if (account != null) {
                        storeAccount(service, account)
                    }
                    
                    updateAuthState(service, true)
                    AuthResult.Success(token)
                } else {
                    AuthResult.Error("Failed to parse token response")
                }
            } else {
                AuthResult.Error("Token exchange failed: ${response.code}")
            }
        } catch (e: Exception) {
            AuthResult.Error("Token exchange error: ${e.message}", e)
        }
    }
    
    private fun getTokenUrl(service: CloudService): String {
        return when (service) {
            CloudService.GOOGLE_DRIVE -> "https://oauth2.googleapis.com/token"
            CloudService.ONEDRIVE -> "https://login.microsoftonline.com/common/oauth2/v2.0/token"
            CloudService.ICLOUD -> "https://appleid.apple.com/auth/token"
            CloudService.DROPBOX -> "https://api.dropboxapi.com/oauth2/token"
        }
    }
    
    private fun buildTokenRequest(service: CloudService, authCode: String): RequestBody {
        val params = mutableMapOf(
            "client_id" to service.clientId,
            "code" to authCode,
            "redirect_uri" to REDIRECT_URI,
            "grant_type" to "authorization_code"
        )
        
        // Add client secret if needed (should be stored securely)
        when (service) {
            CloudService.GOOGLE_DRIVE -> {
                // Google requires client_secret
                params["client_secret"] = "your-google-client-secret"
            }
            CloudService.ONEDRIVE -> {
                // Microsoft requires client_secret
                params["client_secret"] = "your-onedrive-client-secret"
            }
            CloudService.DROPBOX -> {
                // Dropbox requires client_secret
                params["client_secret"] = "your-dropbox-client-secret"
            }
            CloudService.ICLOUD -> {
                // iCloud uses different authentication
            }
        }
        
        val formBody = params.entries.joinToString("&") { "${it.key}=${it.value}" }
        return formBody.toRequestBody("application/x-www-form-urlencoded".toMediaType())
    }
    
    private fun parseTokenResponse(responseBody: String?): AuthToken? {
        return try {
            if (responseBody == null) return null
            
            val tokenData = json.parseToJsonElement(responseBody).jsonObject
            val accessToken = tokenData["access_token"]?.jsonPrimitive?.content ?: return null
            val refreshToken = tokenData["refresh_token"]?.jsonPrimitive?.contentOrNull
            val expiresIn = tokenData["expires_in"]?.jsonPrimitive?.content?.toLongOrNull() ?: 3600L
            val tokenType = tokenData["token_type"]?.jsonPrimitive?.content ?: "Bearer"
            val scope = tokenData["scope"]?.jsonPrimitive?.contentOrNull
            
            AuthToken(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresAt = Instant.now().plusSeconds(expiresIn),
                tokenType = tokenType,
                scope = scope
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun performTokenRefresh(service: CloudService, refreshToken: String): AuthToken? {
        return try {
            val tokenUrl = getTokenUrl(service)
            val params = mapOf(
                "client_id" to service.clientId,
                "refresh_token" to refreshToken,
                "grant_type" to "refresh_token"
            )
            
            val formBody = params.entries.joinToString("&") { "${it.key}=${it.value}" }
            val requestBody = formBody.toRequestBody("application/x-www-form-urlencoded".toMediaType())
            
            val request = Request.Builder()
                .url(tokenUrl)
                .post(requestBody)
                .build()
            
            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                parseTokenResponse(responseBody)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun fetchAccountInfo(service: CloudService, token: AuthToken): CloudAccount? {
        return try {
            val userInfoUrl = getUserInfoUrl(service)
            val request = Request.Builder()
                .url(userInfoUrl)
                .addHeader("Authorization", "${token.tokenType} ${token.accessToken}")
                .build()
            
            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                parseUserInfo(service, responseBody, token)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getUserInfoUrl(service: CloudService): String {
        return when (service) {
            CloudService.GOOGLE_DRIVE -> "https://www.googleapis.com/oauth2/v2/userinfo"
            CloudService.ONEDRIVE -> "https://graph.microsoft.com/v1.0/me"
            CloudService.ICLOUD -> "https://appleid.apple.com/auth/userinfo"
            CloudService.DROPBOX -> "https://api.dropboxapi.com/2/users/get_current_account"
        }
    }
    
    private fun parseUserInfo(service: CloudService, responseBody: String?, token: AuthToken): CloudAccount? {
        return try {
            if (responseBody == null) return null
            
            val userData = json.parseToJsonElement(responseBody).jsonObject
            
            when (service) {
                CloudService.GOOGLE_DRIVE -> {
                    CloudAccount(
                        service = service,
                        userId = userData["id"]?.jsonPrimitive?.content ?: "",
                        email = userData["email"]?.jsonPrimitive?.content ?: "",
                        displayName = userData["name"]?.jsonPrimitive?.content ?: "",
                        token = token
                    )
                }
                CloudService.ONEDRIVE -> {
                    CloudAccount(
                        service = service,
                        userId = userData["id"]?.jsonPrimitive?.content ?: "",
                        email = userData["mail"]?.jsonPrimitive?.content 
                            ?: userData["userPrincipalName"]?.jsonPrimitive?.content ?: "",
                        displayName = userData["displayName"]?.jsonPrimitive?.content ?: "",
                        token = token
                    )
                }
                CloudService.DROPBOX -> {
                    CloudAccount(
                        service = service,
                        userId = userData["account_id"]?.jsonPrimitive?.content ?: "",
                        email = userData["email"]?.jsonPrimitive?.content ?: "",
                        displayName = userData["name"]?.jsonObject?.get("display_name")?.jsonPrimitive?.content ?: "",
                        token = token
                    )
                }
                CloudService.ICLOUD -> {
                    CloudAccount(
                        service = service,
                        userId = userData["sub"]?.jsonPrimitive?.content ?: "",
                        email = userData["email"]?.jsonPrimitive?.content ?: "",
                        displayName = userData["name"]?.jsonPrimitive?.content ?: "",
                        token = token
                    )
                }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun revokeTokenOnServer(service: CloudService, token: AuthToken) {
        try {
            val revokeUrl = getRevokeUrl(service)
            if (revokeUrl != null) {
                val request = Request.Builder()
                    .url("$revokeUrl?token=${token.accessToken}")
                    .post("".toRequestBody())
                    .build()
                
                httpClient.newCall(request).execute()
            }
        } catch (e: Exception) {
            // Ignore revocation errors
        }
    }
    
    private fun getRevokeUrl(service: CloudService): String? {
        return when (service) {
            CloudService.GOOGLE_DRIVE -> "https://oauth2.googleapis.com/revoke"
            CloudService.ONEDRIVE -> null // Microsoft doesn't have a revoke endpoint
            CloudService.DROPBOX -> "https://api.dropboxapi.com/2/auth/token/revoke"
            CloudService.ICLOUD -> null // Apple doesn't have a revoke endpoint
        }
    }
    
    private suspend fun storeToken(service: CloudService, token: AuthToken) {
        val tokenJson = json.encodeToString(token)
        secureStorage.store("${TOKEN_STORAGE_PREFIX}${service.name}", tokenJson)
    }
    
    private suspend fun storeAccount(service: CloudService, account: CloudAccount) {
        val accountJson = json.encodeToString(account)
        secureStorage.store("${ACCOUNT_STORAGE_PREFIX}${service.name}", accountJson)
    }
    
    private suspend fun updateAuthState(service: CloudService, isAuthenticated: Boolean) {
        authStateMutex.withLock {
            val currentState = _authStateFlow.value.toMutableMap()
            currentState[service] = isAuthenticated
            _authStateFlow.value = currentState
        }
    }
}