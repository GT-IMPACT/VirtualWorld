package space.gtimpact.virtual_world.api.services.scanning

import space.gtimpact.virtual_world.api.core.ResourcePos
import space.gtimpact.virtual_world.api.core.WorldGrid
import space.gtimpact.virtual_world.api.core.toWorldOrigin

data class VeinBounds(
    val veinPos: ResourcePos,
    val minBlockX: Int,
    val minBlockZ: Int,
    val maxBlockX: Int,
    val maxBlockZ: Int,
) {
    companion object {
        fun fromResPos(pos: ResourcePos): VeinBounds {
            val origin = pos.toWorldOrigin()

            return VeinBounds(
                veinPos = pos,
                minBlockX = origin.x,
                minBlockZ = origin.z,
                maxBlockX = origin.x + WorldGrid.VEIN_SIZE - 1,
                maxBlockZ = origin.z + WorldGrid.VEIN_SIZE - 1,
            )
        }
    }
}
