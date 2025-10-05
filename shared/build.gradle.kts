import org.gradle.kotlin.dsl.implementation
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20"
    id("com.codingfeline.buildkonfig") version "0.15.1"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.auth)
                implementation(libs.ktor.client.logging)

                // Multiplatform dependencies
                implementation(libs.ktor.client.websockets)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.koin.core)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation("androidx.security:security-crypto:1.1.0-alpha06")
                implementation(libs.koin.android)
                // Google Maps
                implementation(libs.play.services.maps)
                implementation(libs.maps.compose)
                implementation(libs.play.services.location)
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

buildkonfig {
    packageName = "org.example.project"

    defaultConfigs {
        buildConfigField(STRING, "BASE_URL", "https://kgomwyksxjqtcjwlzbsp.supabase.co/functions/v1/")
        buildConfigField(STRING, "WS_BASE_URL", "wss://kgomwyksxjqtcjwlzbsp.supabase.co/realtime/v1/websocket")
        buildConfigField(STRING, "SUPABASE_KEY", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imtnb213eWtzeGpxdGNqd2x6YnNwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTU3ODQ0NTEsImV4cCI6MjA3MTM2MDQ1MX0.g0JTJ4fftJum44D3gDJHwnoXK0XBLmWnsRbQcSVO5zs")
    }
}

android {
    namespace = "org.example.project.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
