pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.wagyourtail.xyz/releases")
        maven("https://maven.wagyourtail.xyz/snapshots")
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "worlddownloader-mc"

// The 3 build-proof anchors. Full ~200-row matrix lives in versions.gradle.kts; rest flipped on later.
listOf("1.8.9-forge").forEach { id ->
    include(":$id")
    project(":$id").projectDir = file("versions/$id")
}
