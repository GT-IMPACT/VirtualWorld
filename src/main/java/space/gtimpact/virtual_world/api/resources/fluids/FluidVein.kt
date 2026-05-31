package space.gtimpact.virtual_world.api.resources.fluids

import net.minecraftforge.fluids.FluidStack
import space.gtimpact.virtual_world.api.core.DimensionGen

data class FluidVein(
    val id: Int,
    val name: String,
    val weight: Double,
    val rangeSize: IntRange,
    val color: Int,
    val dimensions: Set<DimensionGen>,
    val fluid: FluidStack?,
    val isHidden: Boolean = false,
) {

    val dimensionIdOnly: Set<Int>
        get() = dimensions.map { it.id }.toSet()

    fun canGenerateIn(
        dimensionId: Int,
    ): Boolean {
        return dimensionIdOnly.isEmpty() || dimensionId in dimensionIdOnly && !isHidden
    }
}
