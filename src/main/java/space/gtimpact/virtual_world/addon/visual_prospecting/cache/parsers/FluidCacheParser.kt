package space.gtimpact.virtual_world.addon.visual_prospecting.cache.parsers

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.world.ChunkCoordIntPair
import space.gtimpact.virtual_world.addon.visual_prospecting.cache.CacheFluidVein
import space.gtimpact.virtual_world.addon.visual_prospecting.cache.CacheFluidVeinChunk
import space.gtimpact.virtual_world.api.VirtualAPI

fun JsonObject?.read(
    fluidChunks: HashMap<ChunkCoordIntPair, CacheFluidVein>,
    dimId: Int,
) {
    runCatching {
        fluidChunks.clear()
        fluidChunks += this?.getAsJsonArray("veins")?.associate { chunk ->
            val chunkXPos = chunk.asJsonObject.getAsJsonPrimitive("x").asInt
            val chunkZPos = chunk.asJsonObject.getAsJsonPrimitive("z").asInt

            val dataX = chunk.asJsonObject.getAsJsonPrimitive("xData").asInt
            val dataZ = chunk.asJsonObject.getAsJsonPrimitive("zData").asInt
            val veinId = chunk.asJsonObject.getAsJsonPrimitive("veinId").asInt

            val chunks = chunk.asJsonObject.getAsJsonArray("chunks").mapNotNull {
                runCatching {
                    CacheFluidVeinChunk(
                        x = 0, // not read, because calculated from dataX
                        z = 0, // not read, because calculated from dataX
                        size = it.asJsonObject.getAsJsonPrimitive("s").asInt
                    )
                }.getOrNull()
            }

            ChunkCoordIntPair(
                chunkXPos,
                chunkZPos,
            ) to CacheFluidVein(
                veinId = veinId,
                x = dataX,
                z = dataZ,
                chunks = chunks,
            ).apply {
                this.dimension = dimId
                this.vein = VirtualAPI.getRegisterFluids().first { it.id == veinId }
            }
        }.orEmpty()
    }.getOrElse {
        it.printStackTrace()
    }
}

fun HashMap<ChunkCoordIntPair, CacheFluidVein>.write(dimId: Int): JsonObject {
    val json = JsonObject()
    val veins = JsonArray()

    this.forEach { (chunkC, data) ->
        val vein = JsonObject()

        vein.addProperty("x", chunkC.chunkXPos)
        vein.addProperty("z", chunkC.chunkZPos)

        vein.addProperty("xData", data.x)
        vein.addProperty("zData", data.z)
        vein.addProperty("veinId", data.veinId)

        val chunks = JsonArray()

        data.chunks.forEach { chunkData ->
            val chunkDataJson = JsonObject()
            chunkDataJson.addProperty("s", chunkData.size)

            chunks.add(chunkDataJson)
        }

        vein.add("chunks", chunks)

        veins.add(vein)
    }

    json.addProperty("dim", dimId)
    json.add("veins", veins)

    return json
}
