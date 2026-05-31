package space.gtimpact.virtual_world.api.resources.fluids

import space.gtimpact.virtual_world.api.core.ResourcePos
import space.gtimpact.virtual_world.api.core.StableRandom
import space.gtimpact.virtual_world.api.core.toWorldOrigin
import space.gtimpact.virtual_world.api.resources.VirtualResource
import space.gtimpact.virtual_world.api.resources.VirtualResourcesGenerator
import kotlin.random.Random

class FluidVeinResourceGenerator(
    private val worldSeed: Long,
    private val config: FluidVeinResourceGeneratorConfig,
): VirtualResourcesGenerator {

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
            random = random,
            totalWeight = config.totalWeight,
            fluids = availableFluids
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
        random: Random,
        totalWeight: Double,
        fluids: List<FluidVein>
    ): FluidVein? {

        if (fluids.isEmpty()) {
            return null
        }

        val roll = random.nextDouble(totalWeight)
        var cursor = 0.0

        for (fluid in fluids) {
            cursor += fluid.weight

            if (roll < cursor) {
                return fluid
            }
        }

        return null
    }
}
