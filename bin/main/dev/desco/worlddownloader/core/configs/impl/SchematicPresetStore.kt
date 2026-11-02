package dev.desco.worlddownloader.core.configs.impl

import dev.desco.worlddownloader.downloads.SaveDestination
import java.io.File
import java.util.Properties

object SchematicPresetStore {
    private val root = File("schematics")
    private val presetDir = root.resolve("presets")
    private val defaultPreset = presetDir.resolve("_default.txt")

    init {
        root.mkdirs()
        presetDir.mkdirs()
    }

    fun ensureFolder(subfolder: String) {
        SaveDestination.directory(root.path, subfolder, "schematics")
    }

    fun listNames(): List<String> {
        presetDir.mkdirs()
        return presetDir.listFiles { file -> file.extension == "properties" }
            ?.map { it.nameWithoutExtension }
            ?.sorted()
            ?.ifEmpty { listOf("default") }
            ?: listOf("default")
    }

    fun defaultName(): String {
        presetDir.mkdirs()
        return defaultPreset.takeIf { it.exists() }?.readText()?.trim()?.ifBlank { null } ?: "default"
    }

    fun setDefault(name: String) {
        presetDir.mkdirs()
        defaultPreset.writeText(SaveDestination.cleanSegment(name.ifBlank { "default" }, "default"))
    }

    fun save(name: String, subfolder: String, schematicName: String) {
        val file = presetFile(name)
        Properties().apply {
            setProperty("subfolder", subfolder)
            setProperty("schematicName", schematicName)
            file.outputStream().use { store(it, "WorldDownloader schematic preset") }
        }
    }

    fun load(name: String): Preset? {
        val file = presetFile(name)
        if (!file.exists()) return null
        return Properties().run {
            file.inputStream().use { load(it) }
            Preset(
                getProperty("subfolder", ""),
                getProperty("schematicName", "")
            )
        }
    }

    fun delete(name: String): Boolean = presetFile(name).delete()

    private fun presetFile(name: String): File {
        presetDir.mkdirs()
        val cleanName = SaveDestination.cleanSegment(name.ifBlank { "default" }, "default")
        return presetDir.resolve("$cleanName.properties")
    }

    data class Preset(val subfolder: String, val schematicName: String)
}
