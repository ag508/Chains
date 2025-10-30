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
public final class P2PErrorHandler_Factory implements Factory<P2PErrorHandler> {
  private final Provider<ErrorHandler> errorHandlerProvider;

  private final Provider<ErrorRecoveryManager> errorRecoveryManagerProvider;

  public P2PErrorHandler_Factory(Provider<ErrorHandler> errorHandlerProvider,
      Provider<ErrorRecoveryManager> errorRecoveryManagerProvider) {
    this.errorHandlerProvider = errorHandlerProvider;
    this.errorRecoveryManagerProvider = errorRecoveryManagerProvider;
  }

  @Override
  public P2PErrorHandler get() {
    return newInstance(errorHandlerProvider.get(), errorRecoveryManagerProvider.get());
  }

  public static P2PErrorHandler_Factory create(Provider<ErrorHandler> errorHandlerProvider,
      Provider<ErrorRecoveryManager> errorRecoveryManagerProvider) {
    return new P2PErrorHandler_Factory(errorHandlerProvider, errorRecoveryManagerProvider);
  }

  public static P2PErrorHandler newInstance(ErrorHandler errorHandler,
      ErrorRecoveryManager errorRecoveryManager) {
    return new P2PErrorHandler(errorHandler, errorRecoveryManager);
  }
}
