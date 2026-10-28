plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.sustainiq.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sustainiq.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "2.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    // Room
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    // Utilities
    implementation(libs.gson)
    implementation(libs.circleimageview)
    implementation(libs.lottie)
    implementation(libs.viewpager2)
    implementation(libs.fragment)
    implementation(libs.cardview)
    implementation(libs.recyclerview)
    implementation(libs.swiperefreshlayout)
    implementation(libs.preference)
    // Network (PIN code lookup)
    implementation(libs.okhttp)
    // Image loading
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
    // Charts
    implementation(libs.mpandroidchart)
    // ML Kit Image Labeling (On-Device Vision)
    implementation(libs.mlkit.image.labeling)
    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}