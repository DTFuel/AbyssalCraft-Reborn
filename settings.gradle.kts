rootProject.name = "abyssalcraft"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.architectury.dev")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.6"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

stonecutter {
    kotlinController = true
    create(rootProject) {
        // (project id, logical version) MUST be separate strings
        version("1.20.1-forge", "1.20.1")
        version("1.21.1-neoforge", "1.21.1")
        vcsVersion = "1.21.1-neoforge"
    }
}
