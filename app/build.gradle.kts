plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization") version "2.1.10"
}

android {
    namespace = "com.example.weatherforecast"
    compileSdk = 35

    packagingOptions {
        resources {
            excludes += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/AL2.0"
            excludes += "META-INF/LGPL2.1"
        }
    }

    defaultConfig {
        applicationId = "com.example.weatherforecast"
        minSdk = 24
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.play.services.location)
    implementation(libs.androidx.monitor)
    implementation(libs.androidx.espresso.core)
    implementation(libs.play.services.maps)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.compose)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation (libs.gson)
    implementation (libs.androidx.room.ktx)
    implementation (libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.lifecycle.viewmodel.compose.android)
    implementation(libs.androidx.runtime.livedata)
    implementation (libs.androidx.material.icons.extended)
    implementation(libs.play.services.location.v2110)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation (libs.osmdroid.android)
    implementation (libs.android.sdk)
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.translate)
    implementation (libs.mlkit.translate.v1701)



    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.kotlinx.coroutines.test.v173)
    androidTestImplementation(libs.kotlinx.coroutines.test.v173)
    testImplementation (libs.mockk.android)
    testImplementation (libs.mockk.agent)
    androidTestImplementation (libs.mockk.android)
    androidTestImplementation (libs.mockk.agent)

}