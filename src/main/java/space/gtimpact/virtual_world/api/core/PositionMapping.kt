package space.gtimpact.virtual_world.api.core

fun WorldPos.toChunkPos(): ChunkPos {
    return ChunkPos(
        x = WorldGrid.worldToChunkCoord(x),
        z = WorldGrid.worldToChunkCoord(z)
    )
}

fun WorldPos.toResourcePos(): ResourcePos {
    return ResourcePos(
        x = WorldGrid.worldToVeinCoord(x),
        z = WorldGrid.worldToVeinCoord(z)
    )
}

fun WorldPos.toRegionPos(): RegionPos {
    return RegionPos(
        x = WorldGrid.worldToRegionCoord(x),
        z = WorldGrid.worldToRegionCoord(z)
    )
}

fun ChunkPos.toWorldOrigin(): WorldPos {
    return WorldPos(
        x = WorldGrid.chunkToWorldCoord(x),
        z = WorldGrid.chunkToWorldCoord(z)
    )
}

fun ResourcePos.toWorldOrigin(): WorldPos {
    return WorldPos(
        x = WorldGrid.veinToWorldCoord(x),
        z = WorldGrid.veinToWorldCoord(z)
    )
}

fun RegionPos.toWorldOrigin(): WorldPos {
    return WorldPos(
        x = WorldGrid.regionToWorldCoord(x),
        z = WorldGrid.regionToWorldCoord(z)
    )
}
