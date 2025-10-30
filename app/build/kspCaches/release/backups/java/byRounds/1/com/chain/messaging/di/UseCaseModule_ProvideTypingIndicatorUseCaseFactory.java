package com.chain.messaging.di;

import com.chain.messaging.core.messaging.TypingIndicatorService;
import com.chain.messaging.domain.usecase.TypingIndicatorUseCase;
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
public final class UseCaseModule_ProvideTypingIndicatorUseCaseFactory implements Factory<TypingIndicatorUseCase> {
  private final Provider<TypingIndicatorService> typingIndicatorServiceProvider;

  public UseCaseModule_ProvideTypingIndicatorUseCaseFactory(
      Provider<TypingIndicatorService> typingIndicatorServiceProvider) {
    this.typingIndicatorServiceProvider = typingIndicatorServiceProvider;
  }

  @Override
  public TypingIndicatorUseCase get() {
    return provideTypingIndicatorUseCase(typingIndicatorServiceProvider.get());
  }

  public static UseCaseModule_ProvideTypingIndicatorUseCaseFactory create(
      Provider<TypingIndicatorService> typingIndicatorServiceProvider) {
    return new UseCaseModule_ProvideTypingIndicatorUseCaseFactory(typingIndicatorServiceProvider);
  }

  public static TypingIndicatorUseCase provideTypingIndicatorUseCase(
      TypingIndicatorService typingIndicatorService) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideTypingIndicatorUseCase(typingIndicatorService));
  }
}
