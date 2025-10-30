package com.chain.messaging.di;

import com.chain.messaging.core.error.ErrorHandler;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class ErrorHandlingModule_ProvideErrorHandlerFactory implements Factory<ErrorHandler> {
  @Override
  public ErrorHandler get() {
    return provideErrorHandler();
  }

  public static ErrorHandlingModule_ProvideErrorHandlerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ErrorHandler provideErrorHandler() {
    return Preconditions.checkNotNullFromProvides(ErrorHandlingModule.INSTANCE.provideErrorHandler());
  }

  private static final class InstanceHolder {
    private static final ErrorHandlingModule_ProvideErrorHandlerFactory INSTANCE = new ErrorHandlingModule_ProvideErrorHandlerFactory();
  }
}
