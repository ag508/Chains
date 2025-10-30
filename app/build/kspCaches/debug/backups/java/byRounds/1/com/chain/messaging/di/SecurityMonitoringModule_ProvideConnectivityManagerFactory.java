package com.chain.messaging.di;

import android.content.Context;
import android.net.ConnectivityManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class SecurityMonitoringModule_ProvideConnectivityManagerFactory implements Factory<ConnectivityManager> {
  private final Provider<Context> contextProvider;

  public SecurityMonitoringModule_ProvideConnectivityManagerFactory(
      Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ConnectivityManager get() {
    return provideConnectivityManager(contextProvider.get());
  }

  public static SecurityMonitoringModule_ProvideConnectivityManagerFactory create(
      Provider<Context> contextProvider) {
    return new SecurityMonitoringModule_ProvideConnectivityManagerFactory(contextProvider);
  }

  public static ConnectivityManager provideConnectivityManager(Context context) {
    return Preconditions.checkNotNullFromProvides(SecurityMonitoringModule.INSTANCE.provideConnectivityManager(context));
  }
}
