package space.gtimpact.virtual_world.api.resources.fluids

import space.gtimpact.virtual_world.api.VirtualAPI

class FluidVeinResourceGeneratorConfig(
    val balanceAreaVeins: Int = 128,
    val emptyWeight: Double = 0.0,
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
