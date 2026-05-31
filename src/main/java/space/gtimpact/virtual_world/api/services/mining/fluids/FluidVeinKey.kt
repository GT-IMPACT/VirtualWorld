package space.gtimpact.virtual_world.api.services.mining.fluids

import net.minecraft.nbt.NBTTagCompound
import space.gtimpact.virtual_world.api.core.ResourcePos

data class FluidVeinKey(
    val worldSeed: Long,
    val dimensionId: Int,
    val pos: ResourcePos,
) {
    fun toNBT(): NBTTagCompound {
        val tag = NBTTagCompound()
        tag.setLong("worldSeed", worldSeed)
        tag.setInteger("amount", dimensionId)
        tag.setInteger("x", pos.x)
        tag.setInteger("z", pos.z)
        return tag
    }

    companion object {
        fun fromNBT(tag: NBTTagCompound): FluidVeinKey {
            return FluidVeinKey(
                worldSeed = tag.getLong("worldSeed"),
                dimensionId = tag.getInteger("dimensionId"),
                pos = ResourcePos(
                    x = tag.getInteger("x"),
                    z = tag.getInteger("z"),
                ),
            )
        }
    }
}
