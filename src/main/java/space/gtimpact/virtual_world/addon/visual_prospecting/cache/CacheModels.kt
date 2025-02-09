package space.gtimpact.virtual_world.addon.visual_prospecting.cache

import journeymap.client.model.BlockCoordIntPair
import net.minecraft.item.ItemStack
import space.gtimpact.virtual_world.api.VirtualFluidVein
import space.gtimpact.virtual_world.api.VirtualOreVein

enum class PutDataStatus {
    NEW, UPDATE, ERROR
}

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

data class CacheFluidVein(
    val veinId: Int, //short
    val x: Int,
    val z: Int,
    val chunks: List<CacheFluidVeinChunk>,
) {
    var dimension: Int = 0
    var vein: VirtualFluidVein? = null
}

data class CacheFluidVeinChunk(
    val x: Int,
    val z: Int,
    val size: Int, //byte (percent 0..100)
)

data class CacheObjectPoint(
    val elements: List<ObjectElement> = emptyList(),
    val coords: BlockCoordIntPair,
    val dimId: Int,
) {
    data class ObjectElement(
        val name: String,
        val stack: ItemStack,
    )
}
