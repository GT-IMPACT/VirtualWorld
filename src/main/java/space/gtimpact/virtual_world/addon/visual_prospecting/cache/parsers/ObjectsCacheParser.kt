package space.gtimpact.virtual_world.addon.visual_prospecting.cache.parsers

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import journeymap.client.model.BlockCoordIntPair
import space.gtimpact.virtual_world.addon.visual_prospecting.DimensionCache.Companion.keyOfCoordsBlock
import space.gtimpact.virtual_world.addon.visual_prospecting.cache.CacheObjectPoint
import space.gtimpact.virtual_world.util.ItemStackJsonUtil

fun JsonObject?.read(
    customObject: HashMap<BlockCoordIntPair, CacheObjectPoint>,
    dimId: Int,
) {
    runCatching {
        customObject.clear()
        customObject += this?.getAsJsonArray("points")?.associate { point ->
            val pointXPos = point.asJsonObject.getAsJsonPrimitive("x").asInt
            val pointZPos = point.asJsonObject.getAsJsonPrimitive("z").asInt

            val elements = point.asJsonObject.getAsJsonArray("elements").mapNotNull { element ->
                runCatching {
                    val name = element.asJsonObject.getAsJsonPrimitive("name").asString
                    val stack = element.asJsonObject.getAsJsonPrimitive("stack").asString

                    CacheObjectPoint.ObjectElement(
                        name = name,
                        stack = ItemStackJsonUtil.jsonToItemStack(stack),
                    )
                }.getOrNull()
            }

            val coords = keyOfCoordsBlock(pointXPos, pointZPos)

            coords to CacheObjectPoint(
                elements = elements,
                coords = coords,
                dimId = dimId,
            )
        }.orEmpty()
    }.getOrElse {
        it.printStackTrace()
    }
}

fun HashMap<BlockCoordIntPair, CacheObjectPoint>.write(dimId: Int): JsonObject {
    val json = JsonObject()
    val jsonArray = JsonArray()

    this.forEach { (block, data) ->
        val points = JsonObject()

        points.addProperty("x", block.x)
        points.addProperty("z", block.z)

        val elements = JsonArray()
        data.elements.forEach { (name, stack) ->
            val element = JsonObject()

            element.addProperty("name", name)
            element.addProperty("stack", ItemStackJsonUtil.itemStackToJson(stack))

            elements.add(element)
        }
        points.add("elements", elements)
        jsonArray.add(points)
    }

    json.addProperty("dim", dimId)
    json.add("points", jsonArray)

    return json
}
