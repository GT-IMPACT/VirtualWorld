package space.impact.virtual_world.api

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.chunk.Chunk
import space.impact.virtual_world.api.fluids.RegionFluid
import space.impact.virtual_world.api.ores.RegionOre

interface IResourceWorld {
    val resourceRegions: Set<ResourceRegion>
    fun generateResourceRegion(chunk: Chunk)
    fun getResourceRegionFromChunk(chunk: Chunk): ResourceRegion?
    fun writeToNBT(nbt: NBTTagCompound)
    fun readFromNBT(nbt: NBTTagCompound)
}

data class ResourceRegion(
    val regionOre: RegionOre,
    val regionFluid: RegionFluid,
) {

    fun writeToNBT(nbt: NBTTagCompound) {
        val regionOreNbt = NBTTagCompound()
        regionOre.writeToNBT(regionOreNbt)
        nbt.setTag("ORE_REGION", regionOreNbt)

        val regionFluidNbt = NBTTagCompound()
        regionFluid.writeToNBT(regionFluidNbt)
        nbt.setTag("FLUID_REGION", regionFluidNbt)
    }

    companion object {
        @JvmStatic
        fun readFromNBT(nbt: NBTTagCompound): ResourceRegion {
            return ResourceRegion(
                regionOre = RegionOre.readFromNBT(nbt.getCompoundTag("ORE_REGION")),
                regionFluid = RegionFluid.readFromNBT(nbt.getCompoundTag("FLUID_REGION")),
            )
        }
    }
}
