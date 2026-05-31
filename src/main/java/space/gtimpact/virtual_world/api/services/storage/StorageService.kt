package space.gtimpact.virtual_world.api.services.storage

import space.gtimpact.virtual_world.api.core.GeneratedRegion
import space.gtimpact.virtual_world.api.core.RegionGenerator
import space.gtimpact.virtual_world.api.core.RegionPos
import space.gtimpact.virtual_world.api.core.ResourcePos
import space.gtimpact.virtual_world.api.core.toRegionPos
import space.gtimpact.virtual_world.api.core.toWorldOrigin
import space.gtimpact.virtual_world.api.services.storage.cache.RegionCache
import space.gtimpact.virtual_world.api.services.storage.cache.RegionSnapshotKey
import space.gtimpact.virtual_world.api.services.storage.snapshot.RegionSnapshotMapper
import space.gtimpact.virtual_world.common.world.SaveDataRegionSnapshotStore

class StorageService(
    private val regionGenerator: RegionGenerator,
    private val worldSeed: Long,
) {
    private val snapshotStore  = SaveDataRegionSnapshotStore
    private val regionCache = RegionCache()
    private val locks = StripedLocks()

    fun getRegion(
        dimensionId: Int,
        pos: RegionPos,
    ): GeneratedRegion {
        val cacheKey = WorldRegionKey(
            worldSeed = worldSeed,
            dimensionId = dimensionId,
            regionPos = pos,
        )

        regionCache.getIfPresent(cacheKey)?.also { cachedRegion ->
            return cachedRegion
        }

        val snapshotKey = RegionSnapshotKey(
            worldSeed = worldSeed,
            dimensionId = dimensionId,
            regionX = pos.x,
            regionZ = pos.z
        )

        return synchronized(locks.lockFor(cacheKey)) {
            regionCache.getIfPresent(cacheKey)?.also { cachedRegion ->
                return@synchronized cachedRegion
            }

            val snapshot = snapshotStore.get(snapshotKey)

            val generatedRegion = if (snapshot != null) {
                RegionSnapshotMapper.fromSnapshot(snapshot)
            } else {
                val generatedRegion = regionGenerator.generateRegion(
                    dimensionId = dimensionId,
                    pos = pos,
                )

                val regionSnapshot = RegionSnapshotMapper.toSnapshot(
                    worldSeed = worldSeed,
                    dimensionId = dimensionId,
                    region = generatedRegion
                )

                snapshotStore.put(regionSnapshot)

                generatedRegion
            }

            regionCache.put(cacheKey, generatedRegion)

            generatedRegion
        }
    }

    fun preloadRegionsForVeins(
        dimensionId: Int,
        veinPositions: Collection<ResourcePos>,
    ) {
        val regionPositions = veinPositions
            .map { veinPos -> veinPos.toWorldOrigin().toRegionPos() }
            .toSet()

        for (regionPos in regionPositions) {
            getRegion(
                dimensionId = dimensionId,
                pos = regionPos,
            )
        }
    }

    fun clearCache() {
        regionCache.clear()
    }

    fun getCachedRegionCount(): Int {
        return regionCache.size()
    }
}
