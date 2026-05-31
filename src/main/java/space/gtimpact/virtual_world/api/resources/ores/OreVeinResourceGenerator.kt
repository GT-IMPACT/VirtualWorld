package space.gtimpact.virtual_world.api.resources.ores

import space.gtimpact.virtual_world.api.core.ChunkPos
import space.gtimpact.virtual_world.api.core.ResourcePos
import space.gtimpact.virtual_world.api.core.StableRandom
import space.gtimpact.virtual_world.api.core.WorldGrid
import space.gtimpact.virtual_world.api.core.toWorldOrigin
import space.gtimpact.virtual_world.api.resources.VirtualResource
import space.gtimpact.virtual_world.api.resources.VirtualResourcesGenerator
import kotlin.random.Random

class OreVeinResourceGenerator(
    private val worldSeed: Long,
    private val config: OreVeinResourceGeneratorConfig,
): VirtualResourcesGenerator {

    override fun generate(
        dimensionId: Int,
        pos: ResourcePos,
    ): VirtualResource {
        val origin = pos.toWorldOrigin()

        val layers = buildList {
            for (layerConfig in config.layers) {
                val layer = generateLayer(
                    dimensionId = dimensionId,
                    pos = pos,
                    layerConfig = layerConfig,
                )

                if (layer != null) {
                    this += layer
                }
            }
        }

        return OreVeinResource(
            pos = pos,
            origin = origin,
            layers = layers
        )
    }

    private fun generateLayer(
        dimensionId: Int,
        pos: ResourcePos,
        layerConfig: OreVeinLayerConfig,
    ): OreVeinLayer? {
        val random = StableRandom.fromSeedAndResourceLayer(
            worldSeed = worldSeed,
            dimensionId = dimensionId,
            pos = pos,
            layerIndex = layerConfig.layerIndex,
        )

        val availableOres = config.getOresFor(
            dimensionId = dimensionId,
            layerIndex = layerConfig.layerIndex,
        )

        val ore = pickOre(
            random = random,
            totalWeight = layerConfig.totalWeight,
            ores = availableOres,
        ) ?: return null


        val chunks = buildList {
            for (localChunkX in 0 until WorldGrid.VEIN_CHUNKS) {
                for (localChunkZ in 0 until WorldGrid.VEIN_CHUNKS) {

                    val chunkPos = ChunkPos(
                        x = pos.x * WorldGrid.VEIN_CHUNKS + localChunkX,
                        z = pos.z * WorldGrid.VEIN_CHUNKS + localChunkZ,
                    )

                    val amount = random.nextInt(
                        from = ore.rangeSize.first,
                        until = ore.rangeSize.last + 1,
                    )

                    this += OreChunkDeposit(
                        localChunkX = localChunkX,
                        localChunkZ = localChunkZ,
                        chunkPos = chunkPos,
                        amount = amount,
                    )
                }
            }
        }

        return OreVeinLayer(
            layerIndex = layerConfig.layerIndex,
            ore = ore,
            chunks = chunks,
        )
    }

    private fun pickOre(
        random: Random,
        totalWeight: Double,
        ores: List<OreVein>
    ): OreVein? {

        if (ores.isEmpty()) {
            return null
        }

        val roll = random.nextDouble(totalWeight)
        var cursor = 0.0

        for (ore in ores) {
            cursor += ore.weight

            if (roll < cursor) {
                return ore
            }
        }

        return null
    }
}
