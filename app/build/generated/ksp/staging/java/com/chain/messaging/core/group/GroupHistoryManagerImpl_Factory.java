package com.chain.messaging.core.group;

import com.chain.messaging.domain.repository.MessageRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class GroupHistoryManagerImpl_Factory implements Factory<GroupHistoryManagerImpl> {
  private final Provider<MessageRepository> messageRepositoryProvider;

  public GroupHistoryManagerImpl_Factory(Provider<MessageRepository> messageRepositoryProvider) {
    this.messageRepositoryProvider = messageRepositoryProvider;
  }

  @Override
  public GroupHistoryManagerImpl get() {
    return newInstance(messageRepositoryProvider.get());
  }

  public static GroupHistoryManagerImpl_Factory create(
      Provider<MessageRepository> messageRepositoryProvider) {
    return new GroupHistoryManagerImpl_Factory(messageRepositoryProvider);
  }

  public static GroupHistoryManagerImpl newInstance(MessageRepository messageRepository) {
    return new GroupHistoryManagerImpl(messageRepository);
  }
}
