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
public final class TypingIndicatorService_Factory implements Factory<TypingIndicatorService> {
  @Override
  public TypingIndicatorService get() {
    return newInstance();
  }

  public static TypingIndicatorService_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TypingIndicatorService newInstance() {
    return new TypingIndicatorService();
  }

  private static final class InstanceHolder {
    private static final TypingIndicatorService_Factory INSTANCE = new TypingIndicatorService_Factory();
  }
}
