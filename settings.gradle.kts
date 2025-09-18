enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
    plugins {
        id("org.jetbrains.kotlin.multiplatform") version "2.1.20"
        id("org.jetbrains.kotlin.android")       version "2.1.20"
        id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"
        id("org.jetbrains.kotlin.native.cocoapods") version "2.1.20"
        id("com.android.application")            version "8.6.1"
        id("com.android.library")                version "8.6.1"
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}


dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}

rootProject.name = "MQTT"
include(":androidApp", ":shared")
