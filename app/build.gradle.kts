plugins {
    // Kotlin est compilé nativement par AGP 9 (built-in Kotlin).
    id("com.android.application")
}

android {
    namespace = "com.example.currencyconverter"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.currencyconverter"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        // Obligatoire pour utiliser ActivityMainBinding au lieu de findViewById.
        viewBinding = true
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
}

dependencies {
    // ConstraintLayout est utilisé pour la structure de l'écran et du clavier.
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
}
