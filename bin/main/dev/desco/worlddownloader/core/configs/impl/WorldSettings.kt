package dev.desco.worlddownloader.core.configs.impl

import dev.desco.worlddownloader.core.configs.Configuration

class WorldSettings: Configuration() {

    private val output = category("Output")
    val outputRoot by textSetting("Main folder", "worlddownloads", "worlddownloads")
    val outputFolder by textSetting("Subfolder", "", "projects/spawn")
    val outputName by textSetting("World name", "", "leave empty for date")
    val overwriteExisting by setting("Overwrite if name exists", false)
    val clearExisting by setting("Clear existing folder before saving", false)

    private val general = category("World")
    val scoreboard by setting("Save Scoreboard", false)
    val dayTime by setting("Save day time", true)
    val weather by setting("Save weather", true)
    val blockUpdates by setting("Remove block updates", false)
    val pauseRandomTicks by setting("Pause random ticks, snow and ice", false)

    private val player = category("Player")
    val spectator by setting("Set player to Spectator", true)
    val attributes by setting("Save player attributes", false)
    val inventory by setting("Save player inventory", false)
    val potions by setting("Save player potion effects", false)

    private val entitiesCategory = category("Entities")
    val entities by setting("Save entities", true)
    val setVisible by setting("Make entities visible", false)
    val invulnerable by setting("Make entities invulnerable", false)
    val silent by setting("Make entities silent", false)
    val tickEntities by setting("Tick entities", true)
}
