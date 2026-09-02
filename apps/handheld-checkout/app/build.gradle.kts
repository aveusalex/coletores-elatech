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
        versionCode = 8
        versionName = "0.7.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    // XCScanner SDK — aar vendorado. Procedência e checksum: app/libs/PROVENANCE.md
    implementation(files("libs/xcscanner_qrcode_v1.3.56.1.14-release.aar"))

    testImplementation("junit:junit:4.13.2")
}
