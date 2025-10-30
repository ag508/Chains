package com.chain.messaging.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.chain.messaging.core.cloud.OAuthCallbackHandler
import com.chain.messaging.presentation.navigation.ChainNavigation
import com.chain.messaging.presentation.theme.ChainTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main activity for the Chain messaging app.
 * Entry point for the application UI.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var oAuthCallbackHandler: OAuthCallbackHandler
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle OAuth callback if present
        handleOAuthCallback(intent)
        
        setContent {
            ChainTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChainApp()
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleOAuthCallback(it) }
    }
    
    private fun handleOAuthCallback(intent: Intent) {
        oAuthCallbackHandler.handleCallback(intent)
    }
}

@Composable
fun ChainApp() {
    val navController = rememberNavController()
    ChainNavigation(navController = navController)
}

@Preview(showBackground = true)
@Composable
fun ChainAppPreview() {
    ChainTheme {
        ChainApp()
    }
}