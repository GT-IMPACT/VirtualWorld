package space.gtimpact.virtual_world.api.resources.fluids

import net.minecraft.nbt.NBTTagCompound
import space.gtimpact.virtual_world.api.VirtualAPI
import space.gtimpact.virtual_world.api.core.ResourcePos
import space.gtimpact.virtual_world.api.core.WorldPos
import space.gtimpact.virtual_world.api.resources.VirtualResource
import space.gtimpact.virtual_world.api.services.storage.snapshot.ResourceSnapshot
import space.gtimpact.virtual_world.api.services.storage.snapshot.VirtualResourceSnapshot

data class FluidVeinResourceSnapshot(
    override val resX: Int,
    override val resZ: Int,
    override val originX: Int,
    override val originZ: Int,
    val fluid: ResourceSnapshot,
    val amount: Int,
) : VirtualResourceSnapshot {

    override fun toResource(): VirtualResource {
        return FluidVeinResource(
            pos = ResourcePos(x = resX, z = resZ),
            origin = WorldPos(x = originX, z = originZ),
            fluid = fluid.toFluidType(amount),
            amount = amount,
        )
    }

    private fun ResourceSnapshot.toFluidType(amount: Int): FluidVein {
        return VirtualAPI.resourcesRegistry.getFluidVein(stableId)
            ?: FluidVein(
                id = stableId,
                name = displayName,
                color = color,
                dimensions = emptySet(),
                weight = 0.0,
                rangeSize = amount..amount,
                fluid = null,
                isHidden = true,
            )
    }

    override fun toNbt(): NBTTagCompound {
        val tag = NBTTagCompound()
        tag.setInteger("x", resX)
        tag.setInteger("z", resZ)
        tag.setInteger("originX", originX)
        tag.setInteger("originZ", originZ)
        tag.setInteger("amount", amount)
        tag.setTag("fluid", fluid.toNbt())
        return tag
    }
}
