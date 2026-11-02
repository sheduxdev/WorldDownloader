package dev.desco.worlddownloader.downloads

import dev.desco.worlddownloader.WorldDownloader
import dev.desco.worlddownloader.core.configs.impl.SchematicSettings
import dev.desco.worlddownloader.mixins.accessors.NBTBaseAccessor
import kotlinx.coroutines.launch
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagDouble
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraft.world.chunk.Chunk
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.File
import java.util.zip.GZIPOutputStream
import kotlin.math.floor
import kotlin.math.roundToInt

class SchematicDownloader(private val settings: SchematicSettings): Downloader {
    override val dir = SaveDestination.directory(settings.outputRoot, settings.outputFolder, "schematics")

    override fun saveChunks(chunks: Set<Chunk>) {
        WorldDownloader.launch {
            var output: File? = null
            runCatching {
                if (chunks.isEmpty()) {
                    error("No chunks were selected for schematic download.")
                }

                val selectedChunks = selectPlayerChunkGroup(chunks)
                val minX = selectedChunks.minOf { it.xPosition } * 16
                val maxX = selectedChunks.maxOf { it.xPosition } * 16 + 16
                val minZ = selectedChunks.minOf { it.zPosition } * 16
                val maxZ = selectedChunks.maxOf { it.zPosition } * 16 + 16
                val sections = selectedChunks.flatMap { it.blockStorageArray.filterNotNull() }
                val selectedMinY = sections.minOfOrNull { it.yLocation } ?: 0
                val selectedMaxY = sections.maxOfOrNull { it.yLocation + 16 } ?: 1
                val bounds = if (settings.trimAirBounds) findNonAirBounds(selectedChunks) else null
                val schematicMinX = bounds?.minX ?: minX
                val schematicMaxX = bounds?.maxX ?: maxX
                val schematicMinY = bounds?.minY ?: selectedMinY
                val schematicMaxY = bounds?.maxY ?: selectedMaxY
                val schematicMinZ = bounds?.minZ ?: minZ
                val schematicMaxZ = bounds?.maxZ ?: maxZ
                val width = schematicMaxX - schematicMinX
                val height = schematicMaxY - schematicMinY
                val length = schematicMaxZ - schematicMinZ
                require(width > 0 && height > 0 && length > 0) {
                    "Invalid schematic bounds: ${width}x${height}x${length}"
                }
                val volume = width.toLong() * height.toLong() * length.toLong()
                require(volume <= Int.MAX_VALUE) {
                    "Schematic is too large: ${width}x${height}x${length}"
                }

                val nbt = NBTTagCompound()
                nbt.setShort("Width", width.toShort())
                nbt.setShort("Height", height.toShort())
                nbt.setShort("Length", length.toShort())
                nbt.setString("Materials", "Alpha")
                if (settings.playerOrigin) {
                    val player = Minecraft.getMinecraft().thePlayer
                    val originX = player.posX.toInt()
                    val originY = player.posY.toInt()
                    val originZ = player.posZ.toInt()
                    nbt.setInteger("WEOriginX", originX)
                    nbt.setInteger("WEOriginY", originY)
                    nbt.setInteger("WEOriginZ", originZ)
                    nbt.setInteger("WEOffsetX", schematicMinX - originX)
                    nbt.setInteger("WEOffsetY", schematicMinY - originY)
                    nbt.setInteger("WEOffsetZ", schematicMinZ - originZ)
                } else {
                    nbt.setInteger("WEOriginX", schematicMinX)
                    nbt.setInteger("WEOriginY", schematicMinY)
                    nbt.setInteger("WEOriginZ", schematicMinZ)
                    nbt.setInteger("WEOffsetX", 0)
                    nbt.setInteger("WEOffsetY", 0)
                    nbt.setInteger("WEOffsetZ", 0)
                }

                val byteArray = ByteArray(volume.toInt())
                val blockMeta = ByteArray(volume.toInt())
                val chunksByPosition = selectedChunks.associateBy { it.xPosition to it.zPosition }

                // Block section
                for (y in schematicMinY until schematicMaxY) {
                    for (z in schematicMinZ until schematicMaxZ) {
                        for (x in schematicMinX until schematicMaxX) {
                            val chunk = chunksByPosition[(x shr 4) to (z shr 4)] ?: continue
                            chunk.blockStorageArray.getOrNull(y shr 4)?.let {
                                val i = x.mod(16)
                                val j = y.mod(16)
                                val k = z.mod(16)

                                val state = it.get(i, j, k)
                                val id = Block.getIdFromBlock(state.block)
                                if (settings.skipAirBlocks && id == 0) {
                                    return@let
                                }
                                val meta = state.block.getMetaFromState(state)

                                val index = ((y - schematicMinY) * length + (z - schematicMinZ)) * width + x - schematicMinX
                                byteArray[index] = (id and 0xff).toByte()
                                blockMeta[index] = (meta and 0xff).toByte()
                            }
                        }
                    }
                }

                nbt.setByteArray("Blocks", byteArray)
                nbt.setByteArray("Data", blockMeta)
                /*            if (addBlocks.isNotEmpty()) {
                                nbt.setByteArray("AddBlocks", addBlocks)
                            }*/

                // Entity section
                val entityList = NBTTagList()
                if (settings.entities) {
                    for (chunk in selectedChunks) {
                        for (list in chunk.entityLists) {
                            for (entity in list) {
                                val entityTag = NBTTagCompound()

                                if (entity.writeToNBTOptional(entityTag)) {
                                    entityList.appendTag(entityTag.apply {
                                        entityTag.getTagList("Pos", 6).apply {
                                            this.set(0, NBTTagDouble(this.getDoubleAt(0) - schematicMinX))
                                            this.set(1, NBTTagDouble(this.getDoubleAt(1) - schematicMinY))
                                            this.set(2, NBTTagDouble(this.getDoubleAt(2) - schematicMinZ))
                                        }

                                        if (settings.setVisible) {
                                            if (this.hasKey("Invisible")) { // For Armor Stands
                                                this.setBoolean("Invisible", false)
                                            }
                                            if (this.hasKey("ActiveEffects")) { // For everyone else
                                                val potions = this.getTagList("ActiveEffects", 10)
                                                for (i in 0 until potions.tagCount()) {
                                                    val effect = potions.getCompoundTagAt(i)
                                                    if (effect.getInteger("Id") == 14) {
                                                        potions.removeTag(i)
                                                        break
                                                    }
                                                }
                                            }
                                        }
                                        if (settings.invulnerable) {
                                            this.setBoolean("Invulnerable", true)
                                        }
                                        if (settings.silent) {
                                            this.setBoolean("Silent", true)
                                        }
                                    })
                                }
                            }
                        }
                    }
                }
                nbt.setTag("Entities", entityList)

                nbt.setTag("TileEntities", NBTTagList())

                val file = SaveDestination.file(dir, nameWithPlayerRotation(settings.outputName), "schematic", settings.overwriteExisting)
                output = file
                DataOutputStream(BufferedOutputStream(GZIPOutputStream(file.outputStream()))).use {
                    it.writeByte(nbt.id.toInt())
                    it.writeUTF("Schematic")
                    // Yet another excellent Mojang gambit
                    (nbt as NBTBaseAccessor).invokeWrite(it)
                }
            }.onFailure {
                Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText(
                    "${EnumChatFormatting.RED}Something went wrong trying to download chunks. Logs may include useful information."
                ))
                it.printStackTrace()
            }.onSuccess {
                Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText(
                    "${EnumChatFormatting.GREEN}Chunks have been downloaded to ${output?.path ?: dir.path}."
                ))
            }
        }
    }

    private fun nameWithPlayerRotation(name: String): String {
        val player = Minecraft.getMinecraft().thePlayer
        val baseName = name.ifBlank { SaveDestination.timestamp() }
        val yaw = player.rotationYaw.roundToInt()
        val pitch = player.rotationPitch.roundToInt()
        val y = player.posY.toInt()
        return "${baseName}_${yaw}_${pitch}_${y}"
    }

    private fun findNonAirBounds(chunks: Set<Chunk>): Bounds? {
        var minX = Int.MAX_VALUE
        var minY = Int.MAX_VALUE
        var minZ = Int.MAX_VALUE
        var maxX = Int.MIN_VALUE
        var maxY = Int.MIN_VALUE
        var maxZ = Int.MIN_VALUE

        for (chunk in chunks) {
            for (section in chunk.blockStorageArray.filterNotNull()) {
                for (y in 0 until 16) {
                    for (z in 0 until 16) {
                        for (x in 0 until 16) {
                            val state = section.get(x, y, z)
                            if (Block.getIdFromBlock(state.block) == 0) {
                                continue
                            }

                            val blockX = chunk.xPosition * 16 + x
                            val blockY = section.yLocation + y
                            val blockZ = chunk.zPosition * 16 + z
                            minX = minOf(minX, blockX)
                            minY = minOf(minY, blockY)
                            minZ = minOf(minZ, blockZ)
                            maxX = maxOf(maxX, blockX + 1)
                            maxY = maxOf(maxY, blockY + 1)
                            maxZ = maxOf(maxZ, blockZ + 1)
                        }
                    }
                }
            }
        }

        if (minX == Int.MAX_VALUE) {
            return null
        }

        return Bounds(minX, maxX, minY, maxY, minZ, maxZ)
    }

    private fun selectPlayerChunkGroup(chunks: Set<Chunk>): Set<Chunk> {
        val byPosition = chunks.associateBy { it.xPosition to it.zPosition }
        val player = Minecraft.getMinecraft().thePlayer
        val playerChunk = (floor(player.posX).toInt() shr 4) to (floor(player.posZ).toInt() shr 4)
        val start = byPosition[playerChunk] ?: return largestConnectedGroup(byPosition)
        val selected = linkedSetOf<Chunk>()
        val queue = ArrayDeque<Chunk>()
        queue.add(start)

        while (queue.isNotEmpty()) {
            val chunk = queue.removeFirst()
            if (!selected.add(chunk)) {
                continue
            }

            val x = chunk.xPosition
            val z = chunk.zPosition
            listOf(x + 1 to z, x - 1 to z, x to z + 1, x to z - 1).forEach { position ->
                byPosition[position]?.let { neighbor ->
                    if (neighbor !in selected) {
                        queue.add(neighbor)
                    }
                }
            }
        }

        return selected
    }

    private fun largestConnectedGroup(chunks: Map<Pair<Int, Int>, Chunk>): Set<Chunk> {
        val visited = mutableSetOf<Pair<Int, Int>>()
        var largest = emptySet<Chunk>()

        for (position in chunks.keys) {
            if (!visited.add(position)) {
                continue
            }

            val group = linkedSetOf<Chunk>()
            val queue = ArrayDeque<Pair<Int, Int>>()
            queue.add(position)
            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                val currentChunk = chunks[current] ?: continue
                group.add(currentChunk)

                val x = current.first
                val z = current.second
                listOf(x + 1 to z, x - 1 to z, x to z + 1, x to z - 1).forEach { neighbor ->
                    if (neighbor in chunks && visited.add(neighbor)) {
                        queue.add(neighbor)
                    }
                }
            }

            if (group.size > largest.size) {
                largest = group
            }
        }

        return largest.ifEmpty { chunks.values.toSet() }
    }

    private data class Bounds(
        val minX: Int,
        val maxX: Int,
        val minY: Int,
        val maxY: Int,
        val minZ: Int,
        val maxZ: Int,
    )
}
