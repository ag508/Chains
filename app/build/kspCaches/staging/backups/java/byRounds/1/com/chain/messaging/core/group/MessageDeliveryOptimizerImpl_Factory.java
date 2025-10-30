package com.chain.messaging.core.group;

import com.chain.messaging.core.p2p.P2PManager;
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
public final class MessageDeliveryOptimizerImpl_Factory implements Factory<MessageDeliveryOptimizerImpl> {
  private final Provider<P2PManager> p2pManagerProvider;

  public MessageDeliveryOptimizerImpl_Factory(Provider<P2PManager> p2pManagerProvider) {
    this.p2pManagerProvider = p2pManagerProvider;
  }

  @Override
  public MessageDeliveryOptimizerImpl get() {
    return newInstance(p2pManagerProvider.get());
  }

  public static MessageDeliveryOptimizerImpl_Factory create(
      Provider<P2PManager> p2pManagerProvider) {
    return new MessageDeliveryOptimizerImpl_Factory(p2pManagerProvider);
  }

  public static MessageDeliveryOptimizerImpl newInstance(P2PManager p2pManager) {
    return new MessageDeliveryOptimizerImpl(p2pManager);
  }
}
