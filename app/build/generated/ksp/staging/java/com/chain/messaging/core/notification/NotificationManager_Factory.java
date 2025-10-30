package com.chain.messaging.core.notification;

import com.chain.messaging.core.messaging.MessagingService;
import com.chain.messaging.domain.repository.MessageRepository;
import com.chain.messaging.domain.repository.SettingsRepository;
import com.chain.messaging.domain.repository.UserRepository;
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
public final class NotificationManager_Factory implements Factory<NotificationManager> {
  private final Provider<NotificationService> notificationServiceProvider;

  private final Provider<NotificationActionHandler> notificationActionHandlerProvider;

  private final Provider<MessagingService> messagingServiceProvider;

  private final Provider<MessageRepository> messageRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public NotificationManager_Factory(Provider<NotificationService> notificationServiceProvider,
      Provider<NotificationActionHandler> notificationActionHandlerProvider,
      Provider<MessagingService> messagingServiceProvider,
      Provider<MessageRepository> messageRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.notificationServiceProvider = notificationServiceProvider;
    this.notificationActionHandlerProvider = notificationActionHandlerProvider;
    this.messagingServiceProvider = messagingServiceProvider;
    this.messageRepositoryProvider = messageRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public NotificationManager get() {
    return newInstance(notificationServiceProvider.get(), notificationActionHandlerProvider.get(), messagingServiceProvider.get(), messageRepositoryProvider.get(), userRepositoryProvider.get(), settingsRepositoryProvider.get());
  }

  public static NotificationManager_Factory create(
      Provider<NotificationService> notificationServiceProvider,
      Provider<NotificationActionHandler> notificationActionHandlerProvider,
      Provider<MessagingService> messagingServiceProvider,
      Provider<MessageRepository> messageRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new NotificationManager_Factory(notificationServiceProvider, notificationActionHandlerProvider, messagingServiceProvider, messageRepositoryProvider, userRepositoryProvider, settingsRepositoryProvider);
  }

  public static NotificationManager newInstance(NotificationService notificationService,
      NotificationActionHandler notificationActionHandler, MessagingService messagingService,
      MessageRepository messageRepository, UserRepository userRepository,
      SettingsRepository settingsRepository) {
    return new NotificationManager(notificationService, notificationActionHandler, messagingService, messageRepository, userRepository, settingsRepository);
  }
}
