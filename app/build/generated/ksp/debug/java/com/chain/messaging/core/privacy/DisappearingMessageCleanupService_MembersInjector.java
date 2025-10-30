package com.chain.messaging.core.privacy;

import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class DisappearingMessageCleanupService_MembersInjector implements MembersInjector<DisappearingMessageCleanupService> {
  private final Provider<DisappearingMessageManager> disappearingMessageManagerProvider;

  public DisappearingMessageCleanupService_MembersInjector(
      Provider<DisappearingMessageManager> disappearingMessageManagerProvider) {
    this.disappearingMessageManagerProvider = disappearingMessageManagerProvider;
  }

  public static MembersInjector<DisappearingMessageCleanupService> create(
      Provider<DisappearingMessageManager> disappearingMessageManagerProvider) {
    return new DisappearingMessageCleanupService_MembersInjector(disappearingMessageManagerProvider);
  }

  @Override
  public void injectMembers(DisappearingMessageCleanupService instance) {
    injectDisappearingMessageManager(instance, disappearingMessageManagerProvider.get());
  }

  @InjectedFieldSignature("com.chain.messaging.core.privacy.DisappearingMessageCleanupService.disappearingMessageManager")
  public static void injectDisappearingMessageManager(DisappearingMessageCleanupService instance,
      DisappearingMessageManager disappearingMessageManager) {
    instance.disappearingMessageManager = disappearingMessageManager;
  }
}
