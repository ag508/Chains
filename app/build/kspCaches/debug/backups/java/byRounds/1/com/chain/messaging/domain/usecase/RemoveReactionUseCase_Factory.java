package com.chain.messaging.domain.usecase;

import com.chain.messaging.domain.repository.MessageRepository;
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
public final class RemoveReactionUseCase_Factory implements Factory<RemoveReactionUseCase> {
  private final Provider<MessageRepository> messageRepositoryProvider;

  public RemoveReactionUseCase_Factory(Provider<MessageRepository> messageRepositoryProvider) {
    this.messageRepositoryProvider = messageRepositoryProvider;
  }

  @Override
  public RemoveReactionUseCase get() {
    return newInstance(messageRepositoryProvider.get());
  }

  public static RemoveReactionUseCase_Factory create(
      Provider<MessageRepository> messageRepositoryProvider) {
    return new RemoveReactionUseCase_Factory(messageRepositoryProvider);
  }

  public static RemoveReactionUseCase newInstance(MessageRepository messageRepository) {
    return new RemoveReactionUseCase(messageRepository);
  }
}
