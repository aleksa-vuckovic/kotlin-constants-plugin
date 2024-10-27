import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    application
}
application {
    mainClass.set("org.example.MainKt")
}
val pluginName = "ir-plugin"
val plugin = project(":${pluginName}")
val pluginJar = "${plugin.layout.buildDirectory.get()}/libs/${pluginName}-${plugin.version}.jar"
dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.24")
    //compileOnly(plugin)
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.24")
}

tasks.withType<KotlinCompile> {
    dependsOn(":${pluginName}:assemble")
    inputs.file(pluginJar)
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-Xplugin=${pluginJar}"
        )
    }
}