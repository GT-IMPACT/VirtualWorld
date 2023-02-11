package space.gtimpact.virtual_world.api

import net.minecraft.world.chunk.Chunk
import space.gtimpact.virtual_world.api.VirtualAPI.GENERATED_REGIONS_VIRTUAL_ORES
import space.gtimpact.virtual_world.api.VirtualAPI.LAYERS_VIRTUAL_ORES
import space.gtimpact.virtual_world.api.VirtualAPI.getRandomVirtualOre
import space.gtimpact.virtual_world.api.ores.ChunkOre
import space.gtimpact.virtual_world.api.ores.RegionOre
import space.gtimpact.virtual_world.api.ores.VeinOre
import java.util.*
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * Singleton Virtual Ore Generator
 */
object OreGenerator {

    private const val SHIFT_REGION_FROM_CHUNK = 5
    private const val SHIFT_VEIN_FROM_REGION = 3
    private const val SHIFT_CHUNK_FROM_VEIN = 2
    private const val CHUNK_COUNT_IN_VEIN_COORDINATE = 4
    private const val VEIN_COUNT_IN_REGIN_COORDINATE = 8

    /**
     * Generate Region Ore by Minecraft Chunk
     */
    fun Chunk.createOreRegion(): RegionOre {
        val dim = worldObj.provider.dimensionId
        RegionOre(
            xPosition shr SHIFT_REGION_FROM_CHUNK,
            zPosition shr SHIFT_REGION_FROM_CHUNK,
            dim
        ).let { reg ->
            val hash = Objects.hash(reg.xRegion, reg.zRegion, dim)
            GENERATED_REGIONS_VIRTUAL_ORES[dim]?.let {
                if (!it.contains(hash)) {
                    reg.generate()
                    it[hash] = reg
                } else {
                    return it[hash]!!
                }
            } ?: apply {
                reg.generate()
                GENERATED_REGIONS_VIRTUAL_ORES[dim] = hashMapOf(hash to reg)
            }
            return reg
        }
    }

    /**
     * Set size of Virtual Ore
     *
     * @param ore virtual ore
     */
    private fun ChunkOre.setSize(ore: VirtualOreVein) {
        size = Random.nextInt(ore.rangeSize) * 1000
    }

    /**
     * Generate Ore Vein by Virtual Ore
     *
     * @param ore virtual ore
     */
    private fun VeinOre.generate(ore: VirtualOreVein) {
        for (x in 0 until CHUNK_COUNT_IN_VEIN_COORDINATE) {
            for (z in 0 until CHUNK_COUNT_IN_VEIN_COORDINATE) {
                ChunkOre(
                    x = (xVein shl SHIFT_CHUNK_FROM_VEIN) + x,
                    z = (zVein shl SHIFT_CHUNK_FROM_VEIN) + z,
                ).apply {
                    setSize(ore)
                    oreChunks += this
                }
            }
        }
    }

    /**
     * Generate Ore Region with all layers
     */
    private fun RegionOre.generate() {
        for (layer in 0 until LAYERS_VIRTUAL_ORES) {
            val rawVeins = ArrayList<VeinOre>()
            for (xx in 0 until VEIN_COUNT_IN_REGIN_COORDINATE) {
                for (zz in 0 until VEIN_COUNT_IN_REGIN_COORDINATE) {
                    getRandomVirtualOre(layer, dim)?.also { ore ->
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
            this.veins[layer] = rawVeins
        }
    }

    /**
     * Get Vein and Chunk Ore
     *
     * @param layer layer
     */
    fun Chunk.getVeinAndChunk(layer: Int): Pair<VeinOre, ChunkOre>? {
        return createOreRegion().getVeinAndChunk(this, layer)
    }

    /**
     * Get Vein and Chunk Ore
     *
     * @param chunk current chunk
     * @param layer layer
     */
    fun RegionOre.getVeinAndChunk(chunk: Chunk, layer: Int): Pair<VeinOre, ChunkOre>? {
        veins[layer]?.forEach { veinOre ->
            veinOre.oreChunks.forEach { chunkOre ->
                if (chunkOre.x == chunk.xPosition && chunkOre.z == chunk.zPosition) {
                    return Pair(veinOre, chunkOre)
                }
            }
        }
        return null
    }
}