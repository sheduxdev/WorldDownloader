package dev.shedux.worlddownloader.core

import net.minecraft.util.BlockPos
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

enum class RegionMode { AREA_2D, VOLUME_3D }

object RegionSelection {
    var pos1: BlockPos? = null
    var pos2: BlockPos? = null
    var mode = RegionMode.AREA_2D

    fun ready() = pos1 != null && pos2 != null

    fun yMin() = if (mode == RegionMode.AREA_2D) 0 else min(pos1!!.y, pos2!!.y)
    fun yMax() = if (mode == RegionMode.AREA_2D) 255 else max(pos1!!.y, pos2!!.y)

    fun sizeX() = abs(pos1!!.x - pos2!!.x) + 1
    fun sizeZ() = abs(pos1!!.z - pos2!!.z) + 1
    fun sizeY() = yMax() - yMin() + 1

    fun chunkCount(): Int {
        val cx = (max(pos1!!.x, pos2!!.x) shr 4) - (min(pos1!!.x, pos2!!.x) shr 4) + 1
        val cz = (max(pos1!!.z, pos2!!.z) shr 4) - (min(pos1!!.z, pos2!!.z) shr 4) + 1
        return cx * cz
    }
}
