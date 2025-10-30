package com.chain.messaging.domain.usecase;

import com.chain.messaging.core.messaging.MessageStatusTracker;
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
public final class MessageStatusUseCase_Factory implements Factory<MessageStatusUseCase> {
  private final Provider<MessageStatusTracker> messageStatusTrackerProvider;

  public MessageStatusUseCase_Factory(Provider<MessageStatusTracker> messageStatusTrackerProvider) {
    this.messageStatusTrackerProvider = messageStatusTrackerProvider;
  }

  @Override
  public MessageStatusUseCase get() {
    return newInstance(messageStatusTrackerProvider.get());
  }

  public static MessageStatusUseCase_Factory create(
      Provider<MessageStatusTracker> messageStatusTrackerProvider) {
    return new MessageStatusUseCase_Factory(messageStatusTrackerProvider);
  }

  public static MessageStatusUseCase newInstance(MessageStatusTracker messageStatusTracker) {
    return new MessageStatusUseCase(messageStatusTracker);
  }
}
