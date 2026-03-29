plugins {
    kotlin("jvm") version "2.3.10"
    id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"
}

group = "moe.caa"
version = "3.0-SNAPSHOT"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")

    implementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_api_version")}")
    implementation("net.fabricmc:fabric-language-kotlin:${project.property("fabric_kotlin_version")}")
}