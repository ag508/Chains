package com.chain.messaging.core.error;

import com.chain.messaging.core.network.NetworkMonitor;
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
public final class NetworkErrorHandler_Factory implements Factory<NetworkErrorHandler> {
  private final Provider<ErrorHandler> errorHandlerProvider;

  private final Provider<ErrorRecoveryManager> errorRecoveryManagerProvider;

  private final Provider<NetworkMonitor> networkMonitorProvider;

  public NetworkErrorHandler_Factory(Provider<ErrorHandler> errorHandlerProvider,
      Provider<ErrorRecoveryManager> errorRecoveryManagerProvider,
      Provider<NetworkMonitor> networkMonitorProvider) {
    this.errorHandlerProvider = errorHandlerProvider;
    this.errorRecoveryManagerProvider = errorRecoveryManagerProvider;
    this.networkMonitorProvider = networkMonitorProvider;
  }

  @Override
  public NetworkErrorHandler get() {
    return newInstance(errorHandlerProvider.get(), errorRecoveryManagerProvider.get(), networkMonitorProvider.get());
  }

  public static NetworkErrorHandler_Factory create(Provider<ErrorHandler> errorHandlerProvider,
      Provider<ErrorRecoveryManager> errorRecoveryManagerProvider,
      Provider<NetworkMonitor> networkMonitorProvider) {
    return new NetworkErrorHandler_Factory(errorHandlerProvider, errorRecoveryManagerProvider, networkMonitorProvider);
  }

  public static NetworkErrorHandler newInstance(ErrorHandler errorHandler,
      ErrorRecoveryManager errorRecoveryManager, NetworkMonitor networkMonitor) {
    return new NetworkErrorHandler(errorHandler, errorRecoveryManager, networkMonitor);
  }
}
