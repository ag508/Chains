package com.chain.messaging.di;

import com.chain.messaging.domain.repository.ChatRepository;
import com.chain.messaging.domain.usecase.PinChatUseCase;
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
public final class UseCaseModule_ProvidePinChatUseCaseFactory implements Factory<PinChatUseCase> {
  private final Provider<ChatRepository> chatRepositoryProvider;

  public UseCaseModule_ProvidePinChatUseCaseFactory(
      Provider<ChatRepository> chatRepositoryProvider) {
    this.chatRepositoryProvider = chatRepositoryProvider;
  }

  @Override
  public PinChatUseCase get() {
    return providePinChatUseCase(chatRepositoryProvider.get());
  }

  public static UseCaseModule_ProvidePinChatUseCaseFactory create(
      Provider<ChatRepository> chatRepositoryProvider) {
    return new UseCaseModule_ProvidePinChatUseCaseFactory(chatRepositoryProvider);
  }

  public static PinChatUseCase providePinChatUseCase(ChatRepository chatRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.providePinChatUseCase(chatRepository));
  }
}
