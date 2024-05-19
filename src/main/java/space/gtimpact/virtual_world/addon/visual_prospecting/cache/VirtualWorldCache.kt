package space.gtimpact.virtual_world.addon.visual_prospecting.cache

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import space.gtimpact.virtual_world.addon.visual_prospecting.DimensionCache
import space.gtimpact.virtual_world.addon.visual_prospecting.VirtualFluidVeinPosition
import java.io.File

abstract class VirtualWorldCache {

    companion object {
        const val DIR_ORES_L0 = "ores_0"
        const val DIR_ORES_L1 = "ores_1"
        const val DIR_FLUIDS = "fluids"
        const val DIR_OBJECTS = "objects"
    }

    protected val dimensions: HashMap<Int, DimensionCache> = HashMap()

    private var needsSaving = false
    protected var oreVeinCacheDirectory: File? = null
    protected var undergroundFluidCacheDirectory: File? = null
    private var isLoaded = false

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
        val fileTarget = File(file, "$dirName.json")
        if (!fileTarget.canRead()) fileTarget.createNewFile()
        return gson.fromJson(fileTarget.readText(), JsonObject::class.java)
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
        val fileTarget = File(dimFolder, "$dirName.json")
        if (!fileTarget.canRead()) fileTarget.createNewFile()
        fileTarget.writeText(cache.toString())
    }

    private fun getOrCreateCache(dimId: Int): DimensionCache {
        var dimension = dimensions[dimId]

        if (dimension == null) {
            dimension = DimensionCache(dimId)
            dimensions[dimId] = dimension
        }
        return dimension
    }

    fun putOre(layer: Int, veinPosition: CacheOreVein) {
        getOrCreateCache(veinPosition.dimension).putOre(layer, veinPosition)
    }

    fun getOre(layer: Int, dimId: Int, x: Int, z: Int): CacheOreVein? {
        return getOrCreateCache(dimId).getOreVein(layer, x, z)
    }

    fun putFluid(veinPosition: VirtualFluidVeinPosition) {
        getOrCreateCache(veinPosition.dimId).putFluid(veinPosition)
    }

    fun getFluid(dimId: Int, x: Int, z: Int): VirtualFluidVeinPosition? {
        return getOrCreateCache(dimId).getFluidVein(x, z)
    }

    fun putObjectElement(element: CacheObjectChunk.ObjectElement, dimId: Int, x: Int, z: Int) {
        getOrCreateCache(dimId).putObjectElement(element, x, z)
    }

    fun putObjectChunk(obj: CacheObjectChunk, dimId: Int, x: Int, z: Int) {
        getOrCreateCache(dimId).putObjectChunk(obj, x, z)
    }

    fun getObjectChunk(dimId: Int, x: Int, z: Int): CacheObjectChunk? {
        return getOrCreateCache(dimId).getObjectChunk(x, z)
    }

    fun removeObjectChunk(element: CacheObjectChunk.ObjectElement, dimId: Int, x: Int, z: Int) {
        return getOrCreateCache(dimId).removeObjectChunk(element, x, z)
    }
}
