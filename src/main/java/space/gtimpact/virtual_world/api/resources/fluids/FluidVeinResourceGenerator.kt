package space.gtimpact.virtual_world.api.resources.fluids

import space.gtimpact.virtual_world.api.core.ResourcePos
import space.gtimpact.virtual_world.api.core.StableRandom
import space.gtimpact.virtual_world.api.core.toWorldOrigin
import space.gtimpact.virtual_world.api.resources.BalancedWeightedPicker
import space.gtimpact.virtual_world.api.resources.VirtualResource
import space.gtimpact.virtual_world.api.resources.VirtualResourcesGenerator

class FluidVeinResourceGenerator(
    private val worldSeed: Long,
    private val config: FluidVeinResourceGeneratorConfig,
): VirtualResourcesGenerator {

    private val fluidPicker = BalancedWeightedPicker<FluidVein>(
        idOf = { fluid -> fluid.id },
        weightOf = { fluid -> fluid.weight },
    )

    override fun generate(
        dimensionId: Int,
        pos: ResourcePos,
    ): VirtualResource? {

        val random = StableRandom.fromSeedAndFluidResource(
            worldSeed = worldSeed,
            dimensionId = dimensionId,
            pos = pos,
        )

        val availableFluids = config.getFluidsFor(
            dimensionId = dimensionId,
        )

        val fluid = pickFluid(
            dimensionId = dimensionId,
            pos = pos,
            fluids = availableFluids,
        )

        val origin = pos.toWorldOrigin()

        if (fluid == null) {
            return null
        }

        val amount = random.nextInt(
            from = fluid.rangeSize.first,
            until = fluid.rangeSize.last + 1,
        )

        return FluidVeinResource(
            pos = pos,
            origin = origin,
            fluid = fluid,
            amount = amount,
        )
    }

    private fun pickFluid(
        dimensionId: Int,
        pos: ResourcePos,
        fluids: List<FluidVein>,
    ): FluidVein? {
        return fluidPicker.pick(
            worldSeed = worldSeed,
            dimensionId = dimensionId,
            pos = pos,
            balanceAreaVeins = config.balanceAreaVeins,
            emptyWeight = config.emptyWeight,
            channel = FLUID_CHANNEL,
            items = fluids,
        )
    }

    private companion object {
        const val FLUID_CHANNEL = -5797282940391623497L
    }
}
