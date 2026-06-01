package space.gtimpact.virtual_world.api.services.scanning.ores

import space.gtimpact.virtual_world.api.core.ResourcePos
import space.gtimpact.virtual_world.api.resources.ores.OreVein
import space.gtimpact.virtual_world.api.services.scanning.VeinBounds

data class OreVeinScanResult(
    val dimensionId: Int,
    val pos: ResourcePos,
    val bounds: VeinBounds,
    val layerIndex: Int,
    val ore: OreVein,
    val generatedAmount: Int?,
    val minedAmount: Int?,
    val remainingAmount: Int?,
    val chunks: List<OreChunkResult> = emptyList(),
)
