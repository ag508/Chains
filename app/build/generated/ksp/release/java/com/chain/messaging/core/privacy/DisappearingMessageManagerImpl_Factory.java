package com.chain.messaging.core.privacy;

import android.content.Context;
import com.chain.messaging.core.blockchain.BlockchainManager;
import com.chain.messaging.domain.repository.MessageRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class DisappearingMessageManagerImpl_Factory implements Factory<DisappearingMessageManagerImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<MessageRepository> messageRepositoryProvider;

  private final Provider<BlockchainManager> blockchainManagerProvider;

  private final Provider<ScreenshotDetector> screenshotDetectorProvider;

  public DisappearingMessageManagerImpl_Factory(Provider<Context> contextProvider,
      Provider<MessageRepository> messageRepositoryProvider,
      Provider<BlockchainManager> blockchainManagerProvider,
      Provider<ScreenshotDetector> screenshotDetectorProvider) {
    this.contextProvider = contextProvider;
    this.messageRepositoryProvider = messageRepositoryProvider;
    this.blockchainManagerProvider = blockchainManagerProvider;
    this.screenshotDetectorProvider = screenshotDetectorProvider;
  }

  @Override
  public DisappearingMessageManagerImpl get() {
    return newInstance(contextProvider.get(), messageRepositoryProvider.get(), blockchainManagerProvider.get(), screenshotDetectorProvider.get());
  }

  public static DisappearingMessageManagerImpl_Factory create(Provider<Context> contextProvider,
      Provider<MessageRepository> messageRepositoryProvider,
      Provider<BlockchainManager> blockchainManagerProvider,
      Provider<ScreenshotDetector> screenshotDetectorProvider) {
    return new DisappearingMessageManagerImpl_Factory(contextProvider, messageRepositoryProvider, blockchainManagerProvider, screenshotDetectorProvider);
  }

  public static DisappearingMessageManagerImpl newInstance(Context context,
      MessageRepository messageRepository, BlockchainManager blockchainManager,
      ScreenshotDetector screenshotDetector) {
    return new DisappearingMessageManagerImpl(context, messageRepository, blockchainManager, screenshotDetector);
  }
}
