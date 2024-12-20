pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases")
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.5"
    id("fabric-loom") version "1.9-SNAPSHOT" apply false
    id("me.modmuss50.mod-publish-plugin") version "0.8.1" apply false
    kotlin("jvm") version "2.1.0" apply false
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    create(projects = listOf("ai-npc-client", "ai-npc-launcher")) {
        versions("1.20.4", "1.21.1", "1.21.3")
        vcsVersion = "1.20.4"
    }
}

rootProject.name = "ai-npc"
