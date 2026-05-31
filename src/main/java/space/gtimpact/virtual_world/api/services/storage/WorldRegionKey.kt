package space.gtimpact.virtual_world.api.services.storage

import space.gtimpact.virtual_world.api.core.RegionPos

data class WorldRegionKey(
    val worldSeed: Long,
    val dimensionId: Int,
    val regionPos: RegionPos,
)
