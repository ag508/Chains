package com.chain.messaging.data.repository;

import com.chain.messaging.data.local.dao.UserDao;
import com.chain.messaging.data.local.dao.UserSettingsDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class UserRepositoryImpl_Factory implements Factory<UserRepositoryImpl> {
  private final Provider<UserDao> userDaoProvider;

  private final Provider<UserSettingsDao> userSettingsDaoProvider;

  public UserRepositoryImpl_Factory(Provider<UserDao> userDaoProvider,
      Provider<UserSettingsDao> userSettingsDaoProvider) {
    this.userDaoProvider = userDaoProvider;
    this.userSettingsDaoProvider = userSettingsDaoProvider;
  }

  @Override
  public UserRepositoryImpl get() {
    return newInstance(userDaoProvider.get(), userSettingsDaoProvider.get());
  }

  public static UserRepositoryImpl_Factory create(Provider<UserDao> userDaoProvider,
      Provider<UserSettingsDao> userSettingsDaoProvider) {
    return new UserRepositoryImpl_Factory(userDaoProvider, userSettingsDaoProvider);
  }

  public static UserRepositoryImpl newInstance(UserDao userDao, UserSettingsDao userSettingsDao) {
    return new UserRepositoryImpl(userDao, userSettingsDao);
  }
}
