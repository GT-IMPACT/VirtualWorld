package space.gtimpact.virtual_world.api.services.storage.snapshot

import space.gtimpact.virtual_world.api.core.GeneratedRegion
import space.gtimpact.virtual_world.api.core.RegionPos
import space.gtimpact.virtual_world.api.core.WorldPos
import space.gtimpact.virtual_world.api.services.storage.cache.RegionSnapshotKey

object RegionSnapshotMapper {

    private const val SCHEMA_VERSION = 1

    fun toSnapshot(
        worldSeed: Long,
        dimensionId: Int,
        region: GeneratedRegion,
    ): RegionSnapshot {
        val key = RegionSnapshotKey(
            worldSeed = worldSeed,
            dimensionId = dimensionId,
            regionX = region.pos.x,
            regionZ = region.pos.z,
        )

        return RegionSnapshot(
            key = key,
            schemaVersion = SCHEMA_VERSION,
            region = RegionSnapshotPayload(
                originX = region.origin.x,
                originZ = region.origin.z,
                resources = region.resources.map { it.toSnapshot() },
            )
        )
    }

    fun fromSnapshot(
        snapshot: RegionSnapshot,
    ): GeneratedRegion {
        val payload = snapshot.region
        return GeneratedRegion(
            dimensionId = snapshot.key.dimensionId,
            pos = RegionPos(
                x = snapshot.key.regionX,
                z = snapshot.key.regionZ
            ),
            origin = WorldPos(
                x = payload.originX,
                z = payload.originZ
            ),
            resources = payload.resources.map { it.toResource() },
        )
    }
}
