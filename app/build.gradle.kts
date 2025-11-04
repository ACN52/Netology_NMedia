plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "ru.netology.nmedia"
    compileSdk = 36

    defaultConfig {
        applicationId = "ru.netology.nmedia"
        minSdk = 24    // Android 7.0
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures.viewBinding = true

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["usesCleartextTraffic"] = false
            buildConfigField("String", "BASE_URL", "\"https://netomedia.ru\"")
        }
        debug {
            manifestPlaceholders["usesCleartextTraffic"] = true
            buildConfigField("String", "BASE_URL", "\"https://netomedia.ru\"")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.gson)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    //implementation(libs.androidx.room)
    implementation(libs.generativeai)
    implementation(libs.cronet.embedded)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.credentials)
    implementation(libs.play.services.fido)
    //ksp(libs.androidx.room.compiler)
    implementation(platform(libs.firebase))
    implementation(libs.firebase.messaging)
    implementation(libs.play.services)
    coreLibraryDesugaring(libs.desugaring)
    implementation(libs.okhttp)
    implementation(libs.glide)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.bundles.lifecycle)
    implementation(libs.fragment.ktx)
    implementation(libs.bundles.navigation)
    implementation(libs.bundles.navigation)
    //implementation(libs.bundles.hilt)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Paging
    implementation(libs.paging.runtime)
    implementation(libs.paging.common)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    ksp(libs.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}