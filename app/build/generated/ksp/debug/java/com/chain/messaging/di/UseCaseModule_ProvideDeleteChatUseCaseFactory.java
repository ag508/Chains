package com.chain.messaging.di;

import com.chain.messaging.domain.repository.ChatRepository;
import com.chain.messaging.domain.repository.MessageRepository;
import com.chain.messaging.domain.usecase.DeleteChatUseCase;
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
public final class UseCaseModule_ProvideDeleteChatUseCaseFactory implements Factory<DeleteChatUseCase> {
  private final Provider<ChatRepository> chatRepositoryProvider;

  private final Provider<MessageRepository> messageRepositoryProvider;

  public UseCaseModule_ProvideDeleteChatUseCaseFactory(
      Provider<ChatRepository> chatRepositoryProvider,
      Provider<MessageRepository> messageRepositoryProvider) {
    this.chatRepositoryProvider = chatRepositoryProvider;
    this.messageRepositoryProvider = messageRepositoryProvider;
  }

  @Override
  public DeleteChatUseCase get() {
    return provideDeleteChatUseCase(chatRepositoryProvider.get(), messageRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideDeleteChatUseCaseFactory create(
      Provider<ChatRepository> chatRepositoryProvider,
      Provider<MessageRepository> messageRepositoryProvider) {
    return new UseCaseModule_ProvideDeleteChatUseCaseFactory(chatRepositoryProvider, messageRepositoryProvider);
  }

  public static DeleteChatUseCase provideDeleteChatUseCase(ChatRepository chatRepository,
      MessageRepository messageRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideDeleteChatUseCase(chatRepository, messageRepository));
  }
}
