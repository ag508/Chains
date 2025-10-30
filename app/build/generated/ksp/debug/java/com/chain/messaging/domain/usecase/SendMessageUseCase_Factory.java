package com.chain.messaging.domain.usecase;

import com.chain.messaging.core.messaging.MessagingService;
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
public final class SendMessageUseCase_Factory implements Factory<SendMessageUseCase> {
  private final Provider<MessagingService> messagingServiceProvider;

  private final Provider<DisappearingMessageManager> disappearingMessageManagerProvider;

  public SendMessageUseCase_Factory(Provider<MessagingService> messagingServiceProvider,
      Provider<DisappearingMessageManager> disappearingMessageManagerProvider) {
    this.messagingServiceProvider = messagingServiceProvider;
    this.disappearingMessageManagerProvider = disappearingMessageManagerProvider;
  }

  @Override
  public SendMessageUseCase get() {
    return newInstance(messagingServiceProvider.get(), disappearingMessageManagerProvider.get());
  }

  public static SendMessageUseCase_Factory create(
      Provider<MessagingService> messagingServiceProvider,
      Provider<DisappearingMessageManager> disappearingMessageManagerProvider) {
    return new SendMessageUseCase_Factory(messagingServiceProvider, disappearingMessageManagerProvider);
  }

  public static SendMessageUseCase newInstance(MessagingService messagingService,
      DisappearingMessageManager disappearingMessageManager) {
    return new SendMessageUseCase(messagingService, disappearingMessageManager);
  }
}
