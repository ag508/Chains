package com.chain.messaging.core.error;

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
public final class StorageErrorHandler_Factory implements Factory<StorageErrorHandler> {
  private final Provider<ErrorHandler> errorHandlerProvider;

  private final Provider<ErrorRecoveryManager> errorRecoveryManagerProvider;

  public StorageErrorHandler_Factory(Provider<ErrorHandler> errorHandlerProvider,
      Provider<ErrorRecoveryManager> errorRecoveryManagerProvider) {
    this.errorHandlerProvider = errorHandlerProvider;
    this.errorRecoveryManagerProvider = errorRecoveryManagerProvider;
  }

  @Override
  public StorageErrorHandler get() {
    return newInstance(errorHandlerProvider.get(), errorRecoveryManagerProvider.get());
  }

  public static StorageErrorHandler_Factory create(Provider<ErrorHandler> errorHandlerProvider,
      Provider<ErrorRecoveryManager> errorRecoveryManagerProvider) {
    return new StorageErrorHandler_Factory(errorHandlerProvider, errorRecoveryManagerProvider);
  }

  public static StorageErrorHandler newInstance(ErrorHandler errorHandler,
      ErrorRecoveryManager errorRecoveryManager) {
    return new StorageErrorHandler(errorHandler, errorRecoveryManager);
  }
}
