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
public final class PinChatUseCase_Factory implements Factory<PinChatUseCase> {
  private final Provider<ChatRepository> chatRepositoryProvider;

  public PinChatUseCase_Factory(Provider<ChatRepository> chatRepositoryProvider) {
    this.chatRepositoryProvider = chatRepositoryProvider;
  }

  @Override
  public PinChatUseCase get() {
    return newInstance(chatRepositoryProvider.get());
  }

  public static PinChatUseCase_Factory create(Provider<ChatRepository> chatRepositoryProvider) {
    return new PinChatUseCase_Factory(chatRepositoryProvider);
  }

  public static PinChatUseCase newInstance(ChatRepository chatRepository) {
    return new PinChatUseCase(chatRepository);
  }
}
