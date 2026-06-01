package space.gtimpact.virtual_world.api.services.storage.snapshot

import space.gtimpact.virtual_world.api.services.storage.cache.RegionSnapshotKey

interface RegionSnapshotStore {

    fun get(key: RegionSnapshotKey): RegionSnapshot?

    fun put(snapshot: RegionSnapshot)

    fun contains(key: RegionSnapshotKey): Boolean

    fun flush()
}
