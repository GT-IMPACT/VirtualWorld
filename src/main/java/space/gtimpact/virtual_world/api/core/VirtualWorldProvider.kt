package space.gtimpact.virtual_world.api.core

import space.gtimpact.virtual_world.api.resources.fluids.FluidVeinResourceGeneratorConfig
import space.gtimpact.virtual_world.api.resources.ores.OreVeinLayerConfig
import space.gtimpact.virtual_world.api.resources.ores.OreVeinResourceGeneratorConfig
import space.gtimpact.virtual_world.config.Config

class VirtualWorldProvider {

    internal val oreVeinResourceGeneratorConfig = OreVeinResourceGeneratorConfig(
        layers = listOf(
            OreVeinLayerConfig(
                layerIndex = 0,
                balanceAreaVeins = Config.balanceAreaVeins,
                emptyWeight = Config.emptyWeight,
            ),
            OreVeinLayerConfig(
                layerIndex = 1,
                balanceAreaVeins = Config.balanceAreaVeins,
                emptyWeight = Config.emptyWeight,
            ),
        ),
    )

    internal val fluidVeinResourceGeneratorConfig = FluidVeinResourceGeneratorConfig(
        balanceAreaVeins = Config.balanceAreaVeins,
        emptyWeight = Config.emptyWeight,
    )

    internal var instance: VirtualWorld? = null

    fun starting(seed: Long) {
        instance?.regions?.clearCache()
        instance = VirtualWorld(
            worldSeed = seed,
            oreVeinResourceGeneratorConfig = oreVeinResourceGeneratorConfig,
            fluidVeinResourceGeneratorConfig = fluidVeinResourceGeneratorConfig,
        )
    }

    fun started() {
        // TODO
    }

    fun stopping() {
        // TODO
    }

    fun stopped() {
        instance?.regions?.clearCache()
    }
}
