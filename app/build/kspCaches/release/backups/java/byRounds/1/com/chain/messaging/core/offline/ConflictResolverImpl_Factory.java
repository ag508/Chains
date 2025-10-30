package com.chain.messaging.core.offline;

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
public final class ConflictResolverImpl_Factory implements Factory<ConflictResolverImpl> {
  @Override
  public ConflictResolverImpl get() {
    return newInstance();
  }

  public static ConflictResolverImpl_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ConflictResolverImpl newInstance() {
    return new ConflictResolverImpl();
  }

  private static final class InstanceHolder {
    private static final ConflictResolverImpl_Factory INSTANCE = new ConflictResolverImpl_Factory();
  }
}
