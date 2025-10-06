import org.gradle.kotlin.dsl.implementation
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import kotlin.apply

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            // Google Maps
            implementation(libs.play.services.maps)
            implementation(libs.maps.compose)
            implementation(libs.play.services.location)
            // Compose
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation("com.google.android.gms:play-services-location:21.0.1")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
            implementation(libs.androidx.compose.bom)
            implementation(libs.androidx.compose.ui)
            implementation(libs.androidx.compose.ui.tooling.preview)
            implementation(libs.androidx.compose.foundation)
            implementation(libs.androidx.compose.material3)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            // Coroutines on Android
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.play.services.location.v2101)
            implementation(libs.kotlinx.coroutines.play.services)
            // horizontal list library
            implementation("com.github.shsaudhrb:HorizontalList:0882e3c3a0")
            // vertical list library
            implementation("com.github.etharalrehaili4:verticallist:834045a12a")
            //Koin
            implementation("io.insert-koin:koin-android:3.5.6")
            implementation("io.insert-koin:koin-androidx-compose:3.5.6")
            // horizontal line library
            implementation("com.github.shsaudhrb:HorizontalList:0882e3c3a0")
            // Icons
            implementation(libs.androidx.material.icons.extended)
            implementation(libs.androidx.material)

        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
           // implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(projects.shared)
            implementation("androidx.navigation:navigation-compose:2.8.4")
            // Koin
            implementation(libs.koin.android)
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "org.lmd.project"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        applicationId = "org.lmd.project"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // inject the API key into the APP manifest
        val mapsKey = (project.findProperty("MAPS_API_KEY") as String?)
            ?: System.getenv("MAPS_API_KEY") ?: ""
        manifestPlaceholders["MAPS_API_KEY"] = mapsKey

        val envFile = rootProject.file(".env")
        val props =
            Properties().apply {
                if (envFile.exists()) {
                    envFile.inputStream().use { this.load(it) }
                }
            }
        val baseUrl = props.getProperty("BASE_URL") ?: error("Missing BASE_URL in .env")
        val wsBaseUrl = props.getProperty("WS_BASE_URL") ?: error("Missing WS_BASE_URL in .env")
        val supaKey = props.getProperty("SUPABASE_KEY") ?: error("Missing SUPABASE_KEY in .env")

        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        buildConfigField("String", "WS_BASE_URL", "\"$wsBaseUrl\"")
        buildConfigField("String", "SUPABASE_KEY", "\"$supaKey\"")

    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
    // Ktor (Android engine + websockets) used by SocketPlayground
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.websockets)
}

