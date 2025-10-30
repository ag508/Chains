package com.chain.messaging.di;

import androidx.security.crypto.EncryptedSharedPreferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideDatabasePassphraseFactory implements Factory<String> {
  private final Provider<EncryptedSharedPreferences> encryptedSharedPreferencesProvider;

  public DatabaseModule_ProvideDatabasePassphraseFactory(
      Provider<EncryptedSharedPreferences> encryptedSharedPreferencesProvider) {
    this.encryptedSharedPreferencesProvider = encryptedSharedPreferencesProvider;
  }

  @Override
  public String get() {
    return provideDatabasePassphrase(encryptedSharedPreferencesProvider.get());
  }

  public static DatabaseModule_ProvideDatabasePassphraseFactory create(
      Provider<EncryptedSharedPreferences> encryptedSharedPreferencesProvider) {
    return new DatabaseModule_ProvideDatabasePassphraseFactory(encryptedSharedPreferencesProvider);
  }

  public static String provideDatabasePassphrase(
      EncryptedSharedPreferences encryptedSharedPreferences) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideDatabasePassphrase(encryptedSharedPreferences));
  }
}
