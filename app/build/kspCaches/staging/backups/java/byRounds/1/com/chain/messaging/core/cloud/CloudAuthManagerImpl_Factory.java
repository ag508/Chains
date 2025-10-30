package com.chain.messaging.core.cloud;

import android.content.Context;
import com.chain.messaging.core.security.SecureStorage;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import kotlinx.serialization.json.Json;
import okhttp3.OkHttpClient;

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
public final class CloudAuthManagerImpl_Factory implements Factory<CloudAuthManagerImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<SecureStorage> secureStorageProvider;

  private final Provider<OkHttpClient> httpClientProvider;

  private final Provider<Json> jsonProvider;

  public CloudAuthManagerImpl_Factory(Provider<Context> contextProvider,
      Provider<SecureStorage> secureStorageProvider, Provider<OkHttpClient> httpClientProvider,
      Provider<Json> jsonProvider) {
    this.contextProvider = contextProvider;
    this.secureStorageProvider = secureStorageProvider;
    this.httpClientProvider = httpClientProvider;
    this.jsonProvider = jsonProvider;
  }

  @Override
  public CloudAuthManagerImpl get() {
    return newInstance(contextProvider.get(), secureStorageProvider.get(), httpClientProvider.get(), jsonProvider.get());
  }

  public static CloudAuthManagerImpl_Factory create(Provider<Context> contextProvider,
      Provider<SecureStorage> secureStorageProvider, Provider<OkHttpClient> httpClientProvider,
      Provider<Json> jsonProvider) {
    return new CloudAuthManagerImpl_Factory(contextProvider, secureStorageProvider, httpClientProvider, jsonProvider);
  }

  public static CloudAuthManagerImpl newInstance(Context context, SecureStorage secureStorage,
      OkHttpClient httpClient, Json json) {
    return new CloudAuthManagerImpl(context, secureStorage, httpClient, json);
  }
}
