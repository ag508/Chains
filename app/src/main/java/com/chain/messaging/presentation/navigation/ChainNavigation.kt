package com.chain.messaging.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.chain.messaging.presentation.chatlist.ChatListScreen
import com.chain.messaging.presentation.call.CallHistoryScreen
import com.chain.messaging.presentation.settings.SettingsScreen
import com.chain.messaging.presentation.chat.ChatScreen

/**
 * Navigation setup for Chain messaging app with bottom navigation
 * Implements Requirements: 11.1, 11.3
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChainNavigation(
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        bottomBar = {
            ChainBottomNavigation(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.ChatList.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.ChatList.route) {
                ChatListScreen(
                    onChatClick = { chatId ->
                        navController.navigate("${Screen.Chat.route}/$chatId")
                    }
                )
            }
            
            composable(Screen.Calls.route) {
                CallHistoryScreen(
                    onCallUser = { userId, isVideo ->
                        // Navigate to call screen or initiate call
                        // This could be implemented when call initiation is needed
                    }
                )
            }
            
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToProfile = {
                        // Navigate to profile settings
                        // This could be implemented when profile navigation is needed
                    },
                    onNavigateToPrivacy = {
                        // Navigate to privacy settings
                        // This could be implemented when privacy navigation is needed
                    },
                    onNavigateToNotifications = {
                        // Navigate to notification settings
                        // This could be implemented when notification navigation is needed
                    },
                    onNavigateToAppearance = {
                        // Navigate to appearance settings
                        // This could be implemented when appearance navigation is needed
                    },
                    onNavigateToAccessibility = {
                        // Navigate to accessibility settings
                        // This could be implemented when accessibility navigation is needed
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable("${Screen.Chat.route}/{chatId}") { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                ChatScreen(
                    chatId = chatId,
                    chatName = "Chat", // This could be passed as a parameter or loaded from the chat repository
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
private fun ChainBottomNavigation(
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    NavigationBar {
        bottomNavItems.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (currentDestination?.hierarchy?.any { it.route == screen.route } == true) {
                            screen.selectedIcon
                        } else {
                            screen.unselectedIcon
                        },
                        contentDescription = screen.title
                    )
                },
                label = { Text(screen.title) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}

/**
 * Screen definitions for navigation
 */
sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object ChatList : Screen(
        route = "chat_list",
        title = "Chats",
        selectedIcon = Icons.Filled.Chat,
        unselectedIcon = Icons.Outlined.Chat
    )
    
    object Calls : Screen(
        route = "calls",
        title = "Calls",
        selectedIcon = Icons.Filled.Call,
        unselectedIcon = Icons.Outlined.Call
    )
    
    object Settings : Screen(
        route = "settings",
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
    
    object Chat : Screen(
        route = "chat",
        title = "Chat",
        selectedIcon = Icons.Filled.Chat,
        unselectedIcon = Icons.Outlined.Chat
    )
}

private val bottomNavItems = listOf(
    Screen.ChatList,
    Screen.Calls,
    Screen.Settings
)

