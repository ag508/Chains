package com.chain.messaging.di;

import com.chain.messaging.core.webrtc.BandwidthMonitor;
import com.chain.messaging.core.webrtc.CallQualityManager;
import com.chain.messaging.core.webrtc.CallRecordingManager;
import com.chain.messaging.core.webrtc.CodecManager;
import com.chain.messaging.core.webrtc.ScreenshotDetector;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class WebRTCModule_ProvideCallQualityManagerFactory implements Factory<CallQualityManager> {
  private final Provider<BandwidthMonitor> bandwidthMonitorProvider;

  private final Provider<CodecManager> codecManagerProvider;

  private final Provider<CallRecordingManager> callRecordingManagerProvider;

  private final Provider<ScreenshotDetector> screenshotDetectorProvider;

  public WebRTCModule_ProvideCallQualityManagerFactory(
      Provider<BandwidthMonitor> bandwidthMonitorProvider,
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
    return provideCallQualityManager(bandwidthMonitorProvider.get(), codecManagerProvider.get(), callRecordingManagerProvider.get(), screenshotDetectorProvider.get());
  }

  public static WebRTCModule_ProvideCallQualityManagerFactory create(
      Provider<BandwidthMonitor> bandwidthMonitorProvider,
      Provider<CodecManager> codecManagerProvider,
      Provider<CallRecordingManager> callRecordingManagerProvider,
      Provider<ScreenshotDetector> screenshotDetectorProvider) {
    return new WebRTCModule_ProvideCallQualityManagerFactory(bandwidthMonitorProvider, codecManagerProvider, callRecordingManagerProvider, screenshotDetectorProvider);
  }

  public static CallQualityManager provideCallQualityManager(BandwidthMonitor bandwidthMonitor,
      CodecManager codecManager, CallRecordingManager callRecordingManager,
      ScreenshotDetector screenshotDetector) {
    return Preconditions.checkNotNullFromProvides(WebRTCModule.INSTANCE.provideCallQualityManager(bandwidthMonitor, codecManager, callRecordingManager, screenshotDetector));
  }
}
