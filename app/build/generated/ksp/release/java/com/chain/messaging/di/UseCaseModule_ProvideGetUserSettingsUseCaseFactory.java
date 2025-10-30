package com.chain.messaging.di;

import com.chain.messaging.domain.repository.SettingsRepository;
import com.chain.messaging.domain.usecase.GetUserSettingsUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class UseCaseModule_ProvideGetUserSettingsUseCaseFactory implements Factory<GetUserSettingsUseCase> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public UseCaseModule_ProvideGetUserSettingsUseCaseFactory(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public GetUserSettingsUseCase get() {
    return provideGetUserSettingsUseCase(settingsRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideGetUserSettingsUseCaseFactory create(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new UseCaseModule_ProvideGetUserSettingsUseCaseFactory(settingsRepositoryProvider);
  }

  public static GetUserSettingsUseCase provideGetUserSettingsUseCase(
      SettingsRepository settingsRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideGetUserSettingsUseCase(settingsRepository));
  }
}
