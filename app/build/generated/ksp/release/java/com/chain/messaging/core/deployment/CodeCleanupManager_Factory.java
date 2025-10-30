package com.chain.messaging.core.deployment;

import com.chain.messaging.core.util.Logger;
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
public final class CodeCleanupManager_Factory implements Factory<CodeCleanupManager> {
  private final Provider<Logger> loggerProvider;

  public CodeCleanupManager_Factory(Provider<Logger> loggerProvider) {
    this.loggerProvider = loggerProvider;
  }

  @Override
  public CodeCleanupManager get() {
    return newInstance(loggerProvider.get());
  }

  public static CodeCleanupManager_Factory create(Provider<Logger> loggerProvider) {
    return new CodeCleanupManager_Factory(loggerProvider);
  }

  public static CodeCleanupManager newInstance(Logger logger) {
    return new CodeCleanupManager(logger);
  }
}
