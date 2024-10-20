plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "Constants"
include("ir-plugin")
include("ir-plugin-gradle")
include("ir-plugin-native")
include("compile-test")
