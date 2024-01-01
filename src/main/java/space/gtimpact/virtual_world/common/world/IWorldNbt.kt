package space.gtimpact.virtual_world.common.world

import net.minecraft.nbt.NBTTagCompound

interface IWorldNbt {
    fun writeToNBT(nbt: NBTTagCompound)
    fun readFromNBT(nbt: NBTTagCompound)
    fun addChunk(ch: IModifiableChunk, nbt: NBTTagCompound, name: String)
    fun getChunkNbt(ch: IModifiableChunk): NBTTagCompound
}
