package com.chain.messaging.di;

import com.chain.messaging.data.local.ChainDatabase;
import com.chain.messaging.data.local.dao.QueuedMessageDao;
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
public final class DatabaseModule_ProvideQueuedMessageDaoFactory implements Factory<QueuedMessageDao> {
  private final Provider<ChainDatabase> databaseProvider;

  public DatabaseModule_ProvideQueuedMessageDaoFactory(Provider<ChainDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public QueuedMessageDao get() {
    return provideQueuedMessageDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideQueuedMessageDaoFactory create(
      Provider<ChainDatabase> databaseProvider) {
    return new DatabaseModule_ProvideQueuedMessageDaoFactory(databaseProvider);
  }

  public static QueuedMessageDao provideQueuedMessageDao(ChainDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideQueuedMessageDao(database));
  }
}
