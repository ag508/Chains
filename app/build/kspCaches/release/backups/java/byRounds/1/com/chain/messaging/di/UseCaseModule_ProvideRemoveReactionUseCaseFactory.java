package com.chain.messaging.di;

import com.chain.messaging.domain.repository.MessageRepository;
import com.chain.messaging.domain.usecase.RemoveReactionUseCase;
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
public final class UseCaseModule_ProvideRemoveReactionUseCaseFactory implements Factory<RemoveReactionUseCase> {
  private final Provider<MessageRepository> messageRepositoryProvider;

  public UseCaseModule_ProvideRemoveReactionUseCaseFactory(
      Provider<MessageRepository> messageRepositoryProvider) {
    this.messageRepositoryProvider = messageRepositoryProvider;
  }

  @Override
  public RemoveReactionUseCase get() {
    return provideRemoveReactionUseCase(messageRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideRemoveReactionUseCaseFactory create(
      Provider<MessageRepository> messageRepositoryProvider) {
    return new UseCaseModule_ProvideRemoveReactionUseCaseFactory(messageRepositoryProvider);
  }

  public static RemoveReactionUseCase provideRemoveReactionUseCase(
      MessageRepository messageRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideRemoveReactionUseCase(messageRepository));
  }
}
