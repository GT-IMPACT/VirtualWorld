package space.gtimpact.virtual_world.api.ores

/**
 * Vein with Virtual Ore
 */
data class VeinOre @JvmOverloads constructor(
    val xVein: Int,
    val zVein: Int,
    val oreId: Int,
    val oreChunks: ArrayList<ChunkOre> = ArrayList(),
)
