package space.gtimpact.virtual_world.api

import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidStack

/**
 * Type for Virtual Ore
 */
typealias VirtualOreTypeComponent = ItemStack

/**
 * Type for Special Drilling Fluid (for mining Virtual Ore)
 */
typealias VirtualSpecialTypeComponent = FluidStack

/**
 * Virtual Ore Instance
 *
 * @param id ID of ore
 * @param layer layer of mining
 * @param name name of ore
 * @param weight max weight by generation on the world
 * @param rangeSize ore quantity range
 * @param color color of vein
 * @param dimensions dimensions in which there is ore
 * @param ores components of ore vein
 * @param special special drilling component
 */
data class VirtualOreVein(
    val id: Int,
    val layer: Int,
    val name: String,
    var weight: Double,
    val rangeSize: IntRange,
    val color: Int,
    val dimensions: List<Int>,
    val ores: List<VirtualOreComponent>,
    val special: VirtualSpecialTypeComponent? = null,
    val isHidden: Boolean = false,
) {
    init {
        VirtualAPI.registerVirtualOre(this)
    }

    val maxWeight: Double = weight + 0.5
    fun reduceWeight() {
        weight -= 2.5
        if (weight < 0.0) {
            weight = 0.0
        }
    }

    fun increaseWeight() {
        weight += 0.5
        if (weight > maxWeight) {
            weight = maxWeight
        }
    }
}

/**
 * Vein Component for Virtual Ore
 *
 * @param ore type for virtual ore
 * @param chance chance for mining
 */
data class VirtualOreComponent(
    val ore: VirtualOreTypeComponent,
    val chance: Int,
)