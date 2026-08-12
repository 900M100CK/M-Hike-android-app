plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.m_hikeapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.m_hikeapp"
        minSdk = 26
        targetSdk = 35
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

    // ── Mapbox Maps (G1) ──────────────────────────────────────────────────
    // Note: com.mapbox.common is a transitive dependency of mapbox.maps —
    // do NOT declare it manually; it must be resolved from the Mapbox Maven
    // repo (declared in settings.gradle.kts) not Maven Central.
    implementation(libs.mapbox.maps)
    implementation(libs.play.services.location)
    implementation(libs.okhttp)
    implementation(libs.gson)

    // ── Firebase Services ──────────────────────────────────────────────────────
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")

    // ── Room Database ──────────────────────────────────────────────────────────
    implementation(libs.room.runtime)
    implementation(libs.room.common)
    annotationProcessor(libs.room.compiler)

    // ── Glide Image Loading (ImgBB / Web / Local) ──────────────────────────────
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
