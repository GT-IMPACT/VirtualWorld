package space.gtimpact.virtual_world.api.services.storage

import space.gtimpact.virtual_world.api.core.GeneratedRegion
import space.gtimpact.virtual_world.api.core.ResourcePos
import space.gtimpact.virtual_world.api.core.WorldPos
import space.gtimpact.virtual_world.api.core.toRegionPos
import space.gtimpact.virtual_world.api.core.toWorldOrigin
import space.gtimpact.virtual_world.api.resources.fluids.FluidVeinResource
import space.gtimpact.virtual_world.api.resources.ores.OreVeinResource

fun StorageService.getRegionAtBlock(
    dimensionId: Int,
    blockPos: WorldPos,
): GeneratedRegion {
    return getRegion(
        dimensionId = dimensionId,
        pos = blockPos.toRegionPos(),
    )
}

fun StorageService.getOreVeinAtVein(
    dimensionId: Int,
    pos: ResourcePos,
): OreVeinResource? {
    val region = getRegionAtBlock(
        dimensionId = dimensionId,
        blockPos = pos.toWorldOrigin(),
    )

    return region.resources.filterIsInstance<OreVeinResource>().firstOrNull { oreRes ->
        oreRes.pos == pos
    }
}

fun StorageService.getFluidVeinAtVein(
    dimensionId: Int, pos: ResourcePos
): FluidVeinResource? {
    val region = getRegionAtBlock(
        dimensionId = dimensionId,
        blockPos = pos.toWorldOrigin(),
    )

    return region.resources.filterIsInstance<FluidVeinResource>().firstOrNull { fluidVein ->
        fluidVein.pos == pos
    }
}
