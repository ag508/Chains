package com.chain.messaging.core.webrtc;

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
public final class CodecManagerImpl_Factory implements Factory<CodecManagerImpl> {
  @Override
  public CodecManagerImpl get() {
    return newInstance();
  }

  public static CodecManagerImpl_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CodecManagerImpl newInstance() {
    return new CodecManagerImpl();
  }

  private static final class InstanceHolder {
    private static final CodecManagerImpl_Factory INSTANCE = new CodecManagerImpl_Factory();
  }
}
