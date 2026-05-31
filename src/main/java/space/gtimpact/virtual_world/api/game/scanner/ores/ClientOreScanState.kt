package space.gtimpact.virtual_world.api.game.scanner.ores

import space.gtimpact.virtual_world.api.core.ResourcePos
import space.gtimpact.virtual_world.api.resources.ores.OreVein

data class ClientOreScanState(
    val dimensionId: Int,
    val layer: Int,
    val veins: List<ClientOreVeinState>,
) {

    companion object {
        val EMPTY = ClientOreScanState(
            dimensionId = 0,
            layer = 0,
            veins = emptyList(),
        )
    }

    data class ClientOreVeinState(
        val vein: OreVein,
        val position: ResourcePos,
        val generatedAmount: Int,
        val minedAmount: Int,
        val remainingAmount: Int,
        val chunks: List<ClientOreChunkState>,
    )

    data class ClientOreChunkState(
        val chunkX: Int,
        val chunkZ: Int,
        val generatedAmount: Int,
        val minedAmount: Int,
        val remainingAmount: Int,
    )
}
