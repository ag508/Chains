package com.chain.messaging.domain.usecase;

import com.chain.messaging.domain.repository.ChatRepository;
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
public final class ArchiveChatUseCase_Factory implements Factory<ArchiveChatUseCase> {
  private final Provider<ChatRepository> chatRepositoryProvider;

  public ArchiveChatUseCase_Factory(Provider<ChatRepository> chatRepositoryProvider) {
    this.chatRepositoryProvider = chatRepositoryProvider;
  }

  @Override
  public ArchiveChatUseCase get() {
    return newInstance(chatRepositoryProvider.get());
  }

  public static ArchiveChatUseCase_Factory create(Provider<ChatRepository> chatRepositoryProvider) {
    return new ArchiveChatUseCase_Factory(chatRepositoryProvider);
  }

  public static ArchiveChatUseCase newInstance(ChatRepository chatRepository) {
    return new ArchiveChatUseCase(chatRepository);
  }
}
