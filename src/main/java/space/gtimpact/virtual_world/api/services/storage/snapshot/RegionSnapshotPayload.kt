package space.gtimpact.virtual_world.api.services.storage.snapshot

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList

data class RegionSnapshotPayload(
    val originX: Int,
    val originZ: Int,
    val resources: List<VirtualResourceSnapshot>,
) {
    fun toNbt(): NBTTagCompound {
        val tag = NBTTagCompound()
        tag.setInteger("originX", originX)
        tag.setInteger("originZ", originZ)
        val resources = NBTTagList()
        this.resources.forEach {
            resources.appendTag(it.toNbt())
        }
        tag.setInteger("resourcesSize", this.resources.size)
        tag.setTag("resources", resources)
        return tag
    }
}
