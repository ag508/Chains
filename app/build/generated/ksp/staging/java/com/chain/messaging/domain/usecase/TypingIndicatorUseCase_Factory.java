package com.chain.messaging.domain.usecase;

import com.chain.messaging.core.messaging.TypingIndicatorService;
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
public final class TypingIndicatorUseCase_Factory implements Factory<TypingIndicatorUseCase> {
  private final Provider<TypingIndicatorService> typingIndicatorServiceProvider;

  public TypingIndicatorUseCase_Factory(
      Provider<TypingIndicatorService> typingIndicatorServiceProvider) {
    this.typingIndicatorServiceProvider = typingIndicatorServiceProvider;
  }

  @Override
  public TypingIndicatorUseCase get() {
    return newInstance(typingIndicatorServiceProvider.get());
  }

  public static TypingIndicatorUseCase_Factory create(
      Provider<TypingIndicatorService> typingIndicatorServiceProvider) {
    return new TypingIndicatorUseCase_Factory(typingIndicatorServiceProvider);
  }

  public static TypingIndicatorUseCase newInstance(TypingIndicatorService typingIndicatorService) {
    return new TypingIndicatorUseCase(typingIndicatorService);
  }
}
