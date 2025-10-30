package com.chain.messaging.presentation.settings;

import com.chain.messaging.core.auth.AuthenticationService;
import com.chain.messaging.core.profile.ProfileImageManager;
import com.chain.messaging.domain.usecase.GetUserSettingsUseCase;
import com.chain.messaging.domain.usecase.UpdateUserSettingsUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<GetUserSettingsUseCase> getUserSettingsUseCaseProvider;

  private final Provider<UpdateUserSettingsUseCase> updateUserSettingsUseCaseProvider;

  private final Provider<AuthenticationService> authenticationServiceProvider;

  private final Provider<ProfileImageManager> profileImageManagerProvider;

  public SettingsViewModel_Factory(Provider<GetUserSettingsUseCase> getUserSettingsUseCaseProvider,
      Provider<UpdateUserSettingsUseCase> updateUserSettingsUseCaseProvider,
      Provider<AuthenticationService> authenticationServiceProvider,
      Provider<ProfileImageManager> profileImageManagerProvider) {
    this.getUserSettingsUseCaseProvider = getUserSettingsUseCaseProvider;
    this.updateUserSettingsUseCaseProvider = updateUserSettingsUseCaseProvider;
    this.authenticationServiceProvider = authenticationServiceProvider;
    this.profileImageManagerProvider = profileImageManagerProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(getUserSettingsUseCaseProvider.get(), updateUserSettingsUseCaseProvider.get(), authenticationServiceProvider.get(), profileImageManagerProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<GetUserSettingsUseCase> getUserSettingsUseCaseProvider,
      Provider<UpdateUserSettingsUseCase> updateUserSettingsUseCaseProvider,
      Provider<AuthenticationService> authenticationServiceProvider,
      Provider<ProfileImageManager> profileImageManagerProvider) {
    return new SettingsViewModel_Factory(getUserSettingsUseCaseProvider, updateUserSettingsUseCaseProvider, authenticationServiceProvider, profileImageManagerProvider);
  }

  public static SettingsViewModel newInstance(GetUserSettingsUseCase getUserSettingsUseCase,
      UpdateUserSettingsUseCase updateUserSettingsUseCase,
      AuthenticationService authenticationService, ProfileImageManager profileImageManager) {
    return new SettingsViewModel(getUserSettingsUseCase, updateUserSettingsUseCase, authenticationService, profileImageManager);
  }
}
