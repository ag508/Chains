package com.chain.messaging.presentation.group;

import com.chain.messaging.core.group.GroupManager;
import com.chain.messaging.domain.repository.UserRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class GroupSettingsViewModel_Factory implements Factory<GroupSettingsViewModel> {
  private final Provider<GroupManager> groupManagerProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  public GroupSettingsViewModel_Factory(Provider<GroupManager> groupManagerProvider,
      Provider<UserRepository> userRepositoryProvider) {
    this.groupManagerProvider = groupManagerProvider;
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public GroupSettingsViewModel get() {
    return newInstance(groupManagerProvider.get(), userRepositoryProvider.get());
  }

  public static GroupSettingsViewModel_Factory create(Provider<GroupManager> groupManagerProvider,
      Provider<UserRepository> userRepositoryProvider) {
    return new GroupSettingsViewModel_Factory(groupManagerProvider, userRepositoryProvider);
  }

  public static GroupSettingsViewModel newInstance(GroupManager groupManager,
      UserRepository userRepository) {
    return new GroupSettingsViewModel(groupManager, userRepository);
  }
}
