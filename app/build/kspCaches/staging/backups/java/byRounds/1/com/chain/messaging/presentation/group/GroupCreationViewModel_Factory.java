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
public final class GroupCreationViewModel_Factory implements Factory<GroupCreationViewModel> {
  private final Provider<GroupManager> groupManagerProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  public GroupCreationViewModel_Factory(Provider<GroupManager> groupManagerProvider,
      Provider<UserRepository> userRepositoryProvider) {
    this.groupManagerProvider = groupManagerProvider;
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public GroupCreationViewModel get() {
    return newInstance(groupManagerProvider.get(), userRepositoryProvider.get());
  }

  public static GroupCreationViewModel_Factory create(Provider<GroupManager> groupManagerProvider,
      Provider<UserRepository> userRepositoryProvider) {
    return new GroupCreationViewModel_Factory(groupManagerProvider, userRepositoryProvider);
  }

  public static GroupCreationViewModel newInstance(GroupManager groupManager,
      UserRepository userRepository) {
    return new GroupCreationViewModel(groupManager, userRepository);
  }
}
