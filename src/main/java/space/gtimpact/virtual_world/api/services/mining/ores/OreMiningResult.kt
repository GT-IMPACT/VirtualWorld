package space.gtimpact.virtual_world.api.services.mining.ores

import space.gtimpact.virtual_world.api.core.ChunkPos
import space.gtimpact.virtual_world.api.resources.ores.OreVein

data class OreMiningResult(
    val ore: OreVein,
    val chunkPos: ChunkPos,
    val layerIndex: Int,
    val requestedAmount: Int,
    val minedAmount: Int,
    val remainingAmount: Int,
)
