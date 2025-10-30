package com.chain.messaging.domain.usecase;

import com.chain.messaging.domain.repository.ChatRepository;
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
public final class DeleteChatUseCase_Factory implements Factory<DeleteChatUseCase> {
  private final Provider<ChatRepository> chatRepositoryProvider;

  private final Provider<MessageRepository> messageRepositoryProvider;

  public DeleteChatUseCase_Factory(Provider<ChatRepository> chatRepositoryProvider,
      Provider<MessageRepository> messageRepositoryProvider) {
    this.chatRepositoryProvider = chatRepositoryProvider;
    this.messageRepositoryProvider = messageRepositoryProvider;
  }

  @Override
  public DeleteChatUseCase get() {
    return newInstance(chatRepositoryProvider.get(), messageRepositoryProvider.get());
  }

  public static DeleteChatUseCase_Factory create(Provider<ChatRepository> chatRepositoryProvider,
      Provider<MessageRepository> messageRepositoryProvider) {
    return new DeleteChatUseCase_Factory(chatRepositoryProvider, messageRepositoryProvider);
  }

  public static DeleteChatUseCase newInstance(ChatRepository chatRepository,
      MessageRepository messageRepository) {
    return new DeleteChatUseCase(chatRepository, messageRepository);
  }
}
