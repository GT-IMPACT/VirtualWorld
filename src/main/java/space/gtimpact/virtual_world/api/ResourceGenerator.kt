package space.gtimpact.virtual_world.api

import net.minecraft.world.ChunkCoordIntPair
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import space.gtimpact.virtual_world.api.fluids.ChunkFluid
import space.gtimpact.virtual_world.api.fluids.VeinFluid
import space.gtimpact.virtual_world.api.ores.ChunkOre
import space.gtimpact.virtual_world.api.ores.VeinOre
import space.gtimpact.virtual_world.common.world.IModifiableChunk
import java.util.ArrayList
import java.util.HashMap
import kotlin.random.Random

object ResourceGenerator {

    private const val SHIFT_REGION_FROM_CHUNK = 5
    private const val SHIFT_VEIN_FROM_REGION = 3
    private const val SHIFT_CHUNK_FROM_VEIN = 2
    private const val CHUNK_COUNT_IN_VEIN_COORDINATE = 4
    private const val VEIN_COUNT_IN_REGIN_COORDINATE = 8

    fun Chunk.generateResources() {

        if (this is IModifiableChunk) {

            val reg = RegionRes(
                xRegion = xPosition shr SHIFT_REGION_FROM_CHUNK,
                zRegion = zPosition shr SHIFT_REGION_FROM_CHUNK,
                dim = worldObj.provider.dimensionId
            )

            if (hasGenerate()) {
                reg.generateOreLayers(worldObj)
                reg.generateFluidLayers(worldObj)
            }
        }
    }

    // === ORES === //

    private fun RegionRes.generateOreLayers(world: World) {
        for (layer in 0 until VirtualAPI.LAYERS_VIRTUAL_ORES) {
            val rawVeins = ArrayList<VeinOre>()
            for (xx in 0 until VEIN_COUNT_IN_REGIN_COORDINATE) {
                for (zz in 0 until VEIN_COUNT_IN_REGIN_COORDINATE) {
                    VirtualAPI.getRandomVirtualOre(layer, dim)?.also { ore ->
                        VeinOre(
                            xVein = (xRegion shl SHIFT_VEIN_FROM_REGION) + xx,
                            zVein = (zRegion shl SHIFT_VEIN_FROM_REGION) + zz,
                            oreId = ore.id,
                        ).also { vein ->
                            vein.generate(ore)
                            rawVeins += vein
                        }
                    }
                }
            }
            this.oreVeins[layer] = rawVeins
        }

        oreVeins.forEach { (layer, veins) ->
            veins.forEach { vein ->
                vein.oreChunks.forEach { chunk ->
                    val ch = world.getChunkFromChunkCoords(chunk.x, chunk.z)
                    if (ch is IModifiableChunk) {
                        when (layer) {
                            0 -> ch.saveOreLayer0(vein.oreId, chunk.size)

                            1 -> ch.saveOreLayer1(vein.oreId, chunk.size)
                        }
                    }
                }
            }
        }
    }

    private fun VeinOre.generate(ore: VirtualOreVein) {
        for (x in 0 until CHUNK_COUNT_IN_VEIN_COORDINATE) {
            for (z in 0 until CHUNK_COUNT_IN_VEIN_COORDINATE) {
                ChunkOre(
                    x = (xVein shl SHIFT_CHUNK_FROM_VEIN) + x,
                    z = (zVein shl SHIFT_CHUNK_FROM_VEIN) + z,
                ).apply {
                    if (!ore.isHidden && ore.rangeSize.last > 0)
                        size = Random.nextInt(ore.rangeSize.first * 1000, ore.rangeSize.last * 1000)
                    oreChunks += this
                }
            }
        }
    }

    // === FLUIDS === //

    private fun RegionRes.generateFluidLayers(world: World) {
        for (xx in 0 until VEIN_COUNT_IN_REGIN_COORDINATE) {
            for (zz in 0 until VEIN_COUNT_IN_REGIN_COORDINATE) {
                VirtualAPI.getRandomVirtualFluid(dim)?.also { ore ->
                    this.fluidVeins += VeinFluid(
                        xVein = (xRegion shl SHIFT_VEIN_FROM_REGION) + xx,
                        zVein = (zRegion shl SHIFT_VEIN_FROM_REGION) + zz,
                        fluidId = ore.id,
                    ).also { vein ->
                        vein.generate(ore)
                    }
                }
            }
        }

        fluidVeins.forEach { vein ->
            vein.oreChunks.forEach { chunk ->
                val ch = world.getChunkFromChunkCoords(chunk.x, chunk.z)
                if (ch is IModifiableChunk) {
                    ch.saveFluidLayer(vein.fluidId, chunk.size, chunk.type)
                }
            }
        }
    }

    private fun VeinFluid.generate(vein: VirtualFluidVein) {

        val sizeVein = if (!vein.isHidden && vein.rangeSize.last > 0)
            Random.nextInt(vein.rangeSize.first * 1000, vein.rangeSize.last * 1000)
        else 0

        for (x in 0 until CHUNK_COUNT_IN_VEIN_COORDINATE) {
            for (z in 0 until CHUNK_COUNT_IN_VEIN_COORDINATE) {
                ChunkFluid(
                    x = (xVein shl SHIFT_CHUNK_FROM_VEIN) + x,
                    z = (zVein shl SHIFT_CHUNK_FROM_VEIN) + z,
                    type = TypeFluidVein.values().random(),
                ).apply {
                    size = sizeVein
                    oreChunks += this
                }
            }
        }
    }

    // === GENERAL === //

    private fun RegionRes.getVeinChunks(chunkX: Int, chunkZ: Int): List<ChunkCoordIntPair> {
        val list = mutableListOf<ChunkCoordIntPair>()

        var vein: VeinOre? = null

        loop@ for (xxx in 0 until VEIN_COUNT_IN_REGIN_COORDINATE) {
            for (zzz in 0 until VEIN_COUNT_IN_REGIN_COORDINATE) {

                val veinOre = VeinOre(
                    xVein = (xRegion shl SHIFT_VEIN_FROM_REGION) + xxx,
                    zVein = (zRegion shl SHIFT_VEIN_FROM_REGION) + zzz,
                    oreId = -1,
                )

                for (xx in 0 until CHUNK_COUNT_IN_VEIN_COORDINATE) {
                    for (zz in 0 until CHUNK_COUNT_IN_VEIN_COORDINATE) {

                        val chunk = ChunkOre(
                            x = (veinOre.xVein shl SHIFT_CHUNK_FROM_VEIN) + xx,
                            z = (veinOre.zVein shl SHIFT_CHUNK_FROM_VEIN) + zz,
                        )

                        veinOre.oreChunks += chunk

                        if (chunk.x == chunkX && chunk.z == chunkZ) {
                            vein = veinOre
                            break@loop
                        }
                    }
                }
            }
        }

        if (vein != null) {
            list += vein.oreChunks.map { ChunkCoordIntPair(it.x, it.z) }
        }

        return list
    }

    fun Chunk.getVeinChunks(): List<ChunkCoordIntPair> {
        return RegionRes(
            xPosition shr SHIFT_REGION_FROM_CHUNK,
            zPosition shr SHIFT_REGION_FROM_CHUNK,
            0,
        ).getVeinChunks(xPosition, zPosition)
    }
}

data class RegionRes(
    val xRegion: Int,
    val zRegion: Int,
    val dim: Int,
) {
    val oreVeins: HashMap<Int, ArrayList<VeinOre>> = HashMap()
    val fluidVeins: ArrayList<VeinFluid> = arrayListOf()
}
