package space.gtimpact.virtual_world.addon.visual_prospecting

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.sinthoras.visualprospecting.Utils
import net.minecraft.world.ChunkCoordIntPair
import space.gtimpact.virtual_world.addon.visual_prospecting.cache.CacheObjectChunk
import space.gtimpact.virtual_world.addon.visual_prospecting.cache.CacheOreVein
import space.gtimpact.virtual_world.addon.visual_prospecting.cache.CacheOreVeinChunk
import space.gtimpact.virtual_world.api.ResourceGenerator
import space.gtimpact.virtual_world.api.VirtualAPI
import space.gtimpact.virtual_world.api.VirtualFluidVein
import space.gtimpact.virtual_world.api.VirtualOreVein
import space.gtimpact.virtual_world.util.ItemStackJsonUtil

class DimensionCache(private val dimId: Int) {

    private val oreChunksLayer0 = HashMap<ChunkCoordIntPair, CacheOreVein>()
    private val oreChunksLayer1 = HashMap<ChunkCoordIntPair, CacheOreVein>()
    private val fluidChunks = HashMap<ChunkCoordIntPair, VirtualFluidVeinPosition>()
    private val customObject = HashMap<ChunkCoordIntPair, CacheObjectChunk>()

    fun saveOreChunksLayer0(): JsonElement {
        val json = JsonObject()
        val jsonArray = JsonArray()

        oreChunksLayer0.forEach { (chunkC, data) ->
            val chunk = JsonObject()

            chunk.addProperty("chunkXPos", chunkC.chunkXPos)
            chunk.addProperty("chunkZPos", chunkC.chunkZPos)

            chunk.addProperty("dataX", data.x)
            chunk.addProperty("dataZ", data.z)
            chunk.addProperty("veinId", data.veinId)

            val chunkDataJsons = JsonArray()

            data.chunks.forEach { chunkData ->
                val chunkDataJson = JsonObject()
                chunkDataJson.addProperty("size", chunkData.size)
                chunkDataJsons.add(chunkDataJson)
            }

            chunk.add("chunks", chunkDataJsons)

            jsonArray.add(chunk)
        }

        json.addProperty("dim", dimId)
        json.add("chunks", jsonArray)

        return json
    }

    fun saveOreChunksLayer1(): JsonElement {
        val json = JsonObject()
        val jsonArray = JsonArray()

        oreChunksLayer1.forEach { (chunkC, data) ->
            val chunk = JsonObject()

            chunk.addProperty("chunkXPos", chunkC.chunkXPos)
            chunk.addProperty("chunkZPos", chunkC.chunkZPos)

            chunk.addProperty("dataX", data.x)
            chunk.addProperty("dataZ", data.z)
            chunk.addProperty("veinId", data.veinId)

            val chunkDataJsons = JsonArray()

            data.chunks.forEach { chunkData ->
                val chunkDataJson = JsonObject()
                chunkDataJson.addProperty("size", chunkData.size)
                chunkDataJsons.add(chunkDataJson)
            }

            chunk.add("chunks", chunkDataJsons)

            jsonArray.add(chunk)
        }

        json.addProperty("dim", dimId)
        json.add("chunks", jsonArray)

        return json
    }

    fun saveFluidsChunks(): JsonElement {
        val json = JsonObject()
        val jsonArray = JsonArray()

        fluidChunks.forEach { (chunkC, data) ->
            val chunk = JsonObject()

            chunk.addProperty("chunkXPos", chunkC.chunkXPos)
            chunk.addProperty("chunkZPos", chunkC.chunkZPos)

            chunk.addProperty("dataX", data.x)
            chunk.addProperty("dataZ", data.z)
            chunk.addProperty("veinId", data.vein.id)
            chunk.addProperty("size", data.size)

            jsonArray.add(chunk)
        }

        json.addProperty("dim", dimId)
        json.add("chunks", jsonArray)

        return json
    }

    fun saveObjects(): JsonElement {
        val json = JsonObject()
        val jsonArray = JsonArray()

        customObject.forEach { (chunkC, data) ->
            val chunk = JsonObject()

            chunk.addProperty("chunkXPos", chunkC.chunkXPos)
            chunk.addProperty("chunkZPos", chunkC.chunkZPos)

            val elements = JsonArray()
            data.elements.forEach { (name, stack) ->
                val element = JsonObject()

                element.addProperty("name", name)
                element.addProperty("stack", ItemStackJsonUtil.itemStackToJson(stack))

                elements.add(element)
            }
            chunk.add("elements", elements)
            jsonArray.add(chunk)
        }

        json.addProperty("dim", dimId)
        json.add("chunks", jsonArray)

        return json
    }

