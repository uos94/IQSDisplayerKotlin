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

    defaultConfig {
        applicationId = "com.kct.iqsdisplayer"
        minSdk = 24
        targetSdk = 34
        versionCode = 111
        versionName = "1.0.11"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs.getByName("debug")

        manifestPlaceholders["packageName"] = "$applicationId"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.databinding.runtime)
    implementation(libs.glide)
    kapt(libs.glide.compiler)
    implementation(libs.androidx.constraintlayout)
    //ksp(libs.glide.compiler)
    implementation(files("libs/commons-net-3.6.jar"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}