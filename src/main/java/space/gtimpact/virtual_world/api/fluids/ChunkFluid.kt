package space.gtimpact.virtual_world.api.fluids

import space.gtimpact.virtual_world.api.TypeFluidVein

/**
 * Chunk with Virtual Fluid
 */
class ChunkFluid(
    val x: Int,
    val z: Int,
    var size: Int = 0,
    val type: TypeFluidVein
)
