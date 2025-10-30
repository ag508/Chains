package com.chain.messaging.di;

import com.chain.messaging.core.error.ErrorHandler;
import com.chain.messaging.core.error.ErrorRecoveryManager;
import com.chain.messaging.core.error.P2PErrorHandler;
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
public final class ErrorHandlingModule_ProvideP2PErrorHandlerFactory implements Factory<P2PErrorHandler> {
  private final Provider<ErrorHandler> errorHandlerProvider;

  private final Provider<ErrorRecoveryManager> errorRecoveryManagerProvider;

  public ErrorHandlingModule_ProvideP2PErrorHandlerFactory(
      Provider<ErrorHandler> errorHandlerProvider,
      Provider<ErrorRecoveryManager> errorRecoveryManagerProvider) {
    this.errorHandlerProvider = errorHandlerProvider;
    this.errorRecoveryManagerProvider = errorRecoveryManagerProvider;
  }

  @Override
  public P2PErrorHandler get() {
    return provideP2PErrorHandler(errorHandlerProvider.get(), errorRecoveryManagerProvider.get());
  }

  public static ErrorHandlingModule_ProvideP2PErrorHandlerFactory create(
      Provider<ErrorHandler> errorHandlerProvider,
      Provider<ErrorRecoveryManager> errorRecoveryManagerProvider) {
    return new ErrorHandlingModule_ProvideP2PErrorHandlerFactory(errorHandlerProvider, errorRecoveryManagerProvider);
  }

  public static P2PErrorHandler provideP2PErrorHandler(ErrorHandler errorHandler,
      ErrorRecoveryManager errorRecoveryManager) {
    return Preconditions.checkNotNullFromProvides(ErrorHandlingModule.INSTANCE.provideP2PErrorHandler(errorHandler, errorRecoveryManager));
  }
}
