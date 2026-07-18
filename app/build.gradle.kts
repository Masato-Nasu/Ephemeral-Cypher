plugins {
    id("com.android.application")
}

android {
    namespace = "com.masatonasu.ephemeralcypher"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.masatonasu.ephemeralcypher"
        minSdk = 26
        targetSdk = 35
        versionCode = 9
        versionName = "0.1.7"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
