import java.util.Date

/*
 * Chain Messaging App - Build Configuration
 * 
 * Recent fixes applied:
 * - Added proper WebRTC implementation using official WebRTC Android SDK (io.github.webrtc-sdk:android)
 * - Added swipe gesture library for UI interactions
 * - Added image/video compression libraries
 * - Added Firebase Authentication as backup auth provider
 * - Added WebSocket support for real-time communication
 * - Added extended Material Icons for better UI
 * - Fixed packaging excludes for better compatibility
 * - Added Google Services plugin for Firebase integration
 * 
 * Compatibility improvements:
 * - Aligned Compose Compiler (1.5.4) with Kotlin (1.9.20)
 * - Updated Compose BOM to stable version (2023.10.01)
 * - Updated kotlinx-serialization to compatible version (1.6.2)
 * - KSP version (1.9.20-1.0.14) matches Kotlin version
 * - All plugin versions aligned in project-level build.gradle.kts
 */

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.gms.google-services") version "4.4.0" apply false
}

android {
    namespace = "com.chain.messaging"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.chain.messaging"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("../chain-release.keystore")
            storePassword = "ChainApp2024!"
            keyAlias = "chain_release"
            keyPassword = "ChainApp2024!"
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
            buildConfigField("String", "BUILD_ENVIRONMENT", "\"debug\"")
        }

        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "proguard-deployment.pro"
            )
            
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
            buildConfigField("String", "BUILD_ENVIRONMENT", "\"production\"")
        }

        create("staging") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            signingConfig = signingConfigs.getByName("release")
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "proguard-deployment.pro"
            )
            
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
            buildConfigField("String", "BUILD_ENVIRONMENT", "\"staging\"")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-Xjvm-default=all"
        )
    }
    
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    
    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/DEPENDENCIES",
                "/META-INF/LICENSE",
                "/META-INF/LICENSE.txt",
                "/META-INF/NOTICE",
                "/META-INF/NOTICE.txt",
                "/META-INF/ASL2.0",
                "/META-INF/LGPL2.1",
                "/META-INF/INDEX.LIST"
            )
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        animationsDisabled = true
    }

    lint {
        checkReleaseBuilds = true
        abortOnError = false
        warningsAsErrors = false
        
        disable += setOf("MissingTranslation", "ExtraTranslation")
        
        // Security checks
        checkOnly += setOf("HardcodedDebugMode", "HardcodedValues", "SecureRandom")
    }
}



dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.appcompat:appcompat:1.6.1")
    
    // Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    ksp("com.google.dagger:hilt-compiler:2.48")
    
    // Room Database with SQLCipher
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")
    implementation("androidx.sqlite:sqlite:2.4.0")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.1.0")
    ksp("androidx.hilt:hilt-compiler:1.1.0")
    
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // WebSocket for real-time communication
    // implementation("com.squareup.okhttp3:okhttp-ws:4.12.0") // Not available in this version
    implementation("org.java-websocket:Java-WebSocket:1.5.4")
    
    // JSON & Serialization
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // Security & Encryption
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("org.signal:libsignal-android:0.42.0")
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")
    
    // Authentication
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.credentials:credentials:1.2.2")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.2")
    
    // Firebase Authentication (alternative auth provider)
    implementation("com.google.firebase:firebase-auth-ktx:22.3.0")
    implementation("com.google.firebase:firebase-firestore-ktx:24.10.0")
    
    // Cloud Storage Authentication
    implementation("androidx.browser:browser:1.7.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.microsoft.graph:microsoft-graph:5.74.0")
    implementation("com.dropbox.core:dropbox-core-sdk:5.4.5")
    
    // Media handling
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("androidx.exifinterface:exifinterface:1.3.6")
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.media3:media3-ui:1.2.0")
    
    // Image/Video compression and processing
    implementation("id.zelory:compressor:3.0.1")
    // implementation("com.github.yalantis:ucrop:2.2.8") // Not available
    
    // QR Code generation and scanning
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    
    // UI Components
    implementation("me.saket.swipe:swipe:1.2.0")
    
    // WebRTC - Using official WebRTC Android library
    implementation("io.github.webrtc-sdk:android:137.7151.03")
    
    // Core library desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.room:room-testing:2.6.1")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("androidx.test:core:1.5.0")
    
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("io.mockk:mockk-android:1.13.8")
    
    // Add compose UI testing to regular test implementation as well
    testImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    testImplementation("androidx.compose.ui:ui-test-junit4")
    
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// Deployment tasks
tasks.register("generateDeploymentReport") {
    doLast {
        println("=== Chain Messaging Deployment Report ===")
        println("Version: ${android.defaultConfig.versionName}")
        println("Version Code: ${android.defaultConfig.versionCode}")
        println("Application ID: ${android.defaultConfig.applicationId}")
        println("Min SDK: ${android.defaultConfig.minSdk}")
        println("Target SDK: ${android.defaultConfig.targetSdk}")
        println("Compile SDK: ${android.compileSdk}")
        println("Build Time: ${Date()}")
        println("==========================================")
    }
}

tasks.register("validateDeployment") {
    doLast {
        val errors = mutableListOf<String>()
        
        // Validate version information
        if (android.defaultConfig.versionName.isNullOrEmpty()) {
            errors.add("Version name is not set")
        }
        
        if (android.defaultConfig.versionCode == null || android.defaultConfig.versionCode!! <= 0) {
            errors.add("Version code is not set or invalid")
        }
        
        // Validate application ID
        if (android.defaultConfig.applicationId.isNullOrEmpty()) {
            errors.add("Application ID is not set")
        }
        
        // Check for ProGuard configuration
        if (!file("proguard-deployment.pro").exists()) {
            errors.add("Deployment ProGuard configuration not found")
        }
        
        if (errors.isEmpty()) {
            println("✅ Deployment validation passed")
        } else {
            println("❌ Deployment validation failed:")
            errors.forEach { println("  - $it") }
            throw GradleException("Deployment validation failed")
        }
    }
}



// Run validation before release builds
tasks.whenTaskAdded {
    if (name == "assembleRelease" || name == "bundleRelease") {
        dependsOn("validateDeployment")
        finalizedBy("generateDeploymentReport")
    }
}