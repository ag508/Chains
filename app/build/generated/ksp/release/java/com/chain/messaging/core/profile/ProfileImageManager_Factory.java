package com.chain.messaging.core.profile;

import android.content.Context;
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
public final class ProfileImageManager_Factory implements Factory<ProfileImageManager> {
  private final Provider<Context> contextProvider;

  public ProfileImageManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ProfileImageManager get() {
    return newInstance(contextProvider.get());
  }

  public static ProfileImageManager_Factory create(Provider<Context> contextProvider) {
    return new ProfileImageManager_Factory(contextProvider);
  }

  public static ProfileImageManager newInstance(Context context) {
    return new ProfileImageManager(context);
  }
}
