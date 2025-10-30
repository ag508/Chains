package com.chain.messaging.di;

import android.content.Context;
import androidx.security.crypto.EncryptedSharedPreferences;
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
public final class DatabaseModule_ProvideEncryptedSharedPreferencesFactory implements Factory<EncryptedSharedPreferences> {
  private final Provider<Context> contextProvider;

  private final Provider<MasterKey> masterKeyProvider;

  public DatabaseModule_ProvideEncryptedSharedPreferencesFactory(Provider<Context> contextProvider,
      Provider<MasterKey> masterKeyProvider) {
    this.contextProvider = contextProvider;
    this.masterKeyProvider = masterKeyProvider;
  }

  @Override
  public EncryptedSharedPreferences get() {
    return provideEncryptedSharedPreferences(contextProvider.get(), masterKeyProvider.get());
  }

  public static DatabaseModule_ProvideEncryptedSharedPreferencesFactory create(
      Provider<Context> contextProvider, Provider<MasterKey> masterKeyProvider) {
    return new DatabaseModule_ProvideEncryptedSharedPreferencesFactory(contextProvider, masterKeyProvider);
  }

  public static EncryptedSharedPreferences provideEncryptedSharedPreferences(Context context,
      MasterKey masterKey) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideEncryptedSharedPreferences(context, masterKey));
  }
}
