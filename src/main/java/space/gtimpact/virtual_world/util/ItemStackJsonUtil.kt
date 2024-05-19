package space.gtimpact.virtual_world.util

import com.google.gson.*
import net.minecraft.item.ItemStack
import net.minecraft.item.Item
import net.minecraft.nbt.JsonToNBT
import net.minecraft.nbt.NBTException
import net.minecraft.nbt.NBTTagCompound
import java.lang.reflect.Type

object ItemStackJsonUtil {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(ItemStack::class.java, ItemStackSerializer())
        .registerTypeAdapter(ItemStack::class.java, ItemStackDeserializer())
        .setPrettyPrinting()
        .create()

    fun itemStackToJson(itemStack: ItemStack): String {
        return gson.toJson(itemStack)
    }

    fun jsonToItemStack(json: String): ItemStack {
        return gson.fromJson(json, ItemStack::class.java)
    }

    class ItemStackSerializer : JsonSerializer<ItemStack> {
        override fun serialize(src: ItemStack, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val jsonObject = JsonObject()
            jsonObject.addProperty("item", Item.itemRegistry.getNameForObject(src.item).toString())
            jsonObject.addProperty("count", src.stackSize)
            jsonObject.addProperty("damage", src.itemDamage)
            return jsonObject
        }
    }

    class ItemStackDeserializer : JsonDeserializer<ItemStack> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ItemStack {
            val jsonObject = json.asJsonObject
            val itemName = jsonObject.get("item").asString
            val item = Item.itemRegistry.getObject(itemName) as? Item
                ?: throw JsonParseException("Unknown item: $itemName")

            val count = jsonObject.get("count").asInt
            val damage = jsonObject.get("damage").asInt
            val itemStack = ItemStack(item, count, damage)

            return itemStack
        }
    }
}
