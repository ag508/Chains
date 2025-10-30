package com.chain.messaging.data.local.storage;

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
public final class MessagePagination_Factory implements Factory<MessagePagination> {
  private final Provider<MessageStorageService> messageStorageServiceProvider;

  public MessagePagination_Factory(Provider<MessageStorageService> messageStorageServiceProvider) {
    this.messageStorageServiceProvider = messageStorageServiceProvider;
  }

  @Override
  public MessagePagination get() {
    return newInstance(messageStorageServiceProvider.get());
  }

  public static MessagePagination_Factory create(
      Provider<MessageStorageService> messageStorageServiceProvider) {
    return new MessagePagination_Factory(messageStorageServiceProvider);
  }

  public static MessagePagination newInstance(MessageStorageService messageStorageService) {
    return new MessagePagination(messageStorageService);
  }
}
