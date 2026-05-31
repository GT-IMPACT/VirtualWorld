package space.gtimpact.virtual_world.api.services.mining.ores

import net.minecraft.nbt.NBTTagCompound
import space.gtimpact.virtual_world.api.core.ChunkPos

data class OreChunkKey(
    val worldSeed: Long,
    val dimensionId: Int,
    val layerIndex: Int,
    val chunkPos: ChunkPos,
) {
    fun toNBT(): NBTTagCompound {
        val tag = NBTTagCompound()
        tag.setLong("worldSeed", worldSeed)
        tag.setInteger("dimensionId", dimensionId)
        tag.setInteger("layerIndex", layerIndex)
        tag.setInteger("x", chunkPos.x)
        tag.setInteger("z", chunkPos.z)
        return tag
    }

    companion object {
        fun fromNBT(tag: NBTTagCompound): OreChunkKey {
            return OreChunkKey(
                worldSeed = tag.getLong("worldSeed"),
                dimensionId = tag.getInteger("dimensionId"),
                layerIndex = tag.getInteger("layerIndex"),
                chunkPos = ChunkPos(
                    x = tag.getInteger("x"),
                    z = tag.getInteger("z"),
                ),
            )
        }
    }
}
