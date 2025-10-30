package com.chain.messaging.core.group;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class InviteLinkGeneratorImpl_Factory implements Factory<InviteLinkGeneratorImpl> {
  @Override
  public InviteLinkGeneratorImpl get() {
    return newInstance();
  }

  public static InviteLinkGeneratorImpl_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static InviteLinkGeneratorImpl newInstance() {
    return new InviteLinkGeneratorImpl();
  }

  private static final class InstanceHolder {
    private static final InviteLinkGeneratorImpl_Factory INSTANCE = new InviteLinkGeneratorImpl_Factory();
  }
}
