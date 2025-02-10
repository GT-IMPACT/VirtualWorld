package space.gtimpact.virtual_world.api

import net.minecraft.world.ChunkCoordIntPair
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import space.gtimpact.virtual_world.api.VirtualAPI.resizeFluidVeins
import space.gtimpact.virtual_world.api.VirtualAPI.resizeOreVeins
import space.gtimpact.virtual_world.api.fluids.ChunkFluid
import space.gtimpact.virtual_world.api.fluids.VeinFluid
import space.gtimpact.virtual_world.api.ores.ChunkOre
import space.gtimpact.virtual_world.api.ores.VeinOre
import space.gtimpact.virtual_world.common.world.IModifiableChunk
import kotlin.random.Random

object ResourceGenerator {

    internal const val SHIFT_REGION_FROM_CHUNK = 5
    internal const val SHIFT_VEIN_FROM_REGION = 3
    internal const val SHIFT_CHUNK_FROM_VEIN = 2
    internal const val CHUNK_COUNT_IN_VEIN_COORDINATE = 4
    internal const val VEIN_COUNT_IN_REGIN_COORDINATE = 8

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

    private val oresChanceSort by lazy {
        resizeOreVeins.mapValues { (dim, layers) ->
            layers.mapValues { (layer, veins) ->
                veins.flatMap { vein ->
                    List(vein.weight.toInt()) { vein }
                }
            }
        }
    }

    private fun RegionRes.generateOreLayers(world: World) {

        val dimVeins = oresChanceSort[dim].orEmpty()

        for (layer in 0 until VirtualAPI.LAYERS_VIRTUAL_ORES) {

            val layerVeins = dimVeins[layer].orEmpty()

            val rawVeins = ArrayList<VeinOre>()
            val regionSeed = world.seed xor (xRegion.toLong() shl 32) xor zRegion.toLong()
            val rand = Random(regionSeed)

            for (xx in 0 until VEIN_COUNT_IN_REGIN_COORDINATE) {
                for (zz in 0 until VEIN_COUNT_IN_REGIN_COORDINATE) {

                    if (rand.nextDouble() < 0.85) {

                        val oreVein = layerVeins[rand.nextInt(layerVeins.size)]

                        VeinOre(
                            xVein = (xRegion shl SHIFT_VEIN_FROM_REGION) + xx,
                            zVein = (zRegion shl SHIFT_VEIN_FROM_REGION) + zz,
                            oreId = oreVein.id,
                        ).also { vein ->

                            for (x in 0 until CHUNK_COUNT_IN_VEIN_COORDINATE) {
                                for (z in 0 until CHUNK_COUNT_IN_VEIN_COORDINATE) {

                                    ChunkOre(
                                        x = (vein.xVein shl SHIFT_CHUNK_FROM_VEIN) + x,
                                        z = (vein.zVein shl SHIFT_CHUNK_FROM_VEIN) + z,
                                    ).apply {
                                        if (!oreVein.isHidden && oreVein.rangeSize.last > 0)
                                            size = rand.nextInt(oreVein.rangeSize.first * 1000, oreVein.rangeSize.last * 1000)
                                        vein.oreChunks += this
                                    }
                                }
                            }

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

    // === FLUIDS === //

    private val fluidsChanceSort by lazy {
        resizeFluidVeins.mapValues { (dim, veins) ->
            veins.flatMap { vein ->
                List(vein.weight.toInt()) { vein }
            }
        }
    }

    private fun RegionRes.generateFluidLayers(world: World) {

        val regionSeed = world.seed xor (xRegion.toLong() shl 32) xor zRegion.toLong()
        val rand = Random(regionSeed)

        val dimVeins = fluidsChanceSort[dim].orEmpty()

        for (xx in 0 until VEIN_COUNT_IN_REGIN_COORDINATE) {
            for (zz in 0 until VEIN_COUNT_IN_REGIN_COORDINATE) {

                if (rand.nextDouble() < 0.85) {

                    val fluidVein = dimVeins[rand.nextInt(dimVeins.size)]

                    this.fluidVeins += VeinFluid(
                        xVein = (xRegion shl SHIFT_VEIN_FROM_REGION) + xx,
                        zVein = (zRegion shl SHIFT_VEIN_FROM_REGION) + zz,
                        fluidId = fluidVein.id,
                    ).also { vein ->

                        val sizeVein = if (!fluidVein.isHidden && fluidVein.rangeSize.last > 0)
                            rand.nextInt(fluidVein.rangeSize.first * 1000, fluidVein.rangeSize.last * 1000)
                        else 0

                        for (x in 0 until CHUNK_COUNT_IN_VEIN_COORDINATE) {
                            for (z in 0 until CHUNK_COUNT_IN_VEIN_COORDINATE) {
                                ChunkFluid(
                                    x = (vein.xVein shl SHIFT_CHUNK_FROM_VEIN) + x,
                                    z = (vein.zVein shl SHIFT_CHUNK_FROM_VEIN) + z,
                                    type = TypeFluidVein.entries.random(rand),
                                ).apply {
                                    size = sizeVein
                                    vein.oreChunks += this
                                }
                            }
                        }
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

    // === GENERAL === //

    private fun RegionRes.getVeinChunks(chunkX: Int, chunkZ: Int): List<ChunkCoordIntPair> {
        val list = mutableListOf<ChunkCoordIntPair>()

        var vein: VeinOre? = null

        for (xxx in 0 until VEIN_COUNT_IN_REGIN_COORDINATE) {
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
