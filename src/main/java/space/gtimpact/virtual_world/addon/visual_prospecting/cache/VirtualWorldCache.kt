package space.gtimpact.virtual_world.addon.visual_prospecting.cache

import com.google.gson.Gson
import com.google.gson.JsonObject
import space.gtimpact.virtual_world.addon.visual_prospecting.DimensionCache
import space.gtimpact.virtual_world.addon.visual_prospecting.PacketDataOreVein
import space.gtimpact.virtual_world.addon.visual_prospecting.VirtualFluidVeinPosition
import java.io.File

abstract class VirtualWorldCache {

    companion object {
        const val DIR_ORES_L0 = "ores_0"
        const val DIR_ORES_L1 = "ores_1"
        const val DIR_FLUIDS = "fluids"
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

                val fileLayer0 = File(file, "$DIR_ORES_L0.json")
                if (!fileLayer0.canRead()) fileLayer0.createNewFile()
                val layer0 = gson.fromJson(fileLayer0.readText(), JsonObject::class.java)

                val fileLayer1 = File(file, "$DIR_ORES_L1.json")
                if (!fileLayer1.canRead()) fileLayer1.createNewFile()
                val layer1 = gson.fromJson(fileLayer1.readText(), JsonObject::class.java)

                val fileFluids = File(file, "$DIR_FLUIDS.json")
                if (!fileFluids.canRead()) fileFluids.createNewFile()
                val fluids = gson.fromJson(fileFluids.readText(), JsonObject::class.java)

                dimension.load(layer0, layer1, fluids)
                dimensions[dim] = dimension
            }

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun saveCache(worldId: String) {
        try {
            val worldCacheDirectory = File(getStorageDirectory(), worldId)

            dimensions.forEach { (dimId, cache) ->

                val dimFolder = File(worldCacheDirectory, "DIM$dimId")
                dimFolder.mkdirs()

                val layer0 = cache.saveOreChunksLayer0()

                val fileLayer0 = File(dimFolder, "$DIR_ORES_L0.json")
                if (!fileLayer0.canRead()) fileLayer0.createNewFile()
                fileLayer0.writeText(layer0.toString())

                val layer1 = cache.saveOreChunksLayer1()
                val fileLayer1 = File(dimFolder, "$DIR_ORES_L1.json")
                if (!fileLayer1.canRead()) fileLayer1.createNewFile()
                fileLayer1.writeText(layer1.toString())

                val fluids = cache.saveFluidsChunks()
                val fileFluids = File(dimFolder, "$DIR_FLUIDS.json")
                if (!fileFluids.canRead()) fileFluids.createNewFile()
                fileFluids.writeText(fluids.toString())

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun putOre(layer: Int, veinPosition: PacketDataOreVein) {
        var dimension = dimensions[veinPosition.dimension]

        if (dimension == null) {
            dimension = DimensionCache(veinPosition.dimension)
            dimensions[veinPosition.dimension] = dimension
        }

        dimension.putOre(layer, veinPosition)
    }

    fun getOre(layer: Int, dimId: Int, x: Int, z: Int): PacketDataOreVein? {
        return dimensions[dimId]?.getOreVein(layer, x, z)
    }

    fun putFluid(veinPosition: VirtualFluidVeinPosition) {
        var dimension = dimensions[veinPosition.dimId]

        if (dimension == null) {
            dimension = DimensionCache(veinPosition.dimId)
            dimensions[veinPosition.dimId] = dimension
        }

        dimension.putFluid(veinPosition)
    }

    fun getFluid(dimId: Int, x: Int, z: Int): VirtualFluidVeinPosition? {
        return dimensions[dimId]?.getFluidVein(x, z)
    }
}
