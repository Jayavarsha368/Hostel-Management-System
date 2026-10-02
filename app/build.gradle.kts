plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

val googleWebClientId = providers.gradleProperty("GOOGLE_WEB_CLIENT_ID")
    .orElse(providers.environmentVariable("GOOGLE_WEB_CLIENT_ID"))
    .getOrElse("")
val apiBaseUrl = providers.gradleProperty("API_BASE_URL")
    .orElse(providers.environmentVariable("API_BASE_URL"))
    .getOrElse("http://localhost:3000/")
    .let { if (it.endsWith("/")) it else "$it/" }

android {
    namespace = "com.example.freelancerconnect"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.freelancerconnect"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"$googleWebClientId\""
        )
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    // ── AndroidX Core ────────────────────────────────────────────────────────
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.android.gms:play-services-auth:21.3.0")

    // ── Lifecycle (ViewModel + LiveData) ─────────────────────────────────────
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")

    // ── Networking: Retrofit + OkHttp + Gson ─────────────────────────────────
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ── Image Loading ─────────────────────────────────────────────────────────
    implementation("io.coil-kt:coil:2.7.0")

    // ── Testing ─────────────────────────────────────────────────────────────
    testImplementation("junit:junit:4.13.2")
}