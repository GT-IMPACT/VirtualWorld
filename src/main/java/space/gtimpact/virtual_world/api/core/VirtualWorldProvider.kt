package space.gtimpact.virtual_world.api.core

import space.gtimpact.virtual_world.api.resources.fluids.FluidVeinResourceGeneratorConfig
import space.gtimpact.virtual_world.api.resources.ores.OreVeinLayerConfig
import space.gtimpact.virtual_world.api.resources.ores.OreVeinResourceGeneratorConfig

class VirtualWorldProvider {

    internal val oreVeinResourceGeneratorConfig = OreVeinResourceGeneratorConfig(
        layers = listOf(
            OreVeinLayerConfig(layerIndex = 0, totalWeight = 100.0),
            OreVeinLayerConfig(layerIndex = 1, totalWeight = 100.0),
        ),
    )

    internal val fluidVeinResourceGeneratorConfig = FluidVeinResourceGeneratorConfig(
        totalWeight = 100.0,
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
