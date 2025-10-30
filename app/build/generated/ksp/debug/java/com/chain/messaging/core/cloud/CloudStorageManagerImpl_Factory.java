package com.chain.messaging.core.cloud;

import android.content.Context;
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
public final class CloudStorageManagerImpl_Factory implements Factory<CloudStorageManagerImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<CloudAuthManager> cloudAuthManagerProvider;

  private final Provider<FileEncryption> fileEncryptionProvider;

  private final Provider<OkHttpClient> httpClientProvider;

  private final Provider<Json> jsonProvider;

  public CloudStorageManagerImpl_Factory(Provider<Context> contextProvider,
      Provider<CloudAuthManager> cloudAuthManagerProvider,
      Provider<FileEncryption> fileEncryptionProvider, Provider<OkHttpClient> httpClientProvider,
      Provider<Json> jsonProvider) {
    this.contextProvider = contextProvider;
    this.cloudAuthManagerProvider = cloudAuthManagerProvider;
    this.fileEncryptionProvider = fileEncryptionProvider;
    this.httpClientProvider = httpClientProvider;
    this.jsonProvider = jsonProvider;
  }

  @Override
  public CloudStorageManagerImpl get() {
    return newInstance(contextProvider.get(), cloudAuthManagerProvider.get(), fileEncryptionProvider.get(), httpClientProvider.get(), jsonProvider.get());
  }

  public static CloudStorageManagerImpl_Factory create(Provider<Context> contextProvider,
      Provider<CloudAuthManager> cloudAuthManagerProvider,
      Provider<FileEncryption> fileEncryptionProvider, Provider<OkHttpClient> httpClientProvider,
      Provider<Json> jsonProvider) {
    return new CloudStorageManagerImpl_Factory(contextProvider, cloudAuthManagerProvider, fileEncryptionProvider, httpClientProvider, jsonProvider);
  }

  public static CloudStorageManagerImpl newInstance(Context context,
      CloudAuthManager cloudAuthManager, FileEncryption fileEncryption, OkHttpClient httpClient,
      Json json) {
    return new CloudStorageManagerImpl(context, cloudAuthManager, fileEncryption, httpClient, json);
  }
}
