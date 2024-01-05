package space.gtimpact.virtual_world.util

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.world.ChunkCoordIntPair
import net.minecraft.world.World
import space.gtimpact.virtual_world.common.world.IModifiableChunk
import space.gtimpact.virtual_world.common.world.IWorldNbt
import space.gtimpact.virtual_world.extras.NBT

object WorldNBT {

    @JvmStatic
    fun getChunkNBT(chunk: IModifiableChunk, name: String, world: World): NBTTagCompound? {
        return if (world is IWorldNbt) {
            val chunkData = world.getChunkNbt(chunk)
            val nbt = chunkData.getCompoundTag(name)
            if (nbt == null || nbt.hasNoTags()) null else nbt
        } else null
    }

    @JvmStatic
    fun setChunkNBT(chunk: IModifiableChunk, nbt: NBTTagCompound, name: String, world: World) {
        if (world is IWorldNbt) {
            world.addChunk(chunk, nbt, name)
        }
    }

    @JvmStatic
    fun readFromNBTWorld(map: HashMap<ChunkCoordIntPair, NBTTagCompound>, nbt: NBTTagCompound, world: World) {
        map.clear()
        val chunks = nbt.getTag(NBT.CHUNKS) as NBTTagList
        for (i in 0 until chunks.tagCount()) {
            val chunk = chunks.getCompoundTagAt(i)
            if (chunk != null) {
                val ch = world.getChunkFromChunkCoords(
                    chunk.getInteger(NBT.X_POS),
                    chunk.getInteger(NBT.Z_POS)
                )
                if (ch is IModifiableChunk) {
                    map[ch.chunkCoordIntPair] = chunk
                }
            }
        }
    }

    @JvmStatic
    fun writeToNBTWorld(map: HashMap<ChunkCoordIntPair, NBTTagCompound>, nbt: NBTTagCompound, world: World) {
        val chunks = NBTTagList()
        for ((key, chunkNbt) in map) {
            val ch = world.getChunkFromChunkCoords(key.chunkXPos, key.chunkZPos)
            if (ch is IModifiableChunk) {
                chunkNbt.setInteger(NBT.X_POS, key.chunkXPos)
                chunkNbt.setInteger(NBT.Z_POS, key.chunkZPos)
                chunks.appendTag(chunkNbt)
            }
        }
        nbt.setTag(NBT.CHUNKS, chunks)
    }
}
