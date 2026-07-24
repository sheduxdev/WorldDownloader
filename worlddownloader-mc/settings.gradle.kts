rootProject.name = "worlddownloader-mc"

// Only the 3 build-proof anchors are wired as subprojects in sub-project A.
// The full ~280-row matrix lives in versions.gradle.kts; remaining rows are flipped on later.
listOf("1.8.9-forge", "1.20.1-fabric", "26.2-neoforge").forEach { id ->
    include(":$id")
    project(":$id").projectDir = file("versions/$id")
}
