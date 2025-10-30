package com.chain.messaging;

import com.chain.messaging.core.integration.ChainApplicationManager;
import com.chain.messaging.core.notification.NotificationChannelManager;
import com.chain.messaging.core.performance.BatteryOptimizer;
import com.chain.messaging.core.performance.MemoryManager;
import com.chain.messaging.core.performance.PerformanceMonitor;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class ChainApplication_MembersInjector implements MembersInjector<ChainApplication> {
  private final Provider<ChainApplicationManager> applicationManagerProvider;

  private final Provider<NotificationChannelManager> notificationChannelManagerProvider;

  private final Provider<PerformanceMonitor> performanceMonitorProvider;

  private final Provider<BatteryOptimizer> batteryOptimizerProvider;

  private final Provider<MemoryManager> memoryManagerProvider;

  public ChainApplication_MembersInjector(
      Provider<ChainApplicationManager> applicationManagerProvider,
      Provider<NotificationChannelManager> notificationChannelManagerProvider,
      Provider<PerformanceMonitor> performanceMonitorProvider,
      Provider<BatteryOptimizer> batteryOptimizerProvider,
      Provider<MemoryManager> memoryManagerProvider) {
    this.applicationManagerProvider = applicationManagerProvider;
    this.notificationChannelManagerProvider = notificationChannelManagerProvider;
    this.performanceMonitorProvider = performanceMonitorProvider;
    this.batteryOptimizerProvider = batteryOptimizerProvider;
    this.memoryManagerProvider = memoryManagerProvider;
  }

  public static MembersInjector<ChainApplication> create(
      Provider<ChainApplicationManager> applicationManagerProvider,
      Provider<NotificationChannelManager> notificationChannelManagerProvider,
      Provider<PerformanceMonitor> performanceMonitorProvider,
      Provider<BatteryOptimizer> batteryOptimizerProvider,
      Provider<MemoryManager> memoryManagerProvider) {
    return new ChainApplication_MembersInjector(applicationManagerProvider, notificationChannelManagerProvider, performanceMonitorProvider, batteryOptimizerProvider, memoryManagerProvider);
  }

  @Override
  public void injectMembers(ChainApplication instance) {
    injectApplicationManager(instance, applicationManagerProvider.get());
    injectNotificationChannelManager(instance, notificationChannelManagerProvider.get());
    injectPerformanceMonitor(instance, performanceMonitorProvider.get());
    injectBatteryOptimizer(instance, batteryOptimizerProvider.get());
    injectMemoryManager(instance, memoryManagerProvider.get());
  }

  @InjectedFieldSignature("com.chain.messaging.ChainApplication.applicationManager")
  public static void injectApplicationManager(ChainApplication instance,
      ChainApplicationManager applicationManager) {
    instance.applicationManager = applicationManager;
  }

  @InjectedFieldSignature("com.chain.messaging.ChainApplication.notificationChannelManager")
  public static void injectNotificationChannelManager(ChainApplication instance,
      NotificationChannelManager notificationChannelManager) {
    instance.notificationChannelManager = notificationChannelManager;
  }

  @InjectedFieldSignature("com.chain.messaging.ChainApplication.performanceMonitor")
  public static void injectPerformanceMonitor(ChainApplication instance,
      PerformanceMonitor performanceMonitor) {
    instance.performanceMonitor = performanceMonitor;
  }

  @InjectedFieldSignature("com.chain.messaging.ChainApplication.batteryOptimizer")
  public static void injectBatteryOptimizer(ChainApplication instance,
      BatteryOptimizer batteryOptimizer) {
    instance.batteryOptimizer = batteryOptimizer;
  }

  @InjectedFieldSignature("com.chain.messaging.ChainApplication.memoryManager")
  public static void injectMemoryManager(ChainApplication instance, MemoryManager memoryManager) {
    instance.memoryManager = memoryManager;
  }
}
