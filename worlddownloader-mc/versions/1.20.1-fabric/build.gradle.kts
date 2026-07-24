plugins {
    kotlin("jvm") version "2.4.10"
    id("dev.architectury.loom") version "1.17.491"
}

val mc = "1.20.1"

loom { }

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
}

dependencies {
    minecraft("com.mojang:minecraft:$mc")
    mappings("net.fabricmc:yarn:$mc+build.10:v2")
    modImplementation("net.fabricmc:fabric-loader:0.16.14")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.92.2+$mc")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.13.13+kotlin.2.4.10")
}

kotlin {
    jvmToolchain(21)
}
