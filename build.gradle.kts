buildscript {
    extra["kotlin_plugin_id"] = "com.aleksa.constants.ir-plugin"
}

plugins {
    kotlin("jvm") version "1.9.24" apply false
    id("org.jetbrains.dokka") version "+" apply false
    id("com.gradle.plugin-publish") version "+" apply false
    id("com.github.gmazzo.buildconfig") version "+" apply false
}

allprojects {
    group = "com.aleksa.constants"
    version = "0.1.0"
}

subprojects {
    repositories {
        mavenCentral()
    }
}