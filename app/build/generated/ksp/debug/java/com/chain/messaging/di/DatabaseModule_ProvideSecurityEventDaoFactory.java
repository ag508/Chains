package com.chain.messaging.di;

import com.chain.messaging.data.local.ChainDatabase;
import com.chain.messaging.data.local.dao.SecurityEventDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class DatabaseModule_ProvideSecurityEventDaoFactory implements Factory<SecurityEventDao> {
  private final Provider<ChainDatabase> databaseProvider;

  public DatabaseModule_ProvideSecurityEventDaoFactory(Provider<ChainDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public SecurityEventDao get() {
    return provideSecurityEventDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideSecurityEventDaoFactory create(
      Provider<ChainDatabase> databaseProvider) {
    return new DatabaseModule_ProvideSecurityEventDaoFactory(databaseProvider);
  }

  public static SecurityEventDao provideSecurityEventDao(ChainDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideSecurityEventDao(database));
  }
}
