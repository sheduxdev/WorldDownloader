import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import xyz.wagyourtail.unimined.api.minecraft.task.AbstractRemapJarTask

plugins {
    kotlin("jvm") version "2.4.10"
    id("xyz.wagyourtail.unimined") version "1.4.1"
    id("com.gradleup.shadow") version "9.0.0"
}

val mcVersion = "1.8.9"

repositories {
    mavenCentral()
    maven("https://maven.wagyourtail.xyz/releases")
    maven("https://maven.wagyourtail.xyz/snapshots")
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

// Deps to bundle (shade) into the final jar. 1.8.9 Forge has no Kotlin at runtime.
val shade: Configuration by configurations.creating

unimined.minecraft {
    version(mcVersion)

    mappings {
        searge()
        mcp("stable", "22-1.8.9")
    }

    minecraftForge {
        loader("11.15.1.2318-1.8.9")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    shade(kotlin("stdlib"))
}

// Shade + relocate kotlin-stdlib so the jar is self-contained and can't clash with other Kotlin mods.
tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(shade)
    archiveClassifier.set("dev-shadow")
    relocate("kotlin", "dev.shedux.worlddownloader.libs.kotlin")
}

// Unimined remaps the SHADED jar (named -> searge) so production classpath resolves everything.
tasks.named<AbstractRemapJarTask>("remapJar") {
    dependsOn("shadowJar")
    inputFile.set(tasks.named<ShadowJar>("shadowJar").flatMap { it.archiveFile })
}

// 1.8.9 targets Java 8 bytecode and runs on a Java 8 JRE (Gradle daemon stays on 21).
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}
kotlin {
    jvmToolchain(8)
}
