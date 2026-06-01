package space.gtimpact.virtual_world.api.resources.fluids

import space.gtimpact.virtual_world.api.core.ResourcePos
import space.gtimpact.virtual_world.api.core.WorldPos
import space.gtimpact.virtual_world.api.resources.VirtualResource
import space.gtimpact.virtual_world.api.services.storage.snapshot.ResourceSnapshot
import space.gtimpact.virtual_world.api.services.storage.snapshot.VirtualResourceSnapshot

data class FluidVeinResource(
    override val pos: ResourcePos,
    override val origin: WorldPos,
    val fluid: FluidVein,
    val amount: Int,
): VirtualResource {

    override fun toSnapshot(): VirtualResourceSnapshot {
        return FluidVeinResourceSnapshot(
            resX = pos.x,
            resZ = pos.z,
            originX = origin.x,
            originZ = origin.z,
            amount = amount,
            fluid = fluid.toResourceSnapshot(),
        )
    }

    private fun FluidVein.toResourceSnapshot(): ResourceSnapshot {
        return ResourceSnapshot(
            stableId = id,
            displayName = name,
            color = color
        )
    }
}
