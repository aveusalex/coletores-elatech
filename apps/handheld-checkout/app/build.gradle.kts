plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "br.com.elatech.checkoutlab"
    compileSdk = 35

    defaultConfig {
        applicationId = "br.com.elatech.checkoutlab"
        minSdk = 29
        targetSdk = 33
        versionCode = 11
        versionName = "0.10.0"
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

    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    testImplementation("junit:junit:4.13.2")
}
