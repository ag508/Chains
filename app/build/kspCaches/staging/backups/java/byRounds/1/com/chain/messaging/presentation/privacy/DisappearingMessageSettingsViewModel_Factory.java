package com.chain.messaging.presentation.privacy;

import com.chain.messaging.core.privacy.DisappearingMessageManager;
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
public final class DisappearingMessageSettingsViewModel_Factory implements Factory<DisappearingMessageSettingsViewModel> {
  private final Provider<DisappearingMessageManager> disappearingMessageManagerProvider;

  public DisappearingMessageSettingsViewModel_Factory(
      Provider<DisappearingMessageManager> disappearingMessageManagerProvider) {
    this.disappearingMessageManagerProvider = disappearingMessageManagerProvider;
  }

  @Override
  public DisappearingMessageSettingsViewModel get() {
    return newInstance(disappearingMessageManagerProvider.get());
  }

  public static DisappearingMessageSettingsViewModel_Factory create(
      Provider<DisappearingMessageManager> disappearingMessageManagerProvider) {
    return new DisappearingMessageSettingsViewModel_Factory(disappearingMessageManagerProvider);
  }

  public static DisappearingMessageSettingsViewModel newInstance(
      DisappearingMessageManager disappearingMessageManager) {
    return new DisappearingMessageSettingsViewModel(disappearingMessageManager);
  }
}
