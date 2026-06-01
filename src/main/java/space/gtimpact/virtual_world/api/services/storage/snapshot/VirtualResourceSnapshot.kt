package space.gtimpact.virtual_world.api.services.storage.snapshot

import net.minecraft.nbt.NBTTagCompound
import space.gtimpact.virtual_world.api.resources.VirtualResource

interface VirtualResourceSnapshot {
    val resX: Int
    val resZ: Int
    val originX: Int
    val originZ: Int

    fun toResource(): VirtualResource

    fun toNbt(): NBTTagCompound
}
