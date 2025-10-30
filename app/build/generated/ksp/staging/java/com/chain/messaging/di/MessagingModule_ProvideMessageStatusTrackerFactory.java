package com.chain.messaging.di;

import com.chain.messaging.core.messaging.MessageStatusTracker;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class MessagingModule_ProvideMessageStatusTrackerFactory implements Factory<MessageStatusTracker> {
  @Override
  public MessageStatusTracker get() {
    return provideMessageStatusTracker();
  }

  public static MessagingModule_ProvideMessageStatusTrackerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MessageStatusTracker provideMessageStatusTracker() {
    return Preconditions.checkNotNullFromProvides(MessagingModule.INSTANCE.provideMessageStatusTracker());
  }

  private static final class InstanceHolder {
    private static final MessagingModule_ProvideMessageStatusTrackerFactory INSTANCE = new MessagingModule_ProvideMessageStatusTrackerFactory();
  }
}
