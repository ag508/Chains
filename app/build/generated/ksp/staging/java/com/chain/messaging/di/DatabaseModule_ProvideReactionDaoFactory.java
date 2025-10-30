package com.chain.messaging.di;

import com.chain.messaging.data.local.ChainDatabase;
import com.chain.messaging.data.local.dao.ReactionDao;
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
public final class DatabaseModule_ProvideReactionDaoFactory implements Factory<ReactionDao> {
  private final Provider<ChainDatabase> databaseProvider;

  public DatabaseModule_ProvideReactionDaoFactory(Provider<ChainDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ReactionDao get() {
    return provideReactionDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideReactionDaoFactory create(
      Provider<ChainDatabase> databaseProvider) {
    return new DatabaseModule_ProvideReactionDaoFactory(databaseProvider);
  }

  public static ReactionDao provideReactionDao(ChainDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideReactionDao(database));
  }
}
