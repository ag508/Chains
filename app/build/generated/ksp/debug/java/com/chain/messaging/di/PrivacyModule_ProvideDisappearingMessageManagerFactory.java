package com.chain.messaging.di;

import android.content.Context;
import com.chain.messaging.core.blockchain.BlockchainManager;
import com.chain.messaging.core.privacy.DisappearingMessageManager;
import com.chain.messaging.core.privacy.ScreenshotDetector;
import com.chain.messaging.domain.repository.MessageRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class PrivacyModule_ProvideDisappearingMessageManagerFactory implements Factory<DisappearingMessageManager> {
  private final Provider<Context> contextProvider;

  private final Provider<MessageRepository> messageRepositoryProvider;

  private final Provider<BlockchainManager> blockchainManagerProvider;

  private final Provider<ScreenshotDetector> screenshotDetectorProvider;

  public PrivacyModule_ProvideDisappearingMessageManagerFactory(Provider<Context> contextProvider,
      Provider<MessageRepository> messageRepositoryProvider,
      Provider<BlockchainManager> blockchainManagerProvider,
      Provider<ScreenshotDetector> screenshotDetectorProvider) {
    this.contextProvider = contextProvider;
    this.messageRepositoryProvider = messageRepositoryProvider;
    this.blockchainManagerProvider = blockchainManagerProvider;
    this.screenshotDetectorProvider = screenshotDetectorProvider;
  }

  @Override
  public DisappearingMessageManager get() {
    return provideDisappearingMessageManager(contextProvider.get(), messageRepositoryProvider.get(), blockchainManagerProvider.get(), screenshotDetectorProvider.get());
  }

  public static PrivacyModule_ProvideDisappearingMessageManagerFactory create(
      Provider<Context> contextProvider, Provider<MessageRepository> messageRepositoryProvider,
      Provider<BlockchainManager> blockchainManagerProvider,
      Provider<ScreenshotDetector> screenshotDetectorProvider) {
    return new PrivacyModule_ProvideDisappearingMessageManagerFactory(contextProvider, messageRepositoryProvider, blockchainManagerProvider, screenshotDetectorProvider);
  }

  public static DisappearingMessageManager provideDisappearingMessageManager(Context context,
      MessageRepository messageRepository, BlockchainManager blockchainManager,
      ScreenshotDetector screenshotDetector) {
    return Preconditions.checkNotNullFromProvides(PrivacyModule.INSTANCE.provideDisappearingMessageManager(context, messageRepository, blockchainManager, screenshotDetector));
  }
}
