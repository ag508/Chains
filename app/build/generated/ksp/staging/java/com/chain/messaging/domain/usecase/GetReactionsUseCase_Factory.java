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
public final class GetReactionsUseCase_Factory implements Factory<GetReactionsUseCase> {
  private final Provider<MessageRepository> messageRepositoryProvider;

  public GetReactionsUseCase_Factory(Provider<MessageRepository> messageRepositoryProvider) {
    this.messageRepositoryProvider = messageRepositoryProvider;
  }

  @Override
  public GetReactionsUseCase get() {
    return newInstance(messageRepositoryProvider.get());
  }

  public static GetReactionsUseCase_Factory create(
      Provider<MessageRepository> messageRepositoryProvider) {
    return new GetReactionsUseCase_Factory(messageRepositoryProvider);
  }

  public static GetReactionsUseCase newInstance(MessageRepository messageRepository) {
    return new GetReactionsUseCase(messageRepository);
  }
}
