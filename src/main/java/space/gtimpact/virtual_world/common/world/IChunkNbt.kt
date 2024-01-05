package space.gtimpact.virtual_world.common.world

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.ChunkCoordIntPair

interface IModifiableChunk {
    fun getNbt(name: String): NBTTagCompound?
    fun setNbt(nbt: NBTTagCompound, name: String)
    fun getCoords(): ChunkCoordIntPair
}
