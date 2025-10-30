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
public final class BlockchainErrorHandler_Factory implements Factory<BlockchainErrorHandler> {
  private final Provider<ErrorHandler> errorHandlerProvider;

  private final Provider<ErrorRecoveryManager> errorRecoveryManagerProvider;

  public BlockchainErrorHandler_Factory(Provider<ErrorHandler> errorHandlerProvider,
      Provider<ErrorRecoveryManager> errorRecoveryManagerProvider) {
    this.errorHandlerProvider = errorHandlerProvider;
    this.errorRecoveryManagerProvider = errorRecoveryManagerProvider;
  }

  @Override
  public BlockchainErrorHandler get() {
    return newInstance(errorHandlerProvider.get(), errorRecoveryManagerProvider.get());
  }

  public static BlockchainErrorHandler_Factory create(Provider<ErrorHandler> errorHandlerProvider,
      Provider<ErrorRecoveryManager> errorRecoveryManagerProvider) {
    return new BlockchainErrorHandler_Factory(errorHandlerProvider, errorRecoveryManagerProvider);
  }

  public static BlockchainErrorHandler newInstance(ErrorHandler errorHandler,
      ErrorRecoveryManager errorRecoveryManager) {
    return new BlockchainErrorHandler(errorHandler, errorRecoveryManager);
  }
}
