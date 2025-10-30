package com.chain.messaging.di;

import com.chain.messaging.domain.repository.SettingsRepository;
import com.chain.messaging.domain.usecase.UpdateUserSettingsUseCase;
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
public final class UseCaseModule_ProvideUpdateUserSettingsUseCaseFactory implements Factory<UpdateUserSettingsUseCase> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public UseCaseModule_ProvideUpdateUserSettingsUseCaseFactory(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public UpdateUserSettingsUseCase get() {
    return provideUpdateUserSettingsUseCase(settingsRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideUpdateUserSettingsUseCaseFactory create(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new UseCaseModule_ProvideUpdateUserSettingsUseCaseFactory(settingsRepositoryProvider);
  }

  public static UpdateUserSettingsUseCase provideUpdateUserSettingsUseCase(
      SettingsRepository settingsRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideUpdateUserSettingsUseCase(settingsRepository));
  }
}
