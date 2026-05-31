package space.gtimpact.virtual_world.api.services.scanning.fluids

import space.gtimpact.virtual_world.api.core.ResourcePos
import space.gtimpact.virtual_world.api.resources.fluids.FluidVein
import space.gtimpact.virtual_world.api.services.scanning.VeinBounds

data class FluidVeinScanResult(
    val dimensionId: Int,
    val pos: ResourcePos,
    val bounds: VeinBounds,
    val fluid: FluidVein,
    val generatedAmount: Int?,
    val extractedAmount: Int?,
    val remainingAmount: Int?,
)
