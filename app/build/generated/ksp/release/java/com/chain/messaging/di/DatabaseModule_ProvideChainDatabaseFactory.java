package com.chain.messaging.di;

import android.content.Context;
import com.chain.messaging.data.local.ChainDatabase;
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
public final class DatabaseModule_ProvideChainDatabaseFactory implements Factory<ChainDatabase> {
  private final Provider<Context> contextProvider;

  private final Provider<String> passphraseProvider;

  public DatabaseModule_ProvideChainDatabaseFactory(Provider<Context> contextProvider,
      Provider<String> passphraseProvider) {
    this.contextProvider = contextProvider;
    this.passphraseProvider = passphraseProvider;
  }

  @Override
  public ChainDatabase get() {
    return provideChainDatabase(contextProvider.get(), passphraseProvider.get());
  }

  public static DatabaseModule_ProvideChainDatabaseFactory create(Provider<Context> contextProvider,
      Provider<String> passphraseProvider) {
    return new DatabaseModule_ProvideChainDatabaseFactory(contextProvider, passphraseProvider);
  }

  public static ChainDatabase provideChainDatabase(Context context, String passphrase) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideChainDatabase(context, passphrase));
  }
}
