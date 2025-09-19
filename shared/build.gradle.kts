// 先頭に import
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
}

tasks.register("testClasses")

kotlin {
    jvmToolchain(17)

    androidTarget {
        compilations.all { kotlinOptions { jvmTarget = "17" } }
    }

    // ★ アグリゲータに名前を付ける（ここでは "Shared"）
    val xcf = XCFramework("Shared")

    // ★ iOS ターゲットを明示して framework を xcf に登録
    val iosTargets = listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    )

    iosTargets.forEach { t ->
        t.binaries.framework {
            baseName = "shared"
            isStatic = true
            xcf.add(this) // ← これが重要
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.davidepianca98:kmqtt-common:0.4.8")
                implementation("io.github.davidepianca98:kmqtt-client:0.4.8")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            }
        }
        val commonTest by getting { dependencies { implementation(kotlin("test")) } }
        val androidMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

            }
        }
    }
}

android {
    namespace = "com.example.mqtt"
    compileSdk = 34
    defaultConfig { minSdk = 28 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging {
        resources {
            excludes += "/META-INF/INDEX.LIST"
            pickFirsts += "META-INF/io.netty.versions.properties"
        }
    }
}
