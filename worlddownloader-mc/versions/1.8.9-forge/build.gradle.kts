plugins {
    kotlin("jvm") version "2.4.10"
    id("xyz.wagyourtail.unimined") version "1.4.1"
}

val mcVersion = "1.8.9"

repositories {
    mavenCentral()
    maven("https://maven.wagyourtail.xyz/releases")
    maven("https://maven.wagyourtail.xyz/snapshots")
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

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
    // ponytail: kotlin-stdlib on the dev classpath for runClient; shade it into the release jar later (com.gradleup.shadow).
    implementation(kotlin("stdlib"))
}

// 1.8.9 targets Java 8 bytecode and runs on a Java 8 JRE, even though the Gradle daemon is on 21.
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}
kotlin {
    jvmToolchain(8)
}
