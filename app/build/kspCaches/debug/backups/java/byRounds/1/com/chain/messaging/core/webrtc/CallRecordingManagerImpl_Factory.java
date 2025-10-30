package com.chain.messaging.core.webrtc;

import android.content.Context;
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
public final class CallRecordingManagerImpl_Factory implements Factory<CallRecordingManagerImpl> {
  private final Provider<Context> contextProvider;

  public CallRecordingManagerImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public CallRecordingManagerImpl get() {
    return newInstance(contextProvider.get());
  }

  public static CallRecordingManagerImpl_Factory create(Provider<Context> contextProvider) {
    return new CallRecordingManagerImpl_Factory(contextProvider);
  }

  public static CallRecordingManagerImpl newInstance(Context context) {
    return new CallRecordingManagerImpl(context);
  }
}
