package com.chain.messaging.di;

import com.chain.messaging.core.error.ErrorHandler;
import com.chain.messaging.core.error.ErrorRecoveryManager;
import com.chain.messaging.core.error.StorageErrorHandler;
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
public final class ErrorHandlingModule_ProvideStorageErrorHandlerFactory implements Factory<StorageErrorHandler> {
  private final Provider<ErrorHandler> errorHandlerProvider;

  private final Provider<ErrorRecoveryManager> errorRecoveryManagerProvider;

  public ErrorHandlingModule_ProvideStorageErrorHandlerFactory(
      Provider<ErrorHandler> errorHandlerProvider,
      Provider<ErrorRecoveryManager> errorRecoveryManagerProvider) {
    this.errorHandlerProvider = errorHandlerProvider;
    this.errorRecoveryManagerProvider = errorRecoveryManagerProvider;
  }

  @Override
  public StorageErrorHandler get() {
    return provideStorageErrorHandler(errorHandlerProvider.get(), errorRecoveryManagerProvider.get());
  }

  public static ErrorHandlingModule_ProvideStorageErrorHandlerFactory create(
      Provider<ErrorHandler> errorHandlerProvider,
      Provider<ErrorRecoveryManager> errorRecoveryManagerProvider) {
    return new ErrorHandlingModule_ProvideStorageErrorHandlerFactory(errorHandlerProvider, errorRecoveryManagerProvider);
  }

  public static StorageErrorHandler provideStorageErrorHandler(ErrorHandler errorHandler,
      ErrorRecoveryManager errorRecoveryManager) {
    return Preconditions.checkNotNullFromProvides(ErrorHandlingModule.INSTANCE.provideStorageErrorHandler(errorHandler, errorRecoveryManager));
  }
}
