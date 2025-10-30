package com.chain.messaging.di;

import android.content.Context;
import com.chain.messaging.core.profile.ProfileImageManager;
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
public final class ProfileModule_ProvideProfileImageManagerFactory implements Factory<ProfileImageManager> {
  private final Provider<Context> contextProvider;

  public ProfileModule_ProvideProfileImageManagerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ProfileImageManager get() {
    return provideProfileImageManager(contextProvider.get());
  }

  public static ProfileModule_ProvideProfileImageManagerFactory create(
      Provider<Context> contextProvider) {
    return new ProfileModule_ProvideProfileImageManagerFactory(contextProvider);
  }

  public static ProfileImageManager provideProfileImageManager(Context context) {
    return Preconditions.checkNotNullFromProvides(ProfileModule.INSTANCE.provideProfileImageManager(context));
  }
}
