package space.impact.virtual_world.extras

import com.google.gson.*
import space.impact.virtual_world.api.TypeFluidVein
import java.lang.reflect.Type

class TypeFluidVeinSerialized : JsonSerializer<TypeFluidVein>, JsonDeserializer<TypeFluidVein> {

    override fun serialize(src: TypeFluidVein, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.ordinal)
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): TypeFluidVein {
        return TypeFluidVein.values().first { it.ordinal == json.asInt }
    }
}