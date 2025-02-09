package space.gtimpact.virtual_world.addon.visual_prospecting.cache.parsers

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.world.ChunkCoordIntPair
import space.gtimpact.virtual_world.addon.visual_prospecting.cache.CacheOreVein
import space.gtimpact.virtual_world.addon.visual_prospecting.cache.CacheOreVeinChunk
import space.gtimpact.virtual_world.api.VirtualAPI

fun JsonObject?.read(
    oreChunksLayer: HashMap<ChunkCoordIntPair, CacheOreVein>,
    dimId: Int,
) {
    runCatching {
        oreChunksLayer.clear()
        oreChunksLayer += this?.getAsJsonArray("veins")?.associate { vein ->
            val chunkXPos = vein.asJsonObject.getAsJsonPrimitive("x").asInt
            val chunkZPos = vein.asJsonObject.getAsJsonPrimitive("z").asInt

            val dataX = vein.asJsonObject.getAsJsonPrimitive("xData").asInt
            val dataZ = vein.asJsonObject.getAsJsonPrimitive("zData").asInt
            val veinId = vein.asJsonObject.getAsJsonPrimitive("veinId").asInt

            val chunks = vein.asJsonObject.getAsJsonArray("chunks").mapNotNull {
                runCatching {
                    CacheOreVeinChunk(
                        x = 0, // not read, because calculated from dataX
                        z = 0, // not read, because calculated from dataX
                        size = it.asJsonObject.getAsJsonPrimitive("s").asInt
                    )
                }.getOrNull()
            }

            ChunkCoordIntPair(
                chunkXPos,
                chunkZPos,
            ) to CacheOreVein(
                veinId = veinId,
                x = dataX,
                z = dataZ,
                chunks = chunks,
            ).apply {
                this.dimension = dimId
                this.vein = VirtualAPI.getRegisterOres().find { it.id == veinId }
            }
        }.orEmpty()
    }.getOrElse {
        it.printStackTrace()
    }
}

fun HashMap<ChunkCoordIntPair, CacheOreVein>.write(dimId: Int): JsonObject {
    val json = JsonObject()
    val jsonArray = JsonArray()

    this.forEach { (chunkC, data) ->
        val vein = JsonObject()

        vein.addProperty("x", chunkC.chunkXPos)
        vein.addProperty("z", chunkC.chunkZPos)

        vein.addProperty("xData", data.x)
        vein.addProperty("zData", data.z)
        vein.addProperty("veinId", data.veinId)

        val chunkDataJsons = JsonArray()

        data.chunks.forEach { chunkData ->
            val chunkDataJson = JsonObject()
            chunkDataJson.addProperty("s", chunkData.size)

            chunkDataJsons.add(chunkDataJson)
        }

        vein.add("chunks", chunkDataJsons)

        jsonArray.add(vein)
    }

    json.addProperty("dim", dimId)
    json.add("veins", jsonArray)

    return json
}