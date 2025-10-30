package com.chain.messaging.di;

import com.chain.messaging.domain.repository.MessageRepository;
import com.chain.messaging.domain.usecase.GetMessagesUseCase;
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
public final class UseCaseModule_ProvideGetMessagesUseCaseFactory implements Factory<GetMessagesUseCase> {
  private final Provider<MessageRepository> messageRepositoryProvider;

  public UseCaseModule_ProvideGetMessagesUseCaseFactory(
      Provider<MessageRepository> messageRepositoryProvider) {
    this.messageRepositoryProvider = messageRepositoryProvider;
  }

  @Override
  public GetMessagesUseCase get() {
    return provideGetMessagesUseCase(messageRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideGetMessagesUseCaseFactory create(
      Provider<MessageRepository> messageRepositoryProvider) {
    return new UseCaseModule_ProvideGetMessagesUseCaseFactory(messageRepositoryProvider);
  }

  public static GetMessagesUseCase provideGetMessagesUseCase(MessageRepository messageRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideGetMessagesUseCase(messageRepository));
  }
}
