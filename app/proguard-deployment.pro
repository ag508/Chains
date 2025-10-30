# Chain Messaging - Deployment ProGuard Configuration

# Keep application class
-keep class com.chain.messaging.ChainApplication { *; }

# Keep all model classes for serialization
-keep class com.chain.messaging.domain.model.** { *; }
-keep class com.chain.messaging.data.local.entity.** { *; }

# Keep encryption and security classes
-keep class com.chain.messaging.core.crypto.** { *; }
-keep class com.chain.messaging.core.security.** { *; }

# Keep blockchain and P2P classes
-keep class com.chain.messaging.core.blockchain.** { *; }
-keep class com.chain.messaging.core.p2p.** { *; }

# Keep WebRTC classes
-keep class com.chain.messaging.core.webrtc.** { *; }
-keep class org.webrtc.** { *; }

# Keep Signal Protocol classes
-keep class org.signal.** { *; }
-keep class org.whispersystems.** { *; }

# Keep Room database classes
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# Keep Hilt/Dagger classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# Keep Retrofit and networking classes
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class com.squareup.okhttp3.** { *; }

# Keep Gson/JSON serialization
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep Jetpack Compose
-keep class androidx.compose.** { *; }
-keep class kotlin.Metadata { *; }

# Remove debug logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# Remove debug logging from custom logger
-assumenosideeffects class com.chain.messaging.core.util.Logger {
    public void v(...);
    public void d(...);
}

# Optimization settings
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotations
-keepattributes *Annotation*

# Keep generic signatures
-keepattributes Signature

# Keep inner classes
-keepattributes InnerClasses,EnclosingMethod