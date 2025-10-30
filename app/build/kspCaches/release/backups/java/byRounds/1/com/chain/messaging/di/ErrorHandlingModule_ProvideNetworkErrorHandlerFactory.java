package com.chain.messaging.di;

import com.chain.messaging.core.error.ErrorHandler;
import com.chain.messaging.core.error.ErrorRecoveryManager;
import com.chain.messaging.core.error.NetworkErrorHandler;
import com.chain.messaging.core.network.NetworkMonitor;
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
public final class ErrorHandlingModule_ProvideNetworkErrorHandlerFactory implements Factory<NetworkErrorHandler> {
  private final Provider<ErrorHandler> errorHandlerProvider;

  private final Provider<ErrorRecoveryManager> errorRecoveryManagerProvider;

  private final Provider<NetworkMonitor> networkMonitorProvider;

  public ErrorHandlingModule_ProvideNetworkErrorHandlerFactory(
      Provider<ErrorHandler> errorHandlerProvider,
      Provider<ErrorRecoveryManager> errorRecoveryManagerProvider,
      Provider<NetworkMonitor> networkMonitorProvider) {
    this.errorHandlerProvider = errorHandlerProvider;
    this.errorRecoveryManagerProvider = errorRecoveryManagerProvider;
    this.networkMonitorProvider = networkMonitorProvider;
  }

  @Override
  public NetworkErrorHandler get() {
    return provideNetworkErrorHandler(errorHandlerProvider.get(), errorRecoveryManagerProvider.get(), networkMonitorProvider.get());
  }

  public static ErrorHandlingModule_ProvideNetworkErrorHandlerFactory create(
      Provider<ErrorHandler> errorHandlerProvider,
      Provider<ErrorRecoveryManager> errorRecoveryManagerProvider,
      Provider<NetworkMonitor> networkMonitorProvider) {
    return new ErrorHandlingModule_ProvideNetworkErrorHandlerFactory(errorHandlerProvider, errorRecoveryManagerProvider, networkMonitorProvider);
  }

  public static NetworkErrorHandler provideNetworkErrorHandler(ErrorHandler errorHandler,
      ErrorRecoveryManager errorRecoveryManager, NetworkMonitor networkMonitor) {
    return Preconditions.checkNotNullFromProvides(ErrorHandlingModule.INSTANCE.provideNetworkErrorHandler(errorHandler, errorRecoveryManager, networkMonitor));
  }
}
