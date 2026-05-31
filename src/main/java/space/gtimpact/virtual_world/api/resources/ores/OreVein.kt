package space.gtimpact.virtual_world.api.resources.ores

import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidStack
import space.gtimpact.virtual_world.api.core.DimensionGen

data class OreVein(
    val id: Int,
    val layer: Int,
    val name: String,
    val weight: Double,
    val rangeSize: IntRange,
    val color: Int,
    val dimensions: Set<DimensionGen>,
    val ores: List<OreVeinOut>,
    val special: FluidStack? = null,
    val isHidden: Boolean = false,
) {

    data class OreVeinOut(
        val stack: ItemStack,
        val chance: Int,
    )

    val dimensionIdOnly: Set<Int>
        get() = dimensions.map { it.id }.toSet()

    fun canGenerateIn(
        dimensionId: Int,
        layerIndex: Int
    ): Boolean {
        if (isHidden) return false

        val dimensionIds = dimensionIdOnly
        val dimensionAllowed =
            dimensionIds.isEmpty() || dimensionId in dimensionIds

        val layerAllowed = layerIndex == layer

        return dimensionAllowed && layerAllowed
    }
}
