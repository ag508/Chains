package com.chain.messaging.data.local.storage;

import com.chain.messaging.core.security.MessageEncryption;
import com.chain.messaging.data.local.dao.MediaDao;
import com.chain.messaging.data.local.dao.MessageDao;
import com.chain.messaging.data.local.dao.MessageSearchDao;
import com.chain.messaging.data.local.dao.ReactionDao;
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
public final class MessageStorageService_Factory implements Factory<MessageStorageService> {
  private final Provider<MessageDao> messageDaoProvider;

  private final Provider<MessageSearchDao> messageSearchDaoProvider;

  private final Provider<ReactionDao> reactionDaoProvider;

  private final Provider<MediaDao> mediaDaoProvider;

  private final Provider<MessageEncryption> messageEncryptionProvider;

  private final Provider<MessageCache> messageCacheProvider;

  public MessageStorageService_Factory(Provider<MessageDao> messageDaoProvider,
      Provider<MessageSearchDao> messageSearchDaoProvider,
      Provider<ReactionDao> reactionDaoProvider, Provider<MediaDao> mediaDaoProvider,
      Provider<MessageEncryption> messageEncryptionProvider,
      Provider<MessageCache> messageCacheProvider) {
    this.messageDaoProvider = messageDaoProvider;
    this.messageSearchDaoProvider = messageSearchDaoProvider;
    this.reactionDaoProvider = reactionDaoProvider;
    this.mediaDaoProvider = mediaDaoProvider;
    this.messageEncryptionProvider = messageEncryptionProvider;
    this.messageCacheProvider = messageCacheProvider;
  }

  @Override
  public MessageStorageService get() {
    return newInstance(messageDaoProvider.get(), messageSearchDaoProvider.get(), reactionDaoProvider.get(), mediaDaoProvider.get(), messageEncryptionProvider.get(), messageCacheProvider.get());
  }

  public static MessageStorageService_Factory create(Provider<MessageDao> messageDaoProvider,
      Provider<MessageSearchDao> messageSearchDaoProvider,
      Provider<ReactionDao> reactionDaoProvider, Provider<MediaDao> mediaDaoProvider,
      Provider<MessageEncryption> messageEncryptionProvider,
      Provider<MessageCache> messageCacheProvider) {
    return new MessageStorageService_Factory(messageDaoProvider, messageSearchDaoProvider, reactionDaoProvider, mediaDaoProvider, messageEncryptionProvider, messageCacheProvider);
  }

  public static MessageStorageService newInstance(MessageDao messageDao,
      MessageSearchDao messageSearchDao, ReactionDao reactionDao, MediaDao mediaDao,
      MessageEncryption messageEncryption, MessageCache messageCache) {
    return new MessageStorageService(messageDao, messageSearchDao, reactionDao, mediaDao, messageEncryption, messageCache);
  }
}
