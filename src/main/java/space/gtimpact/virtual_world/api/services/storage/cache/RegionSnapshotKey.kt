package space.gtimpact.virtual_world.api.services.storage.cache

import net.minecraft.nbt.NBTTagCompound

data class RegionSnapshotKey(
    val worldSeed: Long,
    val dimensionId: Int,
    val regionX: Int,
    val regionZ: Int
) {
    fun toNbt(): NBTTagCompound {
        val tag = NBTTagCompound()
        tag.setLong("worldSeed", worldSeed)
        tag.setInteger("dimensionId", dimensionId)
        tag.setInteger("regionX", regionX)
        tag.setInteger("regionZ", regionZ)
        return tag
    }
}
