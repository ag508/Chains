package com.chain.messaging.presentation.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.messaging.core.auth.AuthenticationService
import com.chain.messaging.core.profile.ProfileImageManager
import com.chain.messaging.domain.model.*
import com.chain.messaging.domain.usecase.GetUserSettingsUseCase
import com.chain.messaging.domain.usecase.UpdateUserSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for settings screen
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val updateUserSettingsUseCase: UpdateUserSettingsUseCase,
    private val authenticationService: AuthenticationService,
    private val profileImageManager: ProfileImageManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    private val _currentUserId = MutableStateFlow<String?>(null)
    
    init {
        // Observe settings changes
        _currentUserId.filterNotNull().flatMapLatest { userId ->
            getUserSettingsUseCase.asFlow(userId)
        }.onEach { settings ->
            _uiState.value = _uiState.value.copy(
                settings = settings,
                isLoading = false
            )
        }.launchIn(viewModelScope)
    }
    
    fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentUser = authenticationService.getCurrentUser()
                if (currentUser != null) {
                    _currentUserId.value = currentUser.userId
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No authenticated user found. Please log in again."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to get current user: ${e.message}"
                )
            }
        }
    }
    
    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentUser = authenticationService.getCurrentUser()
                if (currentUser != null) {
                    val result = updateUserSettingsUseCase.updateProfile(currentUser.userId, profile)
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = if (result.isFailure) result.exceptionOrNull()?.message else null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No authenticated user found. Please log in again."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update profile: ${e.message}"
                )
            }
        }
    }
    
    fun updatePrivacySettings(privacy: PrivacySettings) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentUser = authenticationService.getCurrentUser()
                if (currentUser != null) {
                    val result = updateUserSettingsUseCase.updatePrivacySettings(currentUser.userId, privacy)
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = if (result.isFailure) result.exceptionOrNull()?.message else null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No authenticated user found. Please log in again."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update privacy settings: ${e.message}"
                )
            }
        }
    }
    
    fun updateNotificationSettings(notifications: NotificationSettings) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentUser = authenticationService.getCurrentUser()
                if (currentUser != null) {
                    val result = updateUserSettingsUseCase.updateNotificationSettings(currentUser.userId, notifications)
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = if (result.isFailure) result.exceptionOrNull()?.message else null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No authenticated user found. Please log in again."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update notification settings: ${e.message}"
                )
            }
        }
    }
    
    fun updateAppearanceSettings(appearance: AppearanceSettings) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentUser = authenticationService.getCurrentUser()
                if (currentUser != null) {
                    val result = updateUserSettingsUseCase.updateAppearanceSettings(currentUser.userId, appearance)
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = if (result.isFailure) result.exceptionOrNull()?.message else null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No authenticated user found. Please log in again."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update appearance settings: ${e.message}"
                )
            }
        }
    }
    
    fun updateAccessibilitySettings(accessibility: AccessibilitySettings) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentUser = authenticationService.getCurrentUser()
                if (currentUser != null) {
                    val result = updateUserSettingsUseCase.updateAccessibilitySettings(currentUser.userId, accessibility)
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = if (result.isFailure) result.exceptionOrNull()?.message else null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No authenticated user found. Please log in again."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update accessibility settings: ${e.message}"
                )
            }
        }
    }
    
    fun createDefaultSettings(userId: String, displayName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = updateUserSettingsUseCase.createDefaultSettings(userId, displayName)
            
            if (result.isSuccess) {
                _currentUserId.value = userId
            }
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = if (result.isFailure) result.exceptionOrNull()?.message else null
            )
        }
    }
    
    fun updateProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentUser = authenticationService.getCurrentUser()
                if (currentUser != null) {
                    // Save the image and get the file path
                    val imageResult = profileImageManager.saveProfileImage(imageUri, currentUser.userId)
                    
                    if (imageResult.isSuccess) {
                        val imagePath = imageResult.getOrNull()
                        
                        // Update the profile with the new image path
                        val currentProfile = _uiState.value.settings?.profile
                        if (currentProfile != null) {
                            val updatedProfile = currentProfile.copy(avatar = imagePath)
                            val result = updateUserSettingsUseCase.updateProfile(currentUser.userId, updatedProfile)
                            
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = if (result.isFailure) result.exceptionOrNull()?.message else null
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Profile not found. Please try again."
                            )
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to save image: ${imageResult.exceptionOrNull()?.message}"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No authenticated user found. Please log in again."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update profile image: ${e.message}"
                )
            }
        }
    }
    
    fun getProfileImageFile(imagePath: String): java.io.File? {
        return profileImageManager.getProfileImageFile(imagePath)
    }
    
    fun updateWallpaper(wallpaperPath: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentUser = authenticationService.getCurrentUser()
                if (currentUser != null) {
                    val currentAppearance = _uiState.value.settings?.appearance
                    if (currentAppearance != null) {
                        val updatedAppearance = currentAppearance.copy(chatWallpaper = wallpaperPath)
                        val result = updateUserSettingsUseCase.updateAppearanceSettings(currentUser.userId, updatedAppearance)
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = if (result.isFailure) result.exceptionOrNull()?.message else null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Appearance settings not found. Please try again."
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No authenticated user found. Please log in again."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update wallpaper: ${e.message}"
                )
            }
        }
    }
    
    fun resetWallpaper() {
        updateWallpaper(null)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI state for settings screen
 */
data class SettingsUiState(
    val settings: UserSettings? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)