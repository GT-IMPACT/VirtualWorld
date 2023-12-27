package space.gtimpact.virtual_world.api

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.chunk.Chunk
import space.gtimpact.virtual_world.api.OreGenerator.generateRegion
import space.gtimpact.virtual_world.api.VirtualAPI.VIRTUAL_ORES
import space.gtimpact.virtual_world.api.VirtualAPI.getVirtualOreVeinById
import space.gtimpact.virtual_world.common.world.IModifiableChunk
import space.gtimpact.virtual_world.extras.NBT

data class OreVeinCount(
    val type: VirtualOreVein,
    val size: Int,
)

//TODO
data class FluidVeinCount(
    val type: VirtualFluidVein,
    val size: Int,
)

fun Chunk.getOreLayer0(): OreVeinCount? {
    if (this !is IModifiableChunk) return null

    generateIfEmpty()

    val tag = getNbt(NBT.ORE_LAYER_0) ?: return null

    val type = tag.getInteger(NBT.ORE_TYPE_ID).let { id ->
        VIRTUAL_ORES.find { it.id == id }
    } ?: return null

    return OreVeinCount(
        type = type,
        size = tag.getInteger(NBT.SIZE),
    )
}

fun Chunk.getOreLayer1(): OreVeinCount? {
    if (this !is IModifiableChunk) return null

    generateIfEmpty()

    val tag = getNbt(NBT.ORE_LAYER_1) ?: return null

    val type = tag.getInteger(NBT.ORE_TYPE_ID).let { id ->
        getVirtualOreVeinById(id)
    } ?: return null

    return OreVeinCount(
        type = type,
        size = tag.getInteger(NBT.SIZE),
    )
}

fun Chunk.generateIfEmpty() {
    if (!isGenerated) generateRegion()
}

private val Chunk.isGenerated
    get() = virtualState() == VirtualGeneratorState.GENERATED

fun Chunk.saveOreLayer0(type: Int, size: Int) {
    if (this is IModifiableChunk) {
        val tag = getNbt(NBT.ORE_LAYER_0) ?: NBTTagCompound()

        tag.setInteger(NBT.ORE_TYPE_ID, type)
        tag.setInteger(NBT.SIZE, size)

        setNbt(tag, NBT.ORE_LAYER_0)
    }
}

fun Chunk.saveOreLayer1(type: Int, size: Int) {
    if (this is IModifiableChunk) {
        val tag = getNbt(NBT.ORE_LAYER_1) ?: NBTTagCompound()

        tag.setInteger(NBT.ORE_TYPE_ID, type)
        tag.setInteger(NBT.SIZE, size)

        setNbt(tag, NBT.ORE_LAYER_1)
    }
}

fun Chunk.extractOreFromChunk(layer: Int, amount: Int): OreVeinCount? {
    return when (layer) {
        0 -> {
            val layer0 = getOreLayer0() ?: return null
            val newSize = layer0.size - amount
            saveOreLayer0(layer0.type.id, newSize)
            OreVeinCount(layer0.type, newSize)
        }

        1 -> {
            val layer1 = getOreLayer1() ?: return null
            val newSize = layer1.size - amount
            saveOreLayer1(layer1.type.id, newSize)
            OreVeinCount(layer1.type, newSize)
        }

        else -> return null
    }
}

fun Chunk.virtualState(): VirtualGeneratorState = when {
    this !is IModifiableChunk -> VirtualGeneratorState.FAIL
    getNbt(NBT.ORE_LAYER_0) != null && getNbt(NBT.ORE_LAYER_1) != null -> VirtualGeneratorState.GENERATED
    else -> VirtualGeneratorState.EMPTY
}

enum class VirtualGeneratorState {
    GENERATED,
    EMPTY,
    FAIL,
}
