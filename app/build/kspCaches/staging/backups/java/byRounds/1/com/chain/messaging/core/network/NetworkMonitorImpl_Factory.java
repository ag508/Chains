package com.chain.messaging.core.network;

import android.content.Context;
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
public final class NetworkMonitorImpl_Factory implements Factory<NetworkMonitorImpl> {
  private final Provider<Context> contextProvider;

  public NetworkMonitorImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public NetworkMonitorImpl get() {
    return newInstance(contextProvider.get());
  }

  public static NetworkMonitorImpl_Factory create(Provider<Context> contextProvider) {
    return new NetworkMonitorImpl_Factory(contextProvider);
  }

  public static NetworkMonitorImpl newInstance(Context context) {
    return new NetworkMonitorImpl(context);
  }
}
