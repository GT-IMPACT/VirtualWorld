package space.gtimpact.virtual_world.common.world

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.ChunkCoordIntPair

interface IChunkNbt {
    fun writeToNBT(nbt: NBTTagCompound)
    fun readFromNBT(nbt: NBTTagCompound)
    fun getCoords(): ChunkCoordIntPair
}

interface IModifiableChunk : IChunkNbt {

    companion object {
        /** dim of chunk */
        val modifiedChunks = HashMap<Int, ChunkCoordIntPair>()
    }

    fun getNbt(): NBTTagCompound?
    fun setNbt(nbt: NBTTagCompound)
    fun isModified(): Boolean
}
