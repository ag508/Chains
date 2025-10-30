package com.chain.messaging.di;

import com.chain.messaging.data.local.ChainDatabase;
import com.chain.messaging.data.local.dao.ChatDao;
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
public final class DatabaseModule_ProvideChatDaoFactory implements Factory<ChatDao> {
  private final Provider<ChainDatabase> databaseProvider;

  public DatabaseModule_ProvideChatDaoFactory(Provider<ChainDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ChatDao get() {
    return provideChatDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideChatDaoFactory create(
      Provider<ChainDatabase> databaseProvider) {
    return new DatabaseModule_ProvideChatDaoFactory(databaseProvider);
  }

  public static ChatDao provideChatDao(ChainDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideChatDao(database));
  }
}
