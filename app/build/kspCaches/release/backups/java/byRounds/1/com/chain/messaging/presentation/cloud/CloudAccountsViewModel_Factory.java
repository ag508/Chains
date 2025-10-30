package com.chain.messaging.presentation.cloud;

import com.chain.messaging.core.cloud.CloudAuthManager;
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
public final class CloudAccountsViewModel_Factory implements Factory<CloudAccountsViewModel> {
  private final Provider<CloudAuthManager> cloudAuthManagerProvider;

  public CloudAccountsViewModel_Factory(Provider<CloudAuthManager> cloudAuthManagerProvider) {
    this.cloudAuthManagerProvider = cloudAuthManagerProvider;
  }

  @Override
  public CloudAccountsViewModel get() {
    return newInstance(cloudAuthManagerProvider.get());
  }

  public static CloudAccountsViewModel_Factory create(
      Provider<CloudAuthManager> cloudAuthManagerProvider) {
    return new CloudAccountsViewModel_Factory(cloudAuthManagerProvider);
  }

  public static CloudAccountsViewModel newInstance(CloudAuthManager cloudAuthManager) {
    return new CloudAccountsViewModel(cloudAuthManager);
  }
}
