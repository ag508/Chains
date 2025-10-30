package com.chain.messaging.core.security;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class SafetyNumberGenerator_Factory implements Factory<SafetyNumberGenerator> {
  @Override
  public SafetyNumberGenerator get() {
    return newInstance();
  }

  public static SafetyNumberGenerator_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SafetyNumberGenerator newInstance() {
    return new SafetyNumberGenerator();
  }

  private static final class InstanceHolder {
    private static final SafetyNumberGenerator_Factory INSTANCE = new SafetyNumberGenerator_Factory();
  }
}
