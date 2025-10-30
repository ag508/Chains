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
public final class OfflineModule_Companion_ProvideQueuedMessageDaoFactory implements Factory<QueuedMessageDao> {
  private final Provider<ChainDatabase> databaseProvider;

  public OfflineModule_Companion_ProvideQueuedMessageDaoFactory(
      Provider<ChainDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public QueuedMessageDao get() {
    return provideQueuedMessageDao(databaseProvider.get());
  }

  public static OfflineModule_Companion_ProvideQueuedMessageDaoFactory create(
      Provider<ChainDatabase> databaseProvider) {
    return new OfflineModule_Companion_ProvideQueuedMessageDaoFactory(databaseProvider);
  }

  public static QueuedMessageDao provideQueuedMessageDao(ChainDatabase database) {
    return Preconditions.checkNotNullFromProvides(OfflineModule.Companion.provideQueuedMessageDao(database));
  }
}
