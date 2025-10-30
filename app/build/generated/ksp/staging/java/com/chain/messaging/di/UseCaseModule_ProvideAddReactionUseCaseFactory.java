package com.chain.messaging.di;

import com.chain.messaging.domain.repository.MessageRepository;
import com.chain.messaging.domain.usecase.AddReactionUseCase;
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
public final class UseCaseModule_ProvideAddReactionUseCaseFactory implements Factory<AddReactionUseCase> {
  private final Provider<MessageRepository> messageRepositoryProvider;

  public UseCaseModule_ProvideAddReactionUseCaseFactory(
      Provider<MessageRepository> messageRepositoryProvider) {
    this.messageRepositoryProvider = messageRepositoryProvider;
  }

  @Override
  public AddReactionUseCase get() {
    return provideAddReactionUseCase(messageRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideAddReactionUseCaseFactory create(
      Provider<MessageRepository> messageRepositoryProvider) {
    return new UseCaseModule_ProvideAddReactionUseCaseFactory(messageRepositoryProvider);
  }

  public static AddReactionUseCase provideAddReactionUseCase(MessageRepository messageRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideAddReactionUseCase(messageRepository));
  }
}
