package space.gtimpact.virtual_world.api.services.mining.fluids

import space.gtimpact.virtual_world.api.core.ResourcePos
import space.gtimpact.virtual_world.api.resources.fluids.FluidVein

data class FluidExtractionResult(
    val fluid: FluidVein,
    val pos: ResourcePos,
    val requestedVolume: Int,
    val extractedVolume: Int,
    val remainingVolume: Int
)
