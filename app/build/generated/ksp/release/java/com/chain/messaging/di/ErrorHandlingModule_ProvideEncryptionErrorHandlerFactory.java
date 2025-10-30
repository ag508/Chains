package com.chain.messaging.di;

import com.chain.messaging.core.error.EncryptionErrorHandler;
import com.chain.messaging.core.error.ErrorHandler;
import com.chain.messaging.core.error.ErrorRecoveryManager;
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
public final class ErrorHandlingModule_ProvideEncryptionErrorHandlerFactory implements Factory<EncryptionErrorHandler> {
  private final Provider<ErrorHandler> errorHandlerProvider;

  private final Provider<ErrorRecoveryManager> errorRecoveryManagerProvider;

  public ErrorHandlingModule_ProvideEncryptionErrorHandlerFactory(
      Provider<ErrorHandler> errorHandlerProvider,
      Provider<ErrorRecoveryManager> errorRecoveryManagerProvider) {
    this.errorHandlerProvider = errorHandlerProvider;
    this.errorRecoveryManagerProvider = errorRecoveryManagerProvider;
  }

  @Override
  public EncryptionErrorHandler get() {
    return provideEncryptionErrorHandler(errorHandlerProvider.get(), errorRecoveryManagerProvider.get());
  }

  public static ErrorHandlingModule_ProvideEncryptionErrorHandlerFactory create(
      Provider<ErrorHandler> errorHandlerProvider,
      Provider<ErrorRecoveryManager> errorRecoveryManagerProvider) {
    return new ErrorHandlingModule_ProvideEncryptionErrorHandlerFactory(errorHandlerProvider, errorRecoveryManagerProvider);
  }

  public static EncryptionErrorHandler provideEncryptionErrorHandler(ErrorHandler errorHandler,
      ErrorRecoveryManager errorRecoveryManager) {
    return Preconditions.checkNotNullFromProvides(ErrorHandlingModule.INSTANCE.provideEncryptionErrorHandler(errorHandler, errorRecoveryManager));
  }
}
