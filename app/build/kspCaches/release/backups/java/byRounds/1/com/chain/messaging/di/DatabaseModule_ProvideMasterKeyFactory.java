package com.chain.messaging.di;

import android.content.Context;
import androidx.security.crypto.MasterKey;
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
public final class DatabaseModule_ProvideMasterKeyFactory implements Factory<MasterKey> {
  private final Provider<Context> contextProvider;

  public DatabaseModule_ProvideMasterKeyFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public MasterKey get() {
    return provideMasterKey(contextProvider.get());
  }

  public static DatabaseModule_ProvideMasterKeyFactory create(Provider<Context> contextProvider) {
    return new DatabaseModule_ProvideMasterKeyFactory(contextProvider);
  }

  public static MasterKey provideMasterKey(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideMasterKey(context));
  }
}
