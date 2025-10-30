package com.chain.messaging.core.webrtc;

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
public final class CallQualityManager_Factory implements Factory<CallQualityManager> {
  private final Provider<BandwidthMonitor> bandwidthMonitorProvider;

  private final Provider<CodecManager> codecManagerProvider;

  private final Provider<CallRecordingManager> callRecordingManagerProvider;

  private final Provider<ScreenshotDetector> screenshotDetectorProvider;

  public CallQualityManager_Factory(Provider<BandwidthMonitor> bandwidthMonitorProvider,
      Provider<CodecManager> codecManagerProvider,
      Provider<CallRecordingManager> callRecordingManagerProvider,
      Provider<ScreenshotDetector> screenshotDetectorProvider) {
    this.bandwidthMonitorProvider = bandwidthMonitorProvider;
    this.codecManagerProvider = codecManagerProvider;
    this.callRecordingManagerProvider = callRecordingManagerProvider;
    this.screenshotDetectorProvider = screenshotDetectorProvider;
  }

  @Override
  public CallQualityManager get() {
    return newInstance(bandwidthMonitorProvider.get(), codecManagerProvider.get(), callRecordingManagerProvider.get(), screenshotDetectorProvider.get());
  }

  public static CallQualityManager_Factory create(
      Provider<BandwidthMonitor> bandwidthMonitorProvider,
      Provider<CodecManager> codecManagerProvider,
      Provider<CallRecordingManager> callRecordingManagerProvider,
      Provider<ScreenshotDetector> screenshotDetectorProvider) {
    return new CallQualityManager_Factory(bandwidthMonitorProvider, codecManagerProvider, callRecordingManagerProvider, screenshotDetectorProvider);
  }

  public static CallQualityManager newInstance(BandwidthMonitor bandwidthMonitor,
      CodecManager codecManager, CallRecordingManager callRecordingManager,
      ScreenshotDetector screenshotDetector) {
    return new CallQualityManager(bandwidthMonitor, codecManager, callRecordingManager, screenshotDetector);
  }
}
