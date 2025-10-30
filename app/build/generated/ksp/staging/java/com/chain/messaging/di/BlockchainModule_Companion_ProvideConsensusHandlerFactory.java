package com.chain.messaging.di;

import com.chain.messaging.core.blockchain.ConsensusHandler;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class BlockchainModule_Companion_ProvideConsensusHandlerFactory implements Factory<ConsensusHandler> {
  @Override
  public ConsensusHandler get() {
    return provideConsensusHandler();
  }

  public static BlockchainModule_Companion_ProvideConsensusHandlerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ConsensusHandler provideConsensusHandler() {
    return Preconditions.checkNotNullFromProvides(BlockchainModule.Companion.provideConsensusHandler());
  }

  private static final class InstanceHolder {
    private static final BlockchainModule_Companion_ProvideConsensusHandlerFactory INSTANCE = new BlockchainModule_Companion_ProvideConsensusHandlerFactory();
  }
}
