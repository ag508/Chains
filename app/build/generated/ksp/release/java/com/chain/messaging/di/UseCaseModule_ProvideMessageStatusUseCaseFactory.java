package com.chain.messaging.di;

import com.chain.messaging.core.messaging.MessageStatusTracker;
import com.chain.messaging.domain.usecase.MessageStatusUseCase;
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
public final class UseCaseModule_ProvideMessageStatusUseCaseFactory implements Factory<MessageStatusUseCase> {
  private final Provider<MessageStatusTracker> messageStatusTrackerProvider;

  public UseCaseModule_ProvideMessageStatusUseCaseFactory(
      Provider<MessageStatusTracker> messageStatusTrackerProvider) {
    this.messageStatusTrackerProvider = messageStatusTrackerProvider;
  }

  @Override
  public MessageStatusUseCase get() {
    return provideMessageStatusUseCase(messageStatusTrackerProvider.get());
  }

  public static UseCaseModule_ProvideMessageStatusUseCaseFactory create(
      Provider<MessageStatusTracker> messageStatusTrackerProvider) {
    return new UseCaseModule_ProvideMessageStatusUseCaseFactory(messageStatusTrackerProvider);
  }

  public static MessageStatusUseCase provideMessageStatusUseCase(
      MessageStatusTracker messageStatusTracker) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideMessageStatusUseCase(messageStatusTracker));
  }
}
