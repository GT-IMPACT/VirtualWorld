package space.gtimpact.virtual_world.api.fluids

import java.util.*

/**
 * Vein with Virtual Fluid
 */
class VeinFluid @JvmOverloads constructor(
    val xVein: Int,
    val zVein: Int,
    val fluidId: Int,
    val oreChunks: ArrayList<ChunkFluid> = arrayListOf(),
)
