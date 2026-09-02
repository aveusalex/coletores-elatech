plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "br.com.elatech.checkoutlab"
    compileSdk = 35

    defaultConfig {
        applicationId = "br.com.elatech.checkoutlab"
        minSdk = 29
        targetSdk = 33
        versionCode = 3
        versionName = "0.3.0-diagnostic"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
