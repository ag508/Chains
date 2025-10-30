package com.chain.messaging.di;

import com.chain.messaging.domain.repository.ChatRepository;
import com.chain.messaging.domain.usecase.ArchiveChatUseCase;
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
public final class UseCaseModule_ProvideArchiveChatUseCaseFactory implements Factory<ArchiveChatUseCase> {
  private final Provider<ChatRepository> chatRepositoryProvider;

  public UseCaseModule_ProvideArchiveChatUseCaseFactory(
      Provider<ChatRepository> chatRepositoryProvider) {
    this.chatRepositoryProvider = chatRepositoryProvider;
  }

  @Override
  public ArchiveChatUseCase get() {
    return provideArchiveChatUseCase(chatRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideArchiveChatUseCaseFactory create(
      Provider<ChatRepository> chatRepositoryProvider) {
    return new UseCaseModule_ProvideArchiveChatUseCaseFactory(chatRepositoryProvider);
  }

  public static ArchiveChatUseCase provideArchiveChatUseCase(ChatRepository chatRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideArchiveChatUseCase(chatRepository));
  }
}
