package space.gtimpact.virtual_world.addon.visual_prospecting

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.sinthoras.visualprospecting.Utils
import net.minecraft.world.ChunkCoordIntPair
import space.gtimpact.virtual_world.api.ResourceGenerator
import space.gtimpact.virtual_world.api.VirtualAPI
import space.gtimpact.virtual_world.api.VirtualFluidVein
import space.gtimpact.virtual_world.api.VirtualOreVein

class DimensionCache(private val dimId: Int) {

    private val oreChunksLayer0 = HashMap<ChunkCoordIntPair, PacketDataOreVein>()
    private val oreChunksLayer1 = HashMap<ChunkCoordIntPair, PacketDataOreVein>()
    private val fluidChunks = HashMap<ChunkCoordIntPair, VirtualFluidVeinPosition>()

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


    fun load(layer0: JsonObject?, layer1: JsonObject?, fluids: JsonObject?) = runCatching {
        if (layer0 != null) {
            oreChunksLayer0.clear()
            oreChunksLayer0 += layer0.getAsJsonArray("chunks").associate { chunk ->
                val chunkXPos = chunk.asJsonObject.getAsJsonPrimitive("chunkXPos").asInt
                val chunkZPos = chunk.asJsonObject.getAsJsonPrimitive("chunkZPos").asInt

                val dataX = chunk.asJsonObject.getAsJsonPrimitive("dataX").asInt
                val dataZ = chunk.asJsonObject.getAsJsonPrimitive("dataZ").asInt
                val veinId = chunk.asJsonObject.getAsJsonPrimitive("veinId").asInt

                val chunkss = chunk.asJsonObject.getAsJsonArray("chunks").map {
                    PacketDataOreVeinChunk(x = 0, z = 0, size = it.asJsonObject.getAsJsonPrimitive("size").asInt)
                }

                ChunkCoordIntPair(
                    chunkXPos,
                    chunkZPos,
                ) to PacketDataOreVein(
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
                    PacketDataOreVeinChunk(x = 0, z = 0, size = it.asJsonObject.getAsJsonPrimitive("size").asInt)
                }

                ChunkCoordIntPair(
                    chunkXPos,
                    chunkZPos,
                ) to PacketDataOreVein(
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
    }

    private fun getOreVeinKey(chunkX: Int, chunkZ: Int): ChunkCoordIntPair {
        return ChunkCoordIntPair(Utils.mapToCenterOreChunkCoord(chunkX), Utils.mapToCenterOreChunkCoord(chunkZ))
    }

    fun putOre(layer: Int, veinPosition: PacketDataOreVein) {
        val key = getOreVeinKey(
            veinPosition.x shl ResourceGenerator.SHIFT_CHUNK_FROM_VEIN,
            veinPosition.z shl ResourceGenerator.SHIFT_CHUNK_FROM_VEIN,
        )
        when (layer) {
            0 -> oreChunksLayer0[key] = veinPosition
            1 -> oreChunksLayer1[key] = veinPosition
        }
    }

    fun getOreVein(layer: Int, x: Int, z: Int): PacketDataOreVein? {
        val key = getOreVeinKey(x, z)
        return when (layer) {
            0 -> oreChunksLayer0[key]
            1 -> oreChunksLayer1[key]
            else -> null
        }
    }

    fun putFluid(veinPosition: VirtualFluidVeinPosition) {
        val key = getOreVeinKey(veinPosition.x, veinPosition.z)
        fluidChunks[key] = veinPosition
    }

    fun getFluidVein(x: Int, z: Int): VirtualFluidVeinPosition? {
        val key = getOreVeinKey(x, z)
        return fluidChunks[key]
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
