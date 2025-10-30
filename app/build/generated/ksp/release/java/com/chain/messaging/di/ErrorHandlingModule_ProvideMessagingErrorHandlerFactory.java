package com.chain.messaging.di;

import com.chain.messaging.core.error.ErrorHandler;
import com.chain.messaging.core.error.ErrorRecoveryManager;
import com.chain.messaging.core.error.MessagingErrorHandler;
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
public final class ErrorHandlingModule_ProvideMessagingErrorHandlerFactory implements Factory<MessagingErrorHandler> {
  private final Provider<ErrorHandler> errorHandlerProvider;

  private final Provider<ErrorRecoveryManager> errorRecoveryManagerProvider;

  public ErrorHandlingModule_ProvideMessagingErrorHandlerFactory(
      Provider<ErrorHandler> errorHandlerProvider,
      Provider<ErrorRecoveryManager> errorRecoveryManagerProvider) {
    this.errorHandlerProvider = errorHandlerProvider;
    this.errorRecoveryManagerProvider = errorRecoveryManagerProvider;
  }

  @Override
  public MessagingErrorHandler get() {
    return provideMessagingErrorHandler(errorHandlerProvider.get(), errorRecoveryManagerProvider.get());
  }

  public static ErrorHandlingModule_ProvideMessagingErrorHandlerFactory create(
      Provider<ErrorHandler> errorHandlerProvider,
      Provider<ErrorRecoveryManager> errorRecoveryManagerProvider) {
    return new ErrorHandlingModule_ProvideMessagingErrorHandlerFactory(errorHandlerProvider, errorRecoveryManagerProvider);
  }

  public static MessagingErrorHandler provideMessagingErrorHandler(ErrorHandler errorHandler,
      ErrorRecoveryManager errorRecoveryManager) {
    return Preconditions.checkNotNullFromProvides(ErrorHandlingModule.INSTANCE.provideMessagingErrorHandler(errorHandler, errorRecoveryManager));
  }
}
