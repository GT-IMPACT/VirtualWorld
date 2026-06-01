package space.gtimpact.virtual_world.api.core

import space.gtimpact.virtual_world.api.resources.VirtualResource

data class GeneratedRegion(
    val dimensionId: Int,
    val pos: RegionPos,
    val origin: WorldPos,
    val resources: List<VirtualResource>,
)
