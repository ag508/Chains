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
public final class GetMessagesUseCase_Factory implements Factory<GetMessagesUseCase> {
  private final Provider<MessageRepository> messageRepositoryProvider;

  public GetMessagesUseCase_Factory(Provider<MessageRepository> messageRepositoryProvider) {
    this.messageRepositoryProvider = messageRepositoryProvider;
  }

  @Override
  public GetMessagesUseCase get() {
    return newInstance(messageRepositoryProvider.get());
  }

  public static GetMessagesUseCase_Factory create(
      Provider<MessageRepository> messageRepositoryProvider) {
    return new GetMessagesUseCase_Factory(messageRepositoryProvider);
  }

  public static GetMessagesUseCase newInstance(MessageRepository messageRepository) {
    return new GetMessagesUseCase(messageRepository);
  }
}
