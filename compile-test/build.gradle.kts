import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}
val pluginName = "ir-plugin"
val plugin = project(":${pluginName}")
dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    //compileOnly(plugin)
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.24")
}

tasks.withType<KotlinCompile> {
    dependsOn(":${pluginName}:assemble")
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-Xplugin=${plugin.layout.buildDirectory.get()}/libs/${pluginName}-${plugin.version}.jar"
        )
    }
}