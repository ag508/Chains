package com.chain.messaging.di;

import com.chain.messaging.core.security.SafetyNumberGenerator;
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
public final class SecurityModule_ProvideSafetyNumberGeneratorFactory implements Factory<SafetyNumberGenerator> {
  @Override
  public SafetyNumberGenerator get() {
    return provideSafetyNumberGenerator();
  }

  public static SecurityModule_ProvideSafetyNumberGeneratorFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SafetyNumberGenerator provideSafetyNumberGenerator() {
    return Preconditions.checkNotNullFromProvides(SecurityModule.INSTANCE.provideSafetyNumberGenerator());
  }

  private static final class InstanceHolder {
    private static final SecurityModule_ProvideSafetyNumberGeneratorFactory INSTANCE = new SecurityModule_ProvideSafetyNumberGeneratorFactory();
  }
}
