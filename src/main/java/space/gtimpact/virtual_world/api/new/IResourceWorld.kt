package space.gtimpact.virtual_world.api.new

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.chunk.Chunk
import space.gtimpact.virtual_world.api.fluids.RegionFluid
import space.gtimpact.virtual_world.api.ores.RegionOre

interface IResourceWorld {
    val regions: Set<ResourceRegion>
    fun generateRegion(ch: Chunk)
    fun getResourceRegionFromChunk(chunk: Chunk): ResourceRegion?
    fun writeToNBT(nbt: NBTTagCompound)
    fun readFromNBT(nbt: NBTTagCompound)
}

data class ResourceRegion(
    val regionOre: RegionOre,
    val regionFluid: RegionFluid,
)