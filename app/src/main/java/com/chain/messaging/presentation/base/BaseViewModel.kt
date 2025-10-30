package com.chain.messaging.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel class with common functionality
 */
abstract class BaseViewModel<T : UiState> : ViewModel() {
    
    protected abstract val initialState: T
    
    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<T> = _uiState.asStateFlow()
    
    protected fun updateState(update: T.() -> T) {
        _uiState.value = _uiState.value.update()
    }
    
    protected fun launchSafe(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    protected open fun handleError(error: Exception) {
        // Default error handling - can be overridden by subclasses
        error.printStackTrace()
    }
}

/**
 * Base interface for UI states
 */
interface UiState

/**
 * Common UI state properties
 */
data class CommonUiState(
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState