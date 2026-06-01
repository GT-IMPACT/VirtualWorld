package space.gtimpact.virtual_world.api.services.storage.snapshot

import net.minecraft.nbt.NBTTagCompound
import space.gtimpact.virtual_world.api.services.storage.cache.RegionSnapshotKey

data class RegionSnapshot(
    val key: RegionSnapshotKey,
    val schemaVersion: Int,
    val region: RegionSnapshotPayload,
) {
    fun toNbt(): NBTTagCompound {
        val tag = NBTTagCompound()
        tag.setInteger("schemaVersion", schemaVersion)
        tag.setTag("key", key.toNbt())
        tag.setTag("region", region.toNbt())
        return tag
    }
}
