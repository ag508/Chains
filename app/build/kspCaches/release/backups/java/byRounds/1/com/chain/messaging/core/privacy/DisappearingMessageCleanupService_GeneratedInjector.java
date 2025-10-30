package com.chain.messaging.core.privacy;

import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ServiceComponent;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.internal.GeneratedEntryPoint;
import javax.annotation.processing.Generated;

@OriginatingElement(
    topLevelClass = DisappearingMessageCleanupService.class
)
@GeneratedEntryPoint
@InstallIn(ServiceComponent.class)
@Generated("dagger.hilt.android.processor.internal.androidentrypoint.InjectorEntryPointGenerator")
public interface DisappearingMessageCleanupService_GeneratedInjector {
  void injectDisappearingMessageCleanupService(
      DisappearingMessageCleanupService disappearingMessageCleanupService);
}
