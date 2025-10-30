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
public final class CallStateMachine_Factory implements Factory<CallStateMachine> {
  @Override
  public CallStateMachine get() {
    return newInstance();
  }

  public static CallStateMachine_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CallStateMachine newInstance() {
    return new CallStateMachine();
  }

  private static final class InstanceHolder {
    private static final CallStateMachine_Factory INSTANCE = new CallStateMachine_Factory();
  }
}
