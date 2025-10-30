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
public final class ErrorRecoveryManager_Factory implements Factory<ErrorRecoveryManager> {
  private final Provider<ErrorHandler> errorHandlerProvider;

  private final Provider<NetworkMonitor> networkMonitorProvider;

  public ErrorRecoveryManager_Factory(Provider<ErrorHandler> errorHandlerProvider,
      Provider<NetworkMonitor> networkMonitorProvider) {
    this.errorHandlerProvider = errorHandlerProvider;
    this.networkMonitorProvider = networkMonitorProvider;
  }

  @Override
  public ErrorRecoveryManager get() {
    return newInstance(errorHandlerProvider.get(), networkMonitorProvider.get());
  }

  public static ErrorRecoveryManager_Factory create(Provider<ErrorHandler> errorHandlerProvider,
      Provider<NetworkMonitor> networkMonitorProvider) {
    return new ErrorRecoveryManager_Factory(errorHandlerProvider, networkMonitorProvider);
  }

  public static ErrorRecoveryManager newInstance(ErrorHandler errorHandler,
      NetworkMonitor networkMonitor) {
    return new ErrorRecoveryManager(errorHandler, networkMonitor);
  }
}
