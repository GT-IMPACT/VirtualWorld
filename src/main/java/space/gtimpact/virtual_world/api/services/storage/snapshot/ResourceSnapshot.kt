package space.gtimpact.virtual_world.api.services.storage.snapshot

import net.minecraft.nbt.NBTTagCompound

data class ResourceSnapshot(
    val stableId: Int,
    val displayName: String,
    val color: Int,
) {
    fun toNbt(): NBTTagCompound {
        val tag = NBTTagCompound()
        tag.setInteger("stableId", stableId)
        tag.setString("displayName", displayName)
        tag.setInteger("color", color)
        return tag
    }
}
