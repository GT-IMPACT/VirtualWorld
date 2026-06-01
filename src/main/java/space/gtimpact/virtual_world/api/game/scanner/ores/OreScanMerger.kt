package space.gtimpact.virtual_world.api.game.scanner.ores

import space.gtimpact.virtual_world.api.game.scanner.ChunkKey
import space.gtimpact.virtual_world.api.game.scanner.VeinKey

internal fun mergeOreVein(
    old: ClientOreScanState.ClientOreVeinState?,
    new: ClientOreScanState.ClientOreVeinState
): ClientOreScanState.ClientOreVeinState {
    if (old == null) return new

    val mergedChunks = old.chunks
        .associateBy { ChunkKey(it.chunkX, it.chunkZ) }
        .toMutableMap()

    for (newChunk in new.chunks) {
        mergedChunks[ChunkKey(newChunk.chunkX, newChunk.chunkZ)] = newChunk
    }

    return new.copy(
        chunks = mergedChunks.values.toList()
    )
}

internal fun ClientOreScanState.ClientOreVeinState.toVeinKey(): VeinKey {
    return VeinKey(
        veinId = vein.id,
        veinX = position.x,
        veinZ = position.z,
    )
}
