package space.gtimpact.virtual_world.api.resources.ores

import space.gtimpact.virtual_world.api.core.ChunkPos
import space.gtimpact.virtual_world.api.core.ResourcePos
import space.gtimpact.virtual_world.api.core.StableRandom
import space.gtimpact.virtual_world.api.core.WorldGrid
import space.gtimpact.virtual_world.api.core.toWorldOrigin
import space.gtimpact.virtual_world.api.resources.BalancedWeightedPicker
import space.gtimpact.virtual_world.api.resources.VirtualResource
import space.gtimpact.virtual_world.api.resources.VirtualResourcesGenerator

class OreVeinResourceGenerator(
    private val worldSeed: Long,
    private val config: OreVeinResourceGeneratorConfig,
): VirtualResourcesGenerator {

    private val orePicker = BalancedWeightedPicker<OreVein>(
        idOf = { ore -> ore.id },
        weightOf = { ore -> ore.weight },
    )

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
            dimensionId = dimensionId,
            pos = pos,
            layerConfig = layerConfig,
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
        dimensionId: Int,
        pos: ResourcePos,
        layerConfig: OreVeinLayerConfig,
        ores: List<OreVein>,
    ): OreVein? {
        return orePicker.pick(
            worldSeed = worldSeed,
            dimensionId = dimensionId,
            pos = pos,
            balanceAreaVeins = layerConfig.balanceAreaVeins,
            emptyWeight = layerConfig.emptyWeight,
            channel = ORE_CHANNEL xor (layerConfig.layerIndex.toLong() * LAYER_CHANNEL_CONST),
            items = ores,
        )
    }

    private companion object {
        const val ORE_CHANNEL = -3372029247567499371L
        const val LAYER_CHANNEL_CONST = -7723592293110705685L
    }
}
