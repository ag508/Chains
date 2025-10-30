package com.chain.messaging.core.messaging;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class MessageStatusTracker_Factory implements Factory<MessageStatusTracker> {
  @Override
  public MessageStatusTracker get() {
    return newInstance();
  }

  public static MessageStatusTracker_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MessageStatusTracker newInstance() {
    return new MessageStatusTracker();
  }

  private static final class InstanceHolder {
    private static final MessageStatusTracker_Factory INSTANCE = new MessageStatusTracker_Factory();
  }
}
