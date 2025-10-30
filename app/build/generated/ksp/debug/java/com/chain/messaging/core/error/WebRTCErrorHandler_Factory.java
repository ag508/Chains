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
public final class WebRTCErrorHandler_Factory implements Factory<WebRTCErrorHandler> {
  private final Provider<ErrorHandler> errorHandlerProvider;

  private final Provider<ErrorRecoveryManager> errorRecoveryManagerProvider;

  public WebRTCErrorHandler_Factory(Provider<ErrorHandler> errorHandlerProvider,
      Provider<ErrorRecoveryManager> errorRecoveryManagerProvider) {
    this.errorHandlerProvider = errorHandlerProvider;
    this.errorRecoveryManagerProvider = errorRecoveryManagerProvider;
  }

  @Override
  public WebRTCErrorHandler get() {
    return newInstance(errorHandlerProvider.get(), errorRecoveryManagerProvider.get());
  }

  public static WebRTCErrorHandler_Factory create(Provider<ErrorHandler> errorHandlerProvider,
      Provider<ErrorRecoveryManager> errorRecoveryManagerProvider) {
    return new WebRTCErrorHandler_Factory(errorHandlerProvider, errorRecoveryManagerProvider);
  }

  public static WebRTCErrorHandler newInstance(ErrorHandler errorHandler,
      ErrorRecoveryManager errorRecoveryManager) {
    return new WebRTCErrorHandler(errorHandler, errorRecoveryManager);
  }
}
