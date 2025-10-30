package com.chain.messaging.core.webrtc;

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
public final class IceServerProvider_Factory implements Factory<IceServerProvider> {
  @Override
  public IceServerProvider get() {
    return newInstance();
  }

  public static IceServerProvider_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static IceServerProvider newInstance() {
    return new IceServerProvider();
  }

  private static final class InstanceHolder {
    private static final IceServerProvider_Factory INSTANCE = new IceServerProvider_Factory();
  }
}
