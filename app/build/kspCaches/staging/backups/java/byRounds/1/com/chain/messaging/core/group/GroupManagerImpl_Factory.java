package com.chain.messaging.core.group;

import com.chain.messaging.domain.repository.ChatRepository;
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
public final class GroupManagerImpl_Factory implements Factory<GroupManagerImpl> {
  private final Provider<ChatRepository> chatRepositoryProvider;

  private final Provider<MessageRepository> messageRepositoryProvider;

  private final Provider<InviteLinkGenerator> inviteLinkGeneratorProvider;

  public GroupManagerImpl_Factory(Provider<ChatRepository> chatRepositoryProvider,
      Provider<MessageRepository> messageRepositoryProvider,
      Provider<InviteLinkGenerator> inviteLinkGeneratorProvider) {
    this.chatRepositoryProvider = chatRepositoryProvider;
    this.messageRepositoryProvider = messageRepositoryProvider;
    this.inviteLinkGeneratorProvider = inviteLinkGeneratorProvider;
  }

  @Override
  public GroupManagerImpl get() {
    return newInstance(chatRepositoryProvider.get(), messageRepositoryProvider.get(), inviteLinkGeneratorProvider.get());
  }

  public static GroupManagerImpl_Factory create(Provider<ChatRepository> chatRepositoryProvider,
      Provider<MessageRepository> messageRepositoryProvider,
      Provider<InviteLinkGenerator> inviteLinkGeneratorProvider) {
    return new GroupManagerImpl_Factory(chatRepositoryProvider, messageRepositoryProvider, inviteLinkGeneratorProvider);
  }

  public static GroupManagerImpl newInstance(ChatRepository chatRepository,
      MessageRepository messageRepository, InviteLinkGenerator inviteLinkGenerator) {
    return new GroupManagerImpl(chatRepository, messageRepository, inviteLinkGenerator);
  }
}
