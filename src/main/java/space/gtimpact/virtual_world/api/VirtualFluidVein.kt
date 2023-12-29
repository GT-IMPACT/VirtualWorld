package space.gtimpact.virtual_world.api

import net.minecraftforge.fluids.FluidStack

/**
 * Type for Virtual Fluid
 */
typealias VirtualFluidTypeComponent = FluidStack

/**
 * Virtual Fluid Instance
 *
 * @param id ID of fluid
 * @param depth depth of drilling
 * @param name name of fluid
 * @param weight max weight by generation on the world
 * @param rangeSize fluid quantity range
 * @param color color of vein
 * @param dimensions dimensions in which there is ore
 * @param fluid component of fluid vein
 * @param type type of vein
 */
data class VirtualFluidVein(
    val id: Int,
    val depth: Int,
    val name: String,
    var weight: Double,
    val rangeSize: IntRange?,
    val color: Int,
    val dimensions: List<Int>,
    val fluid: VirtualFluidTypeComponent,
    val isHidden: Boolean = false,
) {
    init {
        VirtualAPI.registerVirtualFluid(this)
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

enum class TypeFluidVein {
    LP, // Low Pressure
    MP, // Medium Pressure
    HP, // High Pressure
}