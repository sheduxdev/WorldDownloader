package dev.desco.worlddownloader.core.configs.impl

import dev.desco.worlddownloader.core.configs.Configuration

class SchematicSettings: Configuration() {

    val outputRoot = "schematics"
    private var selectedPreset = SchematicPresetStore.defaultName()

    private val output = category("Schematic File")
    var outputFolder by textSetting("Subfolder", "", "projects/spawn") {
        SchematicPresetStore.ensureFolder(it)
    }
    var outputName by textSetting("Schematic name", "", "leave empty for date")
    val overwriteExisting by setting("Overwrite if name exists", false)

    private val presets = category("Presets")
    var presetName by presetSelector("Selected preset", selectedPreset, { SchematicPresetStore.listNames() }) {
        selectedPreset = it
    }
    var newPresetName by textSetting("New preset name", "", "leave empty to update selected")
    private val loadPreset = actionButton("Load selected") {
        loadPreset(presetName)
    }
    private val savePreset = actionButton("Save / update preset") {
        val target = newPresetName.ifBlank { presetName }
        SchematicPresetStore.save(target, outputFolder, outputName)
        presetName = target
        selectedPreset = target
        SchematicPresetStore.ensureFolder(outputFolder)
    }
    private val defaultPreset = actionButton("Set as default") {
        SchematicPresetStore.setDefault(presetName)
    }
    private val deletePreset = actionButton("Delete selected") {
        SchematicPresetStore.delete(presetName)
        presetName = SchematicPresetStore.defaultName()
    }

    private val origin = category("Placement")
    val playerOrigin by setting("Use player position as schematic origin", true)

    private val optimization = category("Optimization")
    val trimAirBounds by setting("Trim empty air edges", true)
    val skipAirBlocks by setting("Skip writing air blocks", true)

    private val worldCategory = category("Entities")
    var entities by setting("Save entities", true)
    var setVisible by setting("Make entities visible", false)
    var invulnerable by setting("Make entities invulnerable", false)
    var silent by setting("Make entities silent", false)

    private fun loadPreset(name: String) {
        SchematicPresetStore.load(name)?.let {
            outputFolder = it.subfolder
            outputName = it.schematicName
            SchematicPresetStore.ensureFolder(outputFolder)
        }
    }
}
