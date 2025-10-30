package com.chain.messaging.di;

import android.content.Context;
import com.chain.messaging.core.media.MediaHandler;
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
public final class MediaModule_ProvideMediaHandlerFactory implements Factory<MediaHandler> {
  private final Provider<Context> contextProvider;

  public MediaModule_ProvideMediaHandlerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public MediaHandler get() {
    return provideMediaHandler(contextProvider.get());
  }

  public static MediaModule_ProvideMediaHandlerFactory create(Provider<Context> contextProvider) {
    return new MediaModule_ProvideMediaHandlerFactory(contextProvider);
  }

  public static MediaHandler provideMediaHandler(Context context) {
    return Preconditions.checkNotNullFromProvides(MediaModule.INSTANCE.provideMediaHandler(context));
  }
}
