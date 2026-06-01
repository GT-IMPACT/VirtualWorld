package space.gtimpact.virtual_world.api.resources.fluids

import space.gtimpact.virtual_world.api.VirtualAPI

class FluidVeinResourceGeneratorConfig(
    val totalWeight: Double = 100.0,
) {

    fun getFluidsFor(
        dimensionId: Int,
    ): List<FluidVein> {
        return VirtualAPI.resourcesRegistry.fluidVeinsMap.values.filter { fluid ->
            fluid.canGenerateIn(
                dimensionId = dimensionId,
            )
        }
    }
}
