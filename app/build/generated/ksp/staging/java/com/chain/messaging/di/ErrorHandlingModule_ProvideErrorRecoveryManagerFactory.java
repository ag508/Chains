package com.chain.messaging.di;

import com.chain.messaging.core.error.ErrorHandler;
import com.chain.messaging.core.error.ErrorRecoveryManager;
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
public final class ErrorHandlingModule_ProvideErrorRecoveryManagerFactory implements Factory<ErrorRecoveryManager> {
  private final Provider<ErrorHandler> errorHandlerProvider;

  private final Provider<NetworkMonitor> networkMonitorProvider;

  public ErrorHandlingModule_ProvideErrorRecoveryManagerFactory(
      Provider<ErrorHandler> errorHandlerProvider,
      Provider<NetworkMonitor> networkMonitorProvider) {
    this.errorHandlerProvider = errorHandlerProvider;
    this.networkMonitorProvider = networkMonitorProvider;
  }

  @Override
  public ErrorRecoveryManager get() {
    return provideErrorRecoveryManager(errorHandlerProvider.get(), networkMonitorProvider.get());
  }

  public static ErrorHandlingModule_ProvideErrorRecoveryManagerFactory create(
      Provider<ErrorHandler> errorHandlerProvider,
      Provider<NetworkMonitor> networkMonitorProvider) {
    return new ErrorHandlingModule_ProvideErrorRecoveryManagerFactory(errorHandlerProvider, networkMonitorProvider);
  }

  public static ErrorRecoveryManager provideErrorRecoveryManager(ErrorHandler errorHandler,
      NetworkMonitor networkMonitor) {
    return Preconditions.checkNotNullFromProvides(ErrorHandlingModule.INSTANCE.provideErrorRecoveryManager(errorHandler, networkMonitor));
  }
}
