package space.gtimpact.virtual_world.common.world

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.world.World
import space.gtimpact.virtual_world.api.services.storage.cache.RegionSnapshotKey
import space.gtimpact.virtual_world.api.services.storage.snapshot.RegionSnapshot
import space.gtimpact.virtual_world.api.services.storage.snapshot.RegionSnapshotStore
import space.gtimpact.virtual_world.api.services.storage.snapshot.toSnapshot
import java.util.concurrent.ConcurrentHashMap

object SaveDataRegionSnapshotStore : RegionSnapshotStore {

    private val snapshots = ConcurrentHashMap<RegionSnapshotKey, RegionSnapshot>()

    fun writeToNBT(world: World, nbt: NBTTagCompound) {
        if (world.provider.dimensionId == 0) {
            val tag = NBTTagCompound()
            val snapshots = NBTTagList()
            this.snapshots.forEach { (key, value) ->
                snapshots.appendTag(value.toNbt())
            }
            tag.setInteger("snapshotsSize", this.snapshots.size)
            tag.setTag("snapshots", snapshots)
            nbt.setTag("SnapshotStore", tag)
        }
    }

    fun readFromNBT( world: World, nbt: NBTTagCompound) {
        if (world.provider.dimensionId == 0) {
            flush()

            val snapshotStoreNbt = nbt.getCompoundTag("SnapshotStore")
            val size = snapshotStoreNbt.getInteger("snapshotsSize")
            val snapshots = snapshotStoreNbt.getTag("snapshots") as NBTTagList
            for (i in 0 until size) {
                val region = snapshots.getCompoundTagAt(i).toSnapshot()
                if (region != null) {
                    put(region)
                }
            }
        }
    }

    override fun get(key: RegionSnapshotKey): RegionSnapshot? {
        return snapshots[key]
    }

    override fun put(snapshot: RegionSnapshot) {
        snapshots[snapshot.key] = snapshot
    }

    override fun contains(key: RegionSnapshotKey): Boolean {
        return snapshots.containsKey(key)
    }

    override fun flush() {
        snapshots.clear()
    }
}
