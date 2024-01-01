package space.gtimpact.virtual_world.api

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.chunk.Chunk
import space.gtimpact.virtual_world.api.ResourceGenerator.generateResources
import space.gtimpact.virtual_world.api.ResourceGenerator.getVeinChunks
import space.gtimpact.virtual_world.api.VirtualAPI.VIRTUAL_ORES
import space.gtimpact.virtual_world.api.VirtualAPI.getVirtualFluidVeinById
import space.gtimpact.virtual_world.api.VirtualAPI.getVirtualOreVeinById
import space.gtimpact.virtual_world.common.world.IModifiableChunk
import space.gtimpact.virtual_world.extras.NBT

data class OreVeinCount(
    val vein: VirtualOreVein,
    val size: Int,
)

data class FluidVeinCount(
    val vein: VirtualFluidVein,
    val size: Int,
    val typeVein: TypeFluidVein,
)

fun Chunk.getOreLayer0(): OreVeinCount? {
    if (this !is IModifiableChunk) return null

    generateResources()

    val tag = getNbt(NBT.ORE_LAYER_0) ?: return null

    val vein = tag.getInteger(NBT.TYPE_ID).let { id ->
        VIRTUAL_ORES.find { it.id == id }
    } ?: return null

    return OreVeinCount(
        vein = vein,
        size = tag.getInteger(NBT.SIZE),
    )
}

fun Chunk.getOreLayer1(): OreVeinCount? {
    if (this !is IModifiableChunk) return null

    generateResources()

    val tag = getNbt(NBT.ORE_LAYER_1) ?: return null

    val type = tag.getInteger(NBT.TYPE_ID).let { id ->
        getVirtualOreVeinById(id)
    } ?: return null

    return OreVeinCount(
        vein = type,
        size = tag.getInteger(NBT.SIZE),
    )
}

fun Chunk.getFluidLayer(): FluidVeinCount? {
    if (this !is IModifiableChunk) return null

    generateResources()

    val tag = getNbt(NBT.FLUID_LAYER) ?: return null
    val type = getVirtualFluidVeinById(tag.getInteger(NBT.TYPE_ID)) ?: return null

    return FluidVeinCount(
        vein = type,
        size = tag.getInteger(NBT.SIZE),
        typeVein = TypeFluidVein.values()
            .find { it.name == tag.getString(NBT.TYPE_VEIN) } ?: return null
    )
}

fun Chunk.saveOreLayer0(type: Int, size: Int) {
    if (this is IModifiableChunk) {
        val tag = getNbt(NBT.ORE_LAYER_0) ?: NBTTagCompound()

        tag.setInteger(NBT.TYPE_ID, type)
        tag.setInteger(NBT.SIZE, size)

        setNbt(tag, NBT.ORE_LAYER_0)
    }
}

fun Chunk.saveOreLayer1(type: Int, size: Int) {
    if (this is IModifiableChunk) {
        val tag = getNbt(NBT.ORE_LAYER_1) ?: NBTTagCompound()

        tag.setInteger(NBT.TYPE_ID, type)
        tag.setInteger(NBT.SIZE, size)

        setNbt(tag, NBT.ORE_LAYER_1)
    }
}

fun Chunk.saveFluidLayer(type: Int, size: Int, typeVein: TypeFluidVein) {
    if (this is IModifiableChunk) {
        val tag = getNbt(NBT.FLUID_LAYER) ?: NBTTagCompound()

        tag.setInteger(NBT.TYPE_ID, type)
        tag.setInteger(NBT.SIZE, size)
        tag.setString(NBT.TYPE_VEIN, typeVein.name)

        setNbt(tag, NBT.FLUID_LAYER)
    }
}

fun Chunk.extractOreFromChunk(layer: Int, amount: Int): OreVeinCount? {
    return when (layer) {
        0 -> {
            val layer0 = getOreLayer0() ?: return null
            val newSize = layer0.size - amount
            saveOreLayer0(layer0.vein.id, newSize)
            OreVeinCount(layer0.vein, newSize)
        }

        1 -> {
            val layer1 = getOreLayer1() ?: return null
            val newSize = layer1.size - amount
            saveOreLayer1(layer1.vein.id, newSize)
            OreVeinCount(layer1.vein, newSize)
        }

        else -> return null
    }
}

fun Chunk.extractOreFormVein(layer: Int, amount: Int): OreVeinCount? {

    val chunks = getVeinChunks()

    val veins = chunks.mapNotNull { ch ->
        val chunk = worldObj.getChunkFromChunkCoords(ch.chunkXPos, ch.chunkZPos)
        when (layer) {
            0 -> chunk.getOreLayer0()
            1 -> chunk.getOreLayer1()
            else -> null
        }
    }

    val veinCount = veins.firstOrNull()?.let { vein ->
        OreVeinCount(
            vein = vein.vein,
            size = veins.sumOf { it.size } - veins.size * amount
        )
    }

    if (veinCount != null) {
        chunks.forEach { ch ->
            val chunk = worldObj.getChunkFromChunkCoords(ch.chunkXPos, ch.chunkZPos)
            chunk.extractOreFromChunk(layer, amount)
        }
    }

    return veinCount
}

fun Chunk.extractFluidFromChunk(amount: Int): FluidVeinCount? {

    val layer = getFluidLayer() ?: return null
    val newSize = layer.size - amount

    saveFluidLayer(layer.vein.id, newSize, layer.typeVein)

    return FluidVeinCount(layer.vein, newSize, layer.typeVein)
}

fun Chunk.extractFluidFormVein(amount: Int): FluidVeinCount? {

    val chunks = getVeinChunks()

    val veins = chunks.mapNotNull { ch ->
        val chunk = worldObj.getChunkFromChunkCoords(ch.chunkXPos, ch.chunkZPos)
        chunk.getFluidLayer()
    }

    val veinCount = veins.firstOrNull()?.let { vein ->
        FluidVeinCount(
            vein = vein.vein,
            size = veins.sumOf { it.size } - veins.size * amount,
            typeVein = vein.typeVein,
        )
    }

    if (veinCount != null) {
        chunks.forEach { ch ->
            val chunk = worldObj.getChunkFromChunkCoords(ch.chunkXPos, ch.chunkZPos)
            chunk.extractFluidFromChunk(amount)
        }
    }
    return veinCount
}

fun IModifiableChunk.hasGenerate(): Boolean {
    return getNbt(NBT.ORE_LAYER_0) == null && getNbt(NBT.ORE_LAYER_1) == null && getNbt(NBT.FLUID_LAYER) == null
}