    fun load(layer0: JsonObject?, layer1: JsonObject?, fluids: JsonObject?, customObjects: JsonObject?) = runCatching {
        if (layer0 != null) {
            oreChunksLayer0.clear()
            oreChunksLayer0 += layer0.getAsJsonArray("chunks").associate { chunk ->
                val chunkXPos = chunk.asJsonObject.getAsJsonPrimitive("chunkXPos").asInt
                val chunkZPos = chunk.asJsonObject.getAsJsonPrimitive("chunkZPos").asInt

                val dataX = chunk.asJsonObject.getAsJsonPrimitive("dataX").asInt
                val dataZ = chunk.asJsonObject.getAsJsonPrimitive("dataZ").asInt
                val veinId = chunk.asJsonObject.getAsJsonPrimitive("veinId").asInt

                val chunkss = chunk.asJsonObject.getAsJsonArray("chunks").map {
                    CacheOreVeinChunk(x = 0, z = 0, size = it.asJsonObject.getAsJsonPrimitive("size").asInt)
                }

                ChunkCoordIntPair(
                    chunkXPos,
                    chunkZPos,
                ) to CacheOreVein(
                    veinId = veinId,
                    x = dataX,
                    z = dataZ,
                    chunks = chunkss,
                ).apply {
                    this.dimension = dimId
                    this.vein = VirtualAPI.getRegisterOres().find { it.id == veinId }
                }
            }
        }
        if (layer1 != null) {
            oreChunksLayer1.clear()
            oreChunksLayer1 += layer1.getAsJsonArray("chunks").associate { chunk ->
                val chunkXPos = chunk.asJsonObject.getAsJsonPrimitive("chunkXPos").asInt
                val chunkZPos = chunk.asJsonObject.getAsJsonPrimitive("chunkZPos").asInt

                val dataX = chunk.asJsonObject.getAsJsonPrimitive("dataX").asInt
                val dataZ = chunk.asJsonObject.getAsJsonPrimitive("dataZ").asInt
                val veinId = chunk.asJsonObject.getAsJsonPrimitive("veinId").asInt

                val chunkss = chunk.asJsonObject.getAsJsonArray("chunks").map {
                    CacheOreVeinChunk(x = 0, z = 0, size = it.asJsonObject.getAsJsonPrimitive("size").asInt)
                }

                ChunkCoordIntPair(
                    chunkXPos,
                    chunkZPos,
                ) to CacheOreVein(
                    veinId = veinId,
                    x = dataX,
                    z = dataZ,
                    chunks = chunkss,
                ).apply {
                    this.dimension = dimId
                    this.vein = VirtualAPI.getRegisterOres().find { it.id == veinId }
                }
            }
        }

        if (fluids != null) {
            fluidChunks.clear()
            fluidChunks += fluids.getAsJsonArray("chunks").associate { chunk ->
                val chunkXPos = chunk.asJsonObject.getAsJsonPrimitive("chunkXPos").asInt
                val chunkZPos = chunk.asJsonObject.getAsJsonPrimitive("chunkZPos").asInt

                val dataX = chunk.asJsonObject.getAsJsonPrimitive("dataX").asInt
                val dataZ = chunk.asJsonObject.getAsJsonPrimitive("dataZ").asInt
                val veinId = chunk.asJsonObject.getAsJsonPrimitive("veinId").asInt
                val size = chunk.asJsonObject.getAsJsonPrimitive("size").asInt

                ChunkCoordIntPair(
                    chunkXPos,
                    chunkZPos,
                ) to VirtualFluidVeinPosition(
                    dimId = dimId,
                    x = dataX,
                    z = dataZ,
                    vein = VirtualAPI.getRegisterFluids().first { it.id == veinId },
                    size = size,
                )
            }
        }

        if (customObjects != null) {
            customObject.clear()
            customObject += customObjects.getAsJsonArray("chunks").associate { chunk ->
                val chunkXPos = chunk.asJsonObject.getAsJsonPrimitive("chunkXPos").asInt
                val chunkZPos = chunk.asJsonObject.getAsJsonPrimitive("chunkZPos").asInt

                val elements = chunk.asJsonObject.getAsJsonArray("elements").map { element ->

                    val name = element.asJsonObject.getAsJsonPrimitive("name").asString
                    val stack = element.asJsonObject.getAsJsonPrimitive("stack").asString

                    CacheObjectChunk.ObjectElement(
                        name = name,
                        stack = ItemStackJsonUtil.jsonToItemStack(stack),
                    )
                }

                val coords = keyOfCoords(chunkXPos, chunkZPos)

                coords to CacheObjectChunk(
                    elements = elements,
                    coords = coords,
                    dimId = dimId,
                )
            }
        }
    }

    private fun keyOfCoords(chunkX: Int, chunkZ: Int): ChunkCoordIntPair {
        return ChunkCoordIntPair(Utils.mapToCenterOreChunkCoord(chunkX), Utils.mapToCenterOreChunkCoord(chunkZ))
    }

    fun putOre(layer: Int, veinPosition: CacheOreVein) {
        val key = keyOfCoords(
            veinPosition.x shl ResourceGenerator.SHIFT_CHUNK_FROM_VEIN,
            veinPosition.z shl ResourceGenerator.SHIFT_CHUNK_FROM_VEIN,
        )
        when (layer) {
            0 -> oreChunksLayer0[key] = veinPosition
            1 -> oreChunksLayer1[key] = veinPosition
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

    fun putFluid(veinPosition: VirtualFluidVeinPosition) {
        val key = keyOfCoords(veinPosition.x, veinPosition.z)
        fluidChunks[key] = veinPosition
    }

    fun getFluidVein(x: Int, z: Int): VirtualFluidVeinPosition? {
        val key = keyOfCoords(x, z)
        return fluidChunks[key]
    }

    fun putObjectElement(element: CacheObjectChunk.ObjectElement, x: Int, z: Int) {
        val key = keyOfCoords(x, z)
        val objectCache = customObject.computeIfAbsent(key) { CacheObjectChunk(coords = key, dimId = dimId) }
        val updateCache = objectCache.copy(elements = objectCache.elements + element)
        customObject.replace(key, updateCache)
    }

    fun putObjectChunk(obj: CacheObjectChunk, x: Int, z: Int) {
        val key = keyOfCoords(x, z)
        customObject[key] = obj
    }

    fun getObjectChunk(x: Int, z: Int): CacheObjectChunk? {
        val key = keyOfCoords(x, z)
        return customObject[key]
    }

    fun removeObjectChunk(element: CacheObjectChunk.ObjectElement, x: Int, z: Int) {
        val key = keyOfCoords(x, z)
        customObject[key]?.also { cache ->
            customObject.replace(key, cache.copy(elements = cache.elements.mapNotNull { if (it.name == element.name) null else it }))
        }
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
