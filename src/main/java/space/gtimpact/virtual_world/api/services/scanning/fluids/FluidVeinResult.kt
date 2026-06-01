package space.gtimpact.virtual_world.api.services.scanning.fluids

import space.gtimpact.virtual_world.api.resources.fluids.FluidVein
import space.gtimpact.virtual_world.api.resources.fluids.FluidVeinResource

data class FluidVeinResult(
    val vein: FluidVeinResource,
    val fluid: FluidVein,
    val generatedVolume: Int,
    val extractedVolume: Int,
    val remainingVolume: Int,
)
