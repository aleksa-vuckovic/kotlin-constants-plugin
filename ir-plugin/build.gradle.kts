import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("com.github.gmazzo.buildconfig")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.24")
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.24")

    kapt("com.google.auto.service:auto-service:1.0-rc7")
    compileOnly("com.google.auto.service:auto-service-annotations:1.0-rc7")

    testImplementation(kotlin("test-junit"))
    testImplementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.24")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.6.0")
}

buildConfig {
    packageName(group.toString())
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"${rootProject.extra["kotlin_plugin_id"]}\"")
}

tasks.test {
    testLogging {
        events(TestLogEvent.STANDARD_OUT, TestLogEvent.FAILED, TestLogEvent.PASSED)
    }
}