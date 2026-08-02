plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.m_hikeapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.m_hikeapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // ── Maps & Location (G1) ──────────────────────────────────────────────────
    implementation(libs.osmdroid)
    implementation(libs.play.services.location)

    // ── Firebase Services ──────────────────────────────────────────────────────
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")

    // ── Room Database ──────────────────────────────────────────────────────────
    // room-runtime  : core Room library (Entity, Dao, Database annotations)
    // room-compiler : annotation processor — generates DAO implementations and
    //                 validates all @Query SQL strings at COMPILE TIME.
    implementation(libs.room.runtime)
    implementation(libs.room.common)
    annotationProcessor(libs.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}