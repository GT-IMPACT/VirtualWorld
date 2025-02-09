package space.gtimpact.virtual_world.addon.visual_prospecting

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.sinthoras.visualprospecting.Utils
import journeymap.client.model.BlockCoordIntPair
import net.minecraft.world.ChunkCoordIntPair
import space.gtimpact.virtual_world.addon.visual_prospecting.cache.CacheFluidVein
import space.gtimpact.virtual_world.addon.visual_prospecting.cache.CacheObjectPoint
import space.gtimpact.virtual_world.addon.visual_prospecting.cache.CacheOreVein
import space.gtimpact.virtual_world.addon.visual_prospecting.cache.PutDataStatus
import space.gtimpact.virtual_world.addon.visual_prospecting.cache.parsers.read
import space.gtimpact.virtual_world.addon.visual_prospecting.cache.parsers.write
import space.gtimpact.virtual_world.api.ResourceGenerator
import space.gtimpact.virtual_world.api.VirtualFluidVein
import space.gtimpact.virtual_world.api.VirtualOreVein

class DimensionCache(private val dimId: Int) {

    companion object {
        internal fun keyOfCoords(chunkX: Int, chunkZ: Int): ChunkCoordIntPair {
            return ChunkCoordIntPair(Utils.mapToCenterOreChunkCoord(chunkX), Utils.mapToCenterOreChunkCoord(chunkZ))
        }

        internal fun keyOfCoordsBlock(blockX: Int, blockZ: Int): BlockCoordIntPair {
            return BlockCoordIntPair(blockX, blockZ)
        }
    }

    private val oreChunksLayer0 = HashMap<ChunkCoordIntPair, CacheOreVein>()
    private val oreChunksLayer1 = HashMap<ChunkCoordIntPair, CacheOreVein>()
    private val fluidChunks = HashMap<ChunkCoordIntPair, CacheFluidVein>()
    private val customObject = HashMap<BlockCoordIntPair, CacheObjectPoint>()

    fun saveOreChunksLayer0(): JsonElement {
        return oreChunksLayer0.write(dimId)
    }

    fun saveOreChunksLayer1(): JsonElement {
        return oreChunksLayer1.write(dimId)
    }

    fun saveFluidsChunks(): JsonElement {
        return fluidChunks.write(dimId)
    }

    fun saveObjects(): JsonElement {
        return customObject.write(dimId)
    }

    fun load(layer0: JsonObject?, layer1: JsonObject?, fluids: JsonObject?, customObjects: JsonObject?) {
        layer0.read(oreChunksLayer0, dimId)
        layer1.read(oreChunksLayer1, dimId)
        fluids.read(fluidChunks, dimId)
        customObjects.read(customObject, dimId)
    }

    fun putOre(layer: Int, veinPosition: CacheOreVein): PutDataStatus {
        val key = keyOfCoords(
            veinPosition.x shl ResourceGenerator.SHIFT_CHUNK_FROM_VEIN,
            veinPosition.z shl ResourceGenerator.SHIFT_CHUNK_FROM_VEIN,
        )
        return when (layer) {
            0 -> {
                val statues = if (oreChunksLayer0.containsKey(key)) PutDataStatus.UPDATE else PutDataStatus.NEW
                oreChunksLayer0[key] = veinPosition
                statues
            }

            1 -> {
                val statues = if (oreChunksLayer1.containsKey(key)) PutDataStatus.UPDATE else PutDataStatus.NEW
                oreChunksLayer1[key] = veinPosition
                statues
            }

            else -> PutDataStatus.ERROR
        }
    }

    fun getOreVein(layer: Int, x: Int, z: Int): CacheOreVein? {
        val key = keyOfCoords(x, z)
        return when (layer) {
            0 -> oreChunksLayer0[key]
            1 -> oreChunksLayer1[key]
            else -> null
        }
    }

    fun putFluid(veinPosition: CacheFluidVein): PutDataStatus {
        val key = keyOfCoords(
            veinPosition.x shl ResourceGenerator.SHIFT_CHUNK_FROM_VEIN,
            veinPosition.z shl ResourceGenerator.SHIFT_CHUNK_FROM_VEIN,
        )
        val statues = if (fluidChunks.containsKey(key)) PutDataStatus.UPDATE else PutDataStatus.NEW
        fluidChunks[key] = veinPosition

        return statues
    }

    fun getFluidVein(x: Int, z: Int): CacheFluidVein? {
        val key = keyOfCoords(x, z)
        return fluidChunks[key]
    }

    fun putObjectElement(element: CacheObjectPoint.ObjectElement, blockX: Int, blockZ: Int): PutDataStatus {
        val key = keyOfCoordsBlock(blockX, blockZ)
        val statues = if (customObject.containsKey(key)) PutDataStatus.UPDATE else PutDataStatus.NEW
        val objectCache = customObject.computeIfAbsent(key) { CacheObjectPoint(coords = key, dimId = dimId) }
        val updateCache = objectCache.copy(elements = objectCache.elements + element)
        customObject.replace(key, updateCache)

        return statues
    }

    fun putObjectChunk(obj: CacheObjectPoint, blockX: Int, blockZ: Int): PutDataStatus {
        val key = keyOfCoordsBlock(blockX, blockZ)
        val statues = if (customObject.containsKey(key)) PutDataStatus.UPDATE else PutDataStatus.NEW
        customObject[key] = obj

        return statues
    }

    fun getObjectChunk(blockX: Int, blockZ: Int): CacheObjectPoint? {
        val key = keyOfCoordsBlock(blockX, blockZ)
        return customObject[key]
    }

    fun removeObjectChunk(element: CacheObjectPoint.ObjectElement, blockX: Int, blockZ: Int): PutDataStatus {
        val key = keyOfCoordsBlock(blockX, blockZ)

        val statues = if (customObject.containsKey(key)) PutDataStatus.UPDATE else PutDataStatus.NEW

        customObject[key]?.also { cache ->
            customObject.replace(key, cache.copy(elements = cache.elements.mapNotNull { if (it.name == element.name) null else it }))
        }

        return statues
    }
}

data class VirtualOreVeinPosition(
    val dimId: Int,
    val x: Int,
    val z: Int,
    val vein: VirtualOreVein,
    val size: Int,
    val isMain: Boolean = false,
)

data class VirtualFluidVeinPosition(
    val dimId: Int,
    val x: Int,
    val z: Int,
    val vein: VirtualFluidVein,
    val size: Int,
)
