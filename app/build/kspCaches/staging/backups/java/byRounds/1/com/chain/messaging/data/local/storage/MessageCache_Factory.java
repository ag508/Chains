package com.chain.messaging.data.local.storage;

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
public final class MessageCache_Factory implements Factory<MessageCache> {
  @Override
  public MessageCache get() {
    return newInstance();
  }

  public static MessageCache_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MessageCache newInstance() {
    return new MessageCache();
  }

  private static final class InstanceHolder {
    private static final MessageCache_Factory INSTANCE = new MessageCache_Factory();
  }
}
