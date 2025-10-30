package com.chain.messaging.di;

import com.chain.messaging.domain.repository.MessageRepository;
import com.chain.messaging.domain.usecase.GetReactionsUseCase;
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
public final class UseCaseModule_ProvideGetReactionsUseCaseFactory implements Factory<GetReactionsUseCase> {
  private final Provider<MessageRepository> messageRepositoryProvider;

  public UseCaseModule_ProvideGetReactionsUseCaseFactory(
      Provider<MessageRepository> messageRepositoryProvider) {
    this.messageRepositoryProvider = messageRepositoryProvider;
  }

  @Override
  public GetReactionsUseCase get() {
    return provideGetReactionsUseCase(messageRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideGetReactionsUseCaseFactory create(
      Provider<MessageRepository> messageRepositoryProvider) {
    return new UseCaseModule_ProvideGetReactionsUseCaseFactory(messageRepositoryProvider);
  }

  public static GetReactionsUseCase provideGetReactionsUseCase(
      MessageRepository messageRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideGetReactionsUseCase(messageRepository));
  }
}
