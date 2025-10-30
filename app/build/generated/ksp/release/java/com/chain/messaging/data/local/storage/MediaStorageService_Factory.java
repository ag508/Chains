package com.chain.messaging.data.local.storage;

import android.content.Context;
import com.chain.messaging.core.security.FileEncryption;
import com.chain.messaging.data.local.dao.MediaDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class MediaStorageService_Factory implements Factory<MediaStorageService> {
  private final Provider<Context> contextProvider;

  private final Provider<MediaDao> mediaDaoProvider;

  private final Provider<FileEncryption> fileEncryptionProvider;

  private final Provider<MediaCompressor> mediaCompressorProvider;

  private final Provider<ThumbnailGenerator> thumbnailGeneratorProvider;

  public MediaStorageService_Factory(Provider<Context> contextProvider,
      Provider<MediaDao> mediaDaoProvider, Provider<FileEncryption> fileEncryptionProvider,
      Provider<MediaCompressor> mediaCompressorProvider,
      Provider<ThumbnailGenerator> thumbnailGeneratorProvider) {
    this.contextProvider = contextProvider;
    this.mediaDaoProvider = mediaDaoProvider;
    this.fileEncryptionProvider = fileEncryptionProvider;
    this.mediaCompressorProvider = mediaCompressorProvider;
    this.thumbnailGeneratorProvider = thumbnailGeneratorProvider;
  }

  @Override
  public MediaStorageService get() {
    return newInstance(contextProvider.get(), mediaDaoProvider.get(), fileEncryptionProvider.get(), mediaCompressorProvider.get(), thumbnailGeneratorProvider.get());
  }

  public static MediaStorageService_Factory create(Provider<Context> contextProvider,
      Provider<MediaDao> mediaDaoProvider, Provider<FileEncryption> fileEncryptionProvider,
      Provider<MediaCompressor> mediaCompressorProvider,
      Provider<ThumbnailGenerator> thumbnailGeneratorProvider) {
    return new MediaStorageService_Factory(contextProvider, mediaDaoProvider, fileEncryptionProvider, mediaCompressorProvider, thumbnailGeneratorProvider);
  }

  public static MediaStorageService newInstance(Context context, MediaDao mediaDao,
      FileEncryption fileEncryption, MediaCompressor mediaCompressor,
      ThumbnailGenerator thumbnailGenerator) {
    return new MediaStorageService(context, mediaDao, fileEncryption, mediaCompressor, thumbnailGenerator);
  }
}
