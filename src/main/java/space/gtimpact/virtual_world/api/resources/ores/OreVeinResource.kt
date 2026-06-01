package space.gtimpact.virtual_world.api.resources.ores

import space.gtimpact.virtual_world.api.core.ResourcePos
import space.gtimpact.virtual_world.api.core.WorldPos
import space.gtimpact.virtual_world.api.resources.VirtualResource
import space.gtimpact.virtual_world.api.resources.ores.OreVeinResourceSnapshot.OreChunkDepositSnapshot
import space.gtimpact.virtual_world.api.resources.ores.OreVeinResourceSnapshot.OreLayerSnapshot
import space.gtimpact.virtual_world.api.services.storage.snapshot.ResourceSnapshot
import space.gtimpact.virtual_world.api.services.storage.snapshot.VirtualResourceSnapshot

data class OreVeinResource(
    override val pos: ResourcePos,
    override val origin: WorldPos,
    val layers: List<OreVeinLayer>,
) : VirtualResource {

    override fun toSnapshot(): VirtualResourceSnapshot {
        return OreVeinResourceSnapshot(
            resX = pos.x,
            resZ = pos.z,
            originX = origin.x,
            originZ = origin.z,
            layers = layers.map { layer ->
                OreLayerSnapshot(
                    layerIndex = layer.layerIndex,
                    ore = layer.ore.toSnapshot(),
                    chunks = layer.chunks.map { deposit ->
                        OreChunkDepositSnapshot(
                            localChunkX = deposit.localChunkX,
                            localChunkZ = deposit.localChunkZ,
                            chunkX = deposit.chunkPos.x,
                            chunkZ = deposit.chunkPos.z,
                            amount = deposit.amount,
                        )
                    }
                )
            }
        )
    }

    private fun OreVein.toSnapshot(): ResourceSnapshot {
        return ResourceSnapshot(
            stableId = id,
            displayName = name,
            color = color,
        )
    }
}
