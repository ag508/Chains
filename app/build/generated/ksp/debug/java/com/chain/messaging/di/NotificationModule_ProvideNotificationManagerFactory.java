package com.chain.messaging.di;

import com.chain.messaging.core.messaging.MessagingService;
import com.chain.messaging.core.notification.NotificationActionHandler;
import com.chain.messaging.core.notification.NotificationManager;
import com.chain.messaging.core.notification.NotificationService;
import com.chain.messaging.domain.repository.MessageRepository;
import com.chain.messaging.domain.repository.SettingsRepository;
import com.chain.messaging.domain.repository.UserRepository;
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
public final class NotificationModule_ProvideNotificationManagerFactory implements Factory<NotificationManager> {
  private final Provider<NotificationService> notificationServiceProvider;

  private final Provider<NotificationActionHandler> notificationActionHandlerProvider;

  private final Provider<MessagingService> messagingServiceProvider;

  private final Provider<MessageRepository> messageRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public NotificationModule_ProvideNotificationManagerFactory(
      Provider<NotificationService> notificationServiceProvider,
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
    return provideNotificationManager(notificationServiceProvider.get(), notificationActionHandlerProvider.get(), messagingServiceProvider.get(), messageRepositoryProvider.get(), userRepositoryProvider.get(), settingsRepositoryProvider.get());
  }

  public static NotificationModule_ProvideNotificationManagerFactory create(
      Provider<NotificationService> notificationServiceProvider,
      Provider<NotificationActionHandler> notificationActionHandlerProvider,
      Provider<MessagingService> messagingServiceProvider,
      Provider<MessageRepository> messageRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new NotificationModule_ProvideNotificationManagerFactory(notificationServiceProvider, notificationActionHandlerProvider, messagingServiceProvider, messageRepositoryProvider, userRepositoryProvider, settingsRepositoryProvider);
  }

  public static NotificationManager provideNotificationManager(
      NotificationService notificationService, NotificationActionHandler notificationActionHandler,
      MessagingService messagingService, MessageRepository messageRepository,
      UserRepository userRepository, SettingsRepository settingsRepository) {
    return Preconditions.checkNotNullFromProvides(NotificationModule.INSTANCE.provideNotificationManager(notificationService, notificationActionHandler, messagingService, messageRepository, userRepository, settingsRepository));
  }
}
