pluginManagement {
    plugins {
        id("net.neoforged.gradle.userdev") version "7.0.154"
        id("com.github.minecraftschurlimods.helperplugin") version "1.14"
    }
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven { url = uri("https://maven.neoforged.net/releases") }
        maven { url = uri("https://minecraftschurli.ddns.net/repository/maven-public") }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

rootProject.name = "PotionBundles"
