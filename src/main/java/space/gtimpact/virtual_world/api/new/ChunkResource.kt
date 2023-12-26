package space.gtimpact.virtual_world.api.new

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import space.gtimpact.virtual_world.api.VirtualAPI
import space.gtimpact.virtual_world.api.VirtualFluidVein
import space.gtimpact.virtual_world.api.VirtualOreVein

class ChunkResource(
    val xCord: Int,
    val zCord: Int,
    val dimId: Int,
    val ores: Array<OreVeinCount>,
    val fluids: Array<FluidVeinCount>
)

class OreVeinCount(
    val type: VirtualOreVein,
    val size: Int,
)

class FluidVeinCount(
    val type: VirtualFluidVein,
    val size: Int,
)

fun ChunkResource.saveNbt(): NBTTagCompound {
    val nbt = NBTTagCompound()

    nbt.setInteger("x", xCord)
    nbt.setInteger("z", zCord)
    nbt.setInteger("dim", dimId)

    val oresNbt = NBTTagList()
    for (ore in ores) {
        val oreNbt = NBTTagCompound()
        oreNbt.setInteger("type", ore.type.id)
        oreNbt.setInteger("size", ore.size)
        oresNbt.appendTag(oreNbt)
    }
    nbt.setInteger("oresSize", ores.size)
    nbt.setTag("ores", oresNbt)

    val fluidsNbt = NBTTagList()
    for (fluid in fluids) {
        val fluidNbt = NBTTagCompound()
        fluidNbt.setInteger("type", fluid.type.id)
        fluidNbt.setInteger("size", fluid.size)
        fluidsNbt.appendTag(fluidNbt)
    }
    nbt.setInteger("fluidsSize", fluids.size)
    nbt.setTag("fluids", fluidsNbt)

    return nbt
}

fun NBTTagCompound.loadFromNbtChunkResource(): ChunkResource {
    val xCord = getInteger("x")
    val zCord = getInteger("z")
    val dimId = getInteger("dim")

    val oresNbt = getTagList("ores", 0)
    val ores = Array(getInteger("oresSize")) {
        val tag = oresNbt.getCompoundTagAt(it)
        OreVeinCount(
            type = VirtualAPI.getVirtualOreVeinById(tag.getInteger("type")),
            size = tag.getInteger("size"),
        )
    }

    val fluidsNbt = getTagList("fluids", 0)
    val fluids = Array(getInteger("fluidsSize")) {
        val tag = fluidsNbt.getCompoundTagAt(it)
        FluidVeinCount(
            type = VirtualAPI.getVirtualFluidVeinById(tag.getInteger("type")),
            size = tag.getInteger("size"),
        )
    }

    return ChunkResource(
        xCord = xCord,
        zCord = zCord,
        dimId = dimId,
        ores = ores,
        fluids = fluids,
    )
}
