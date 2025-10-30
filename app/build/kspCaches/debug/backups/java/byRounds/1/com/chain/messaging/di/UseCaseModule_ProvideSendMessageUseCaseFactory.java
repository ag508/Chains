package com.chain.messaging.di;

import com.chain.messaging.core.messaging.MessagingService;
import com.chain.messaging.core.privacy.DisappearingMessageManager;
import com.chain.messaging.domain.usecase.SendMessageUseCase;
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
public final class UseCaseModule_ProvideSendMessageUseCaseFactory implements Factory<SendMessageUseCase> {
  private final Provider<MessagingService> messagingServiceProvider;

  private final Provider<DisappearingMessageManager> disappearingMessageManagerProvider;

  public UseCaseModule_ProvideSendMessageUseCaseFactory(
      Provider<MessagingService> messagingServiceProvider,
      Provider<DisappearingMessageManager> disappearingMessageManagerProvider) {
    this.messagingServiceProvider = messagingServiceProvider;
    this.disappearingMessageManagerProvider = disappearingMessageManagerProvider;
  }

  @Override
  public SendMessageUseCase get() {
    return provideSendMessageUseCase(messagingServiceProvider.get(), disappearingMessageManagerProvider.get());
  }

  public static UseCaseModule_ProvideSendMessageUseCaseFactory create(
      Provider<MessagingService> messagingServiceProvider,
      Provider<DisappearingMessageManager> disappearingMessageManagerProvider) {
    return new UseCaseModule_ProvideSendMessageUseCaseFactory(messagingServiceProvider, disappearingMessageManagerProvider);
  }

  public static SendMessageUseCase provideSendMessageUseCase(MessagingService messagingService,
      DisappearingMessageManager disappearingMessageManager) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideSendMessageUseCase(messagingService, disappearingMessageManager));
  }
}
