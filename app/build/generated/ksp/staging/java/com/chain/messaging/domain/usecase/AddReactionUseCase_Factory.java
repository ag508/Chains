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
public final class AddReactionUseCase_Factory implements Factory<AddReactionUseCase> {
  private final Provider<MessageRepository> messageRepositoryProvider;

  public AddReactionUseCase_Factory(Provider<MessageRepository> messageRepositoryProvider) {
    this.messageRepositoryProvider = messageRepositoryProvider;
  }

  @Override
  public AddReactionUseCase get() {
    return newInstance(messageRepositoryProvider.get());
  }

  public static AddReactionUseCase_Factory create(
      Provider<MessageRepository> messageRepositoryProvider) {
    return new AddReactionUseCase_Factory(messageRepositoryProvider);
  }

  public static AddReactionUseCase newInstance(MessageRepository messageRepository) {
    return new AddReactionUseCase(messageRepository);
  }
}
