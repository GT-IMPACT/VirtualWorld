package space.gtimpact.virtual_world.api.services.storage.cache

import space.gtimpact.virtual_world.api.core.GeneratedRegion
import space.gtimpact.virtual_world.api.services.storage.WorldRegionKey
import space.gtimpact.virtual_world.config.Config

class RegionCache(
    private val maxRegions: Int = Config.maxCachedRegions,
) {

    private val maxRegionsLimit = maxRegions.coerceAtLeast(1)
    private val lock = Any()

    private val cache = object : LinkedHashMap<WorldRegionKey, GeneratedRegion>(
        maxRegions,
        0.75f,
        true
    ) {
        override fun removeEldestEntry(
            eldest: MutableMap.MutableEntry<WorldRegionKey, GeneratedRegion>
        ): Boolean {
            return size > maxRegionsLimit
        }
    }

    fun get(key: WorldRegionKey, loader: () -> GeneratedRegion): GeneratedRegion {
        synchronized(lock) {
            cache[key]?.let { region ->
                return region
            }
        }

        val loaded = loader()

        return synchronized(lock) {
            cache[key] ?: loaded.also { region ->
                cache[key] = region
            }
        }
    }

    fun getIfPresent(key: WorldRegionKey): GeneratedRegion? {
        return synchronized(lock) {
            cache[key]
        }
    }

    fun put(key: WorldRegionKey, region: GeneratedRegion) {
        synchronized(lock) {
            cache[key] = region
        }
    }

    fun contains(key: WorldRegionKey): Boolean {
        return synchronized(lock) {
            cache.containsKey(key)
        }
    }

    fun clear() {
        synchronized(lock) {
            cache.clear()
        }
    }

    fun size(): Int {
        return synchronized(lock) {
            cache.size
        }
    }
}
