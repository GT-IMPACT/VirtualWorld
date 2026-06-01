package space.gtimpact.virtual_world.api.services.scanning.ores

import space.gtimpact.virtual_world.api.core.WorldPos
import space.gtimpact.virtual_world.api.services.scanning.ScanMode

data class OreScanReport(
    val dimensionId: Int,
    val centerBlockPos: WorldPos,
    val radiusVeins: Int,
    val layerIndex: Int,
    val mode: ScanMode,
    val results: List<OreVeinScanResult>,
)
