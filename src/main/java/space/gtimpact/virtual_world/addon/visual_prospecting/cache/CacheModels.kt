package space.gtimpact.virtual_world.addon.visual_prospecting.cache

import space.gtimpact.virtual_world.api.VirtualOreVein

data class CacheOreVeinList(
    val layer: Int, //byte (0..1)
    val veins: List<CacheOreVein>
)

data class CacheOreVein(
    val veinId: Int, //short
    val x: Int,
    val z: Int,
    val chunks: List<CacheOreVeinChunk>,
) {
    var dimension: Int = 0
    var vein: VirtualOreVein? = null
}

data class CacheOreVeinChunk(
    val x: Int,
    val z: Int,
    val size: Int, //byte (percent 0..100)
)
