package space.gtimpact.virtual_world.api.resources.ores

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import space.gtimpact.virtual_world.api.VirtualAPI
import space.gtimpact.virtual_world.api.core.ChunkPos
import space.gtimpact.virtual_world.api.core.ResourcePos
import space.gtimpact.virtual_world.api.core.WorldPos
import space.gtimpact.virtual_world.api.resources.VirtualResource
import space.gtimpact.virtual_world.api.services.storage.snapshot.ResourceSnapshot
import space.gtimpact.virtual_world.api.services.storage.snapshot.VirtualResourceSnapshot

data class OreVeinResourceSnapshot(
    override val resX: Int,
    override val resZ: Int,
    override val originX: Int,
    override val originZ: Int,
    val layers: List<OreLayerSnapshot>,
) : VirtualResourceSnapshot {

    data class OreLayerSnapshot(
        val layerIndex: Int,
        val ore: ResourceSnapshot,
        val chunks: List<OreChunkDepositSnapshot>,
    ) {
        fun toNbt(): NBTTagCompound {
            val tag = NBTTagCompound()
            tag.setInteger("layerIndex", layerIndex)
            tag.setTag("ore", ore.toNbt())
            tag.setInteger("chunkIndex", chunks.size)
            val chunks = NBTTagList()
            this.chunks.forEach { chunk ->
                chunks.appendTag(chunk.toNbt())
            }
            tag.setInteger("chunksSize", this.chunks.size)
            tag.setTag("chunks", chunks)
            return tag
        }
    }

    data class OreChunkDepositSnapshot(
        val localChunkX: Int,
        val localChunkZ: Int,
        val chunkX: Int,
        val chunkZ: Int,
        val amount: Int,
    ) {
        fun toNbt(): NBTTagCompound {
            val tag = NBTTagCompound()
            tag.setInteger("localChunkX", localChunkX)
            tag.setInteger("localChunkZ", localChunkZ)
            tag.setInteger("chunkX", chunkX)
            tag.setInteger("chunkZ", chunkZ)
            tag.setInteger("amount", amount)
            return tag
        }
    }

    override fun toResource(): VirtualResource {
        return OreVeinResource(
            pos = ResourcePos(x = resX, z = resZ),
            origin = WorldPos(x = originX, z = originZ),
            layers = layers.map { layer ->
                val amounts = layer.chunks.map { it.amount }
                OreVeinLayer(
                    layerIndex = layer.layerIndex,
                    ore = layer.ore.toOreVein(
                        layerIndex = layer.layerIndex,
                        minOrePerChunk = amounts.minOrNull() ?: 0,
                        maxOrePerChunk = amounts.maxOrNull() ?: 0
                    ),
                    chunks = layer.chunks.map { deposit ->
                        OreChunkDeposit(
                            localChunkX = deposit.localChunkX,
                            localChunkZ = deposit.localChunkZ,
                            amount = deposit.amount,
                            chunkPos = ChunkPos(
                                x = deposit.chunkX,
                                z = deposit.chunkZ,
                            ),
                        )
                    }
                )
            }
        )
    }

    private fun ResourceSnapshot.toOreVein(
        layerIndex: Int,
        minOrePerChunk: Int,
        maxOrePerChunk: Int
    ): OreVein {
        return VirtualAPI.resourcesRegistry.getOreVein(stableId)
            ?: OreVein(
                id = stableId,
                name = displayName,
                color = color,
                dimensions = emptySet(),
                layer = layerIndex,
                weight = 0.0,
                rangeSize = minOrePerChunk..maxOrePerChunk,
                special = null,
                isHidden = true,
                ores = emptyList(),
            )
    }

    override fun toNbt(): NBTTagCompound {
        val tag = NBTTagCompound()
        tag.setInteger("x", resX)
        tag.setInteger("z", resZ)
        tag.setInteger("originX", originX)
        tag.setInteger("originZ", originZ)
        val layers = NBTTagList()
        this.layers.forEach { layer ->
            layers.appendTag(layer.toNbt())
        }
        tag.setInteger("layersSize", this.layers.size)
        tag.setTag("layers", layers)
        return tag
    }
}
