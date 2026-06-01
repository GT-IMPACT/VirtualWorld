package space.gtimpact.virtual_world.addon.visual_prospecting.layer.ores

import com.sinthoras.visualprospecting.integration.model.locations.ILocationProvider

class OresLocation(
    val dimension: Int,

    val chunkX: Int,
    val chunkZ: Int,

    val id: Int,

    val layer: Int,
    val color: Int,
    val name: String,

    val remainingAmount: Int,
    val generatedAmount: Int,
    val maxGeneratedSize: Int,

    val isVeinOrigin: Boolean,
) : ILocationProvider {

    override fun getDimensionId() = dimension

    override fun getBlockX(): Double {
        return chunkX * 16.0 + 8.0
    }

    override fun getBlockZ(): Double {
        return chunkZ * 16.0 + 8.0
    }
}
