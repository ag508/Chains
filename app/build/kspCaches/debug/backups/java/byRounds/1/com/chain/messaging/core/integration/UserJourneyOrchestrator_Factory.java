package com.chain.messaging.core.integration;

import com.chain.messaging.core.auth.AuthenticationService;
import com.chain.messaging.core.crypto.SignalEncryptionService;
import com.chain.messaging.core.messaging.MessagingService;
import com.chain.messaging.core.security.IdentityVerificationManager;
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
public final class UserJourneyOrchestrator_Factory implements Factory<UserJourneyOrchestrator> {
  private final Provider<AuthenticationService> authenticationServiceProvider;

  private final Provider<SignalEncryptionService> encryptionServiceProvider;

  private final Provider<MessagingService> messagingServiceProvider;

  private final Provider<IdentityVerificationManager> identityVerificationManagerProvider;

  public UserJourneyOrchestrator_Factory(
      Provider<AuthenticationService> authenticationServiceProvider,
      Provider<SignalEncryptionService> encryptionServiceProvider,
      Provider<MessagingService> messagingServiceProvider,
      Provider<IdentityVerificationManager> identityVerificationManagerProvider) {
    this.authenticationServiceProvider = authenticationServiceProvider;
    this.encryptionServiceProvider = encryptionServiceProvider;
    this.messagingServiceProvider = messagingServiceProvider;
    this.identityVerificationManagerProvider = identityVerificationManagerProvider;
  }

  @Override
  public UserJourneyOrchestrator get() {
    return newInstance(authenticationServiceProvider.get(), encryptionServiceProvider.get(), messagingServiceProvider.get(), identityVerificationManagerProvider.get());
  }

  public static UserJourneyOrchestrator_Factory create(
      Provider<AuthenticationService> authenticationServiceProvider,
      Provider<SignalEncryptionService> encryptionServiceProvider,
      Provider<MessagingService> messagingServiceProvider,
      Provider<IdentityVerificationManager> identityVerificationManagerProvider) {
    return new UserJourneyOrchestrator_Factory(authenticationServiceProvider, encryptionServiceProvider, messagingServiceProvider, identityVerificationManagerProvider);
  }

  public static UserJourneyOrchestrator newInstance(AuthenticationService authenticationService,
      SignalEncryptionService encryptionService, MessagingService messagingService,
      IdentityVerificationManager identityVerificationManager) {
    return new UserJourneyOrchestrator(authenticationService, encryptionService, messagingService, identityVerificationManager);
  }
}
