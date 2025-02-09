package space.gtimpact.virtual_world.addon.visual_prospecting.cache

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import space.gtimpact.virtual_world.addon.visual_prospecting.DimensionCache
import java.io.File

abstract class VirtualWorldCache {

    companion object {
        const val DIR_ORES_L0 = "ores_0"
        const val DIR_ORES_L1 = "ores_1"
        const val DIR_FLUIDS = "fluids"
        const val DIR_OBJECTS = "objects"
    }

    protected val dimensions: HashMap<Int, DimensionCache> = HashMap()

    private val gson = Gson()

    protected abstract fun getStorageDirectory(): File

    fun loadVeinCache(worldId: String): Boolean {
        dimensions.clear()
        return try {
            val worldCacheDirectory = File(getStorageDirectory(), worldId)

            worldCacheDirectory.listFiles()?.forEach { file ->
                val dim = file.name.replace("DIM", "").toIntOrNull() ?: return@forEach
                val dimension = DimensionCache(dim)

                val layer0 = readJson(file, DIR_ORES_L0)
                val layer1 = readJson(file, DIR_ORES_L1)
                val fluids = readJson(file, DIR_FLUIDS)
                val objects = readJson(file, DIR_OBJECTS)

                dimension.load(layer0, layer1, fluids, objects)
                dimensions[dim] = dimension
            }

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun readJson(file: File, dirName: String): JsonObject? {
        return runCatching {
            val fileTarget = File(file, "$dirName.json")
            if (!fileTarget.canRead()) fileTarget.createNewFile()
            gson.fromJson(fileTarget.readText(), JsonObject::class.java)
        }.getOrNull()
    }

    fun saveCache(worldId: String) {
        try {
            val worldCacheDirectory = File(getStorageDirectory(), worldId)

            dimensions.forEach { (dimId, cache) ->

                val dimFolder = File(worldCacheDirectory, "DIM$dimId")
                dimFolder.mkdirs()

                saveJson(dimFolder, cache.saveOreChunksLayer0(), DIR_ORES_L0)
                saveJson(dimFolder, cache.saveOreChunksLayer1(), DIR_ORES_L1)
                saveJson(dimFolder, cache.saveFluidsChunks(), DIR_FLUIDS)
                saveJson(dimFolder, cache.saveObjects(), DIR_OBJECTS)

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveJson(dimFolder: File, cache: JsonElement, dirName: String) {
        runCatching {
            val fileTarget = File(dimFolder, "$dirName.json")
            if (!fileTarget.canRead()) fileTarget.createNewFile()
            fileTarget.writeText(cache.toString())
        }
    }

    private fun getOrCreateCache(dimId: Int): DimensionCache {
        var dimension = dimensions[dimId]

        if (dimension == null) {
            dimension = DimensionCache(dimId)
            dimensions[dimId] = dimension
        }
        return dimension
    }

    private inline fun getAndUpdateDimensionCache(dimId: Int, onUpdate: (DimensionCache) -> PutDataStatus): PutDataStatus {
        val data = getOrCreateCache(dimId)
        return onUpdate(data)
    }

    fun putOre(layer: Int, veinPosition: CacheOreVein): PutDataStatus {
        return getAndUpdateDimensionCache(veinPosition.dimension) { data ->
            data.putOre(layer, veinPosition)
        }
    }

    fun getOre(layer: Int, dimId: Int, x: Int, z: Int): CacheOreVein? {
        return getOrCreateCache(dimId).getOreVein(layer, x, z)
    }

    fun putFluid(veinPosition: CacheFluidVein): PutDataStatus {
        return getAndUpdateDimensionCache(veinPosition.dimension) { data ->
            data.putFluid(veinPosition)
        }
    }

    fun getFluid(dimId: Int, x: Int, z: Int): CacheFluidVein? {
        return getOrCreateCache(dimId).getFluidVein(x, z)
    }

    fun putObjectElement(element: CacheObjectPoint.ObjectElement, dimId: Int, blockX: Int, blockZ: Int): PutDataStatus {
        return getAndUpdateDimensionCache(dimId) { data ->
            data.putObjectElement(element, blockX, blockZ)
        }
    }

    fun putObjectChunk(obj: CacheObjectPoint, dimId: Int, blockX: Int, blockZ: Int): PutDataStatus {
        return getAndUpdateDimensionCache(dimId) { data ->
            data.putObjectChunk(obj, blockX, blockZ)
        }
    }

    fun getObjectChunk(dimId: Int, blockX: Int, blockZ: Int): CacheObjectPoint? {
        return getOrCreateCache(dimId).getObjectChunk(blockX, blockZ)
    }

    fun removeObjectChunk(element: CacheObjectPoint.ObjectElement, dimId: Int, blockX: Int, blockZ: Int): PutDataStatus {
        return getAndUpdateDimensionCache(dimId) { data ->
            data.removeObjectChunk(element, blockX, blockZ)
        }
    }
}
