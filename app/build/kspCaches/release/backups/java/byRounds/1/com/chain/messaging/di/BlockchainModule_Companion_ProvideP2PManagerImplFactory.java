package com.chain.messaging.di;

import com.chain.messaging.core.p2p.P2PManagerImpl;
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
public final class BlockchainModule_Companion_ProvideP2PManagerImplFactory implements Factory<P2PManagerImpl> {
  @Override
  public P2PManagerImpl get() {
    return provideP2PManagerImpl();
  }

  public static BlockchainModule_Companion_ProvideP2PManagerImplFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static P2PManagerImpl provideP2PManagerImpl() {
    return Preconditions.checkNotNullFromProvides(BlockchainModule.Companion.provideP2PManagerImpl());
  }

  private static final class InstanceHolder {
    private static final BlockchainModule_Companion_ProvideP2PManagerImplFactory INSTANCE = new BlockchainModule_Companion_ProvideP2PManagerImplFactory();
  }
}
