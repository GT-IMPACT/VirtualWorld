package space.gtimpact.virtual_world.api.services.scanning.ores

import space.gtimpact.virtual_world.api.resources.ores.OreChunkDeposit
import space.gtimpact.virtual_world.api.resources.ores.OreVein
import space.gtimpact.virtual_world.api.resources.ores.OreVeinLayer
import space.gtimpact.virtual_world.api.resources.ores.OreVeinResource

data class OreChunkResult(
    val ore: OreVein,
    val vein: OreVeinResource,
    val layer: OreVeinLayer,
    val deposit: OreChunkDeposit,
    val generatedAmount: Int,
    val minedAmount: Int,
    val remainingAmount: Int,
)
