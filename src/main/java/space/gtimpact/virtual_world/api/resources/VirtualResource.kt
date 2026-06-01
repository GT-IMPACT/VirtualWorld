package space.gtimpact.virtual_world.api.resources

import space.gtimpact.virtual_world.api.core.ResourcePos
import space.gtimpact.virtual_world.api.core.WorldPos
import space.gtimpact.virtual_world.api.services.storage.snapshot.VirtualResourceSnapshot

interface VirtualResource {
    val pos: ResourcePos
    val origin: WorldPos

    fun toSnapshot(): VirtualResourceSnapshot
}
