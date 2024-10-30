import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.orgJetbrainsKotlinKapt)
    //alias(libs.plugins.ksp)
}

android {
    signingConfigs {
        getByName("debug") {
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storeFile = file("C:\\Users\\uos94\\AndroidStudioProjects\\IQSDisplayerKotlin\\Signed\\platform.jks")
        }
        create("release") {
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storeFile = file("C:\\Users\\uos94\\AndroidStudioProjects\\IQSDisplayerKotlin\\Signed\\platform.jks")
        }
    }

    namespace = "com.kct.iqsdisplayer"
    compileSdk = 34

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    val timestamp = SimpleDateFormat("yyMMdd_HHmm").format(Date())
    defaultConfig {
        applicationId = "com.kct.iqsdisplayer"
        minSdk = 24
        targetSdk = 34
        versionCode = 115
        versionName = "1.0.15"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs.getByName("debug")
        setProperty("archivesBaseName", "iqsdisplayer_v$versionName($versionCode)_$timestamp")
        //manifestPlaceholders["packageName"] = "$applicationId"
        ndk {
            abiFilters.add("armeabi-v7a") //API 25는 armeabi-v7a만 있으면 된다. 빌드 속도 최적화
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.databinding.runtime)
    implementation(libs.glide)
    //implementation(libs.lottie)
    implementation(libs.androidx.core.animation)
    kapt(libs.glide.compiler)
    implementation(libs.androidx.constraintlayout)
    //ksp(libs.glide.compiler)
    implementation(files("libs/commons-net-3.6.jar"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}