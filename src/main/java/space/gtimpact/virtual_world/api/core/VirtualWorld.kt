package space.gtimpact.virtual_world.api.core

import space.gtimpact.virtual_world.api.resources.fluids.FluidVeinResourceGenerator
import space.gtimpact.virtual_world.api.resources.fluids.FluidVeinResourceGeneratorConfig
import space.gtimpact.virtual_world.api.resources.ores.OreVeinResourceGenerator
import space.gtimpact.virtual_world.api.resources.ores.OreVeinResourceGeneratorConfig
import space.gtimpact.virtual_world.api.services.mining.MiningService
import space.gtimpact.virtual_world.api.services.scanning.ScanningService
import space.gtimpact.virtual_world.api.services.storage.StorageService
import space.gtimpact.virtual_world.common.world.SaveDataMiningStateStore

class VirtualWorld(
    val worldSeed: Long,
    oreVeinResourceGeneratorConfig: OreVeinResourceGeneratorConfig,
    fluidVeinResourceGeneratorConfig: FluidVeinResourceGeneratorConfig,
) {

    private val oreGenerator = OreVeinResourceGenerator(
        worldSeed = worldSeed,
        config = oreVeinResourceGeneratorConfig,
    )

    private val fluidGenerator = FluidVeinResourceGenerator(
        worldSeed = worldSeed,
        config = fluidVeinResourceGeneratorConfig,
    )

    private val regionGenerator = RegionGenerator(
        generators = listOf(
            oreGenerator,
            fluidGenerator,
        ),
    )

    internal val regions = StorageService(
        regionGenerator = regionGenerator,
        worldSeed = worldSeed,
    )

    val mining = MiningService(
        worldSeed = worldSeed,
        regions = regions,
        state = SaveDataMiningStateStore,
    )

    val scanning = ScanningService(
        regions = regions,
        mining = mining,
    )
}
