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
        create("api25") {
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storeFile = file("C:\\Users\\uos94\\AndroidStudioProjects\\IQSDisplayerKotlin\\Signed\\platform.jks")
        }
        create("api30") {
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storeFile = file("C:\\Users\\uos94\\AndroidStudioProjects\\IQSDisplayerKotlin\\Signed\\platform_os11.jks")
        }
    }

    namespace = "com.kct.iqsdisplayer"
    compileSdk = 34

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    flavorDimensions += "apiType"
    productFlavors {
        create("api30") {
            dimension = "apiType"
            //signingConfig = signingConfigs.getByName("api30")
        }
        create("api25") {
            dimension = "apiType"
            //signingConfig = signingConfigs.getByName("api25")
        }
    }

    val timestamp = SimpleDateFormat("yyMMdd_HHmm").format(Date())
    defaultConfig {
        applicationId = "com.kct.iqsdisplayer"
        minSdk = 25
        targetSdk = 34
        versionCode = 113
        versionName = "1.0.13"

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
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("api30")
        }
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("api30")
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
    //implementation(files("libs/commons-net-3.6.jar"))
}

