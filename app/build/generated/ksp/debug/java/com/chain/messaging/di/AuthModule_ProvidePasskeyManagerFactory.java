package com.chain.messaging.di;

import android.content.Context;
import com.chain.messaging.core.auth.PasskeyManager;
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
public final class AuthModule_ProvidePasskeyManagerFactory implements Factory<PasskeyManager> {
  private final Provider<Context> contextProvider;

  public AuthModule_ProvidePasskeyManagerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PasskeyManager get() {
    return providePasskeyManager(contextProvider.get());
  }

  public static AuthModule_ProvidePasskeyManagerFactory create(Provider<Context> contextProvider) {
    return new AuthModule_ProvidePasskeyManagerFactory(contextProvider);
  }

  public static PasskeyManager providePasskeyManager(Context context) {
    return Preconditions.checkNotNullFromProvides(AuthModule.INSTANCE.providePasskeyManager(context));
  }
}
