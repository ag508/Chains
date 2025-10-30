package com.chain.messaging.di;

import com.chain.messaging.core.messaging.TypingIndicatorService;
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
public final class MessagingModule_ProvideTypingIndicatorServiceFactory implements Factory<TypingIndicatorService> {
  @Override
  public TypingIndicatorService get() {
    return provideTypingIndicatorService();
  }

  public static MessagingModule_ProvideTypingIndicatorServiceFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TypingIndicatorService provideTypingIndicatorService() {
    return Preconditions.checkNotNullFromProvides(MessagingModule.INSTANCE.provideTypingIndicatorService());
  }

  private static final class InstanceHolder {
    private static final MessagingModule_ProvideTypingIndicatorServiceFactory INSTANCE = new MessagingModule_ProvideTypingIndicatorServiceFactory();
  }
}
