package com.chain.messaging.presentation.error;

import com.chain.messaging.core.error.ErrorHandler;
import com.chain.messaging.core.error.ErrorRecoveryManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class ErrorViewModel_Factory implements Factory<ErrorViewModel> {
  private final Provider<ErrorHandler> errorHandlerProvider;

  private final Provider<ErrorRecoveryManager> errorRecoveryManagerProvider;

  public ErrorViewModel_Factory(Provider<ErrorHandler> errorHandlerProvider,
      Provider<ErrorRecoveryManager> errorRecoveryManagerProvider) {
    this.errorHandlerProvider = errorHandlerProvider;
    this.errorRecoveryManagerProvider = errorRecoveryManagerProvider;
  }

  @Override
  public ErrorViewModel get() {
    return newInstance(errorHandlerProvider.get(), errorRecoveryManagerProvider.get());
  }

  public static ErrorViewModel_Factory create(Provider<ErrorHandler> errorHandlerProvider,
      Provider<ErrorRecoveryManager> errorRecoveryManagerProvider) {
    return new ErrorViewModel_Factory(errorHandlerProvider, errorRecoveryManagerProvider);
  }

  public static ErrorViewModel newInstance(ErrorHandler errorHandler,
      ErrorRecoveryManager errorRecoveryManager) {
    return new ErrorViewModel(errorHandler, errorRecoveryManager);
  }
}
