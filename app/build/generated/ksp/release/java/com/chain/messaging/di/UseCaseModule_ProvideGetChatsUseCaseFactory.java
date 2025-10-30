package com.chain.messaging.di;

import com.chain.messaging.domain.repository.ChatRepository;
import com.chain.messaging.domain.usecase.GetChatsUseCase;
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
public final class UseCaseModule_ProvideGetChatsUseCaseFactory implements Factory<GetChatsUseCase> {
  private final Provider<ChatRepository> chatRepositoryProvider;

  public UseCaseModule_ProvideGetChatsUseCaseFactory(
      Provider<ChatRepository> chatRepositoryProvider) {
    this.chatRepositoryProvider = chatRepositoryProvider;
  }

  @Override
  public GetChatsUseCase get() {
    return provideGetChatsUseCase(chatRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideGetChatsUseCaseFactory create(
      Provider<ChatRepository> chatRepositoryProvider) {
    return new UseCaseModule_ProvideGetChatsUseCaseFactory(chatRepositoryProvider);
  }

  public static GetChatsUseCase provideGetChatsUseCase(ChatRepository chatRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideGetChatsUseCase(chatRepository));
  }
}
