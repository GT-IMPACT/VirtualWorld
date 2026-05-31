package space.gtimpact.virtual_world.api.services.scanning

import space.gtimpact.virtual_world.api.core.ResourcePos
import space.gtimpact.virtual_world.api.core.WorldPos
import space.gtimpact.virtual_world.api.core.toResourcePos

internal fun calculateVeinAreaAroundBlock(
    centerBlockPos: WorldPos,
    radiusVeins: Int
): List<ResourcePos> {

    if (radiusVeins <= 0) {
        return emptyList()
    }

    val centerVeinPos = centerBlockPos.toResourcePos()
    val result = mutableListOf<ResourcePos>()

    for (offsetX in -radiusVeins..radiusVeins) {
        for (offsetZ in -radiusVeins..radiusVeins) {
            result += ResourcePos(
                x = centerVeinPos.x + offsetX,
                z = centerVeinPos.z + offsetZ
            )
        }
    }

    return result
}
