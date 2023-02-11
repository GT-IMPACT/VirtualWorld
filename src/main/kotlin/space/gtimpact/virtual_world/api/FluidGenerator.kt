package space.gtimpact.virtual_world.api

import net.minecraft.world.chunk.Chunk
import space.gtimpact.virtual_world.api.VirtualAPI.GENERATED_REGIONS_VIRTUAL_FLUIDS
import space.gtimpact.virtual_world.api.fluids.ChunkFluid
import space.gtimpact.virtual_world.api.fluids.RegionFluid
import space.gtimpact.virtual_world.api.fluids.VeinFluid
import java.util.*
import kotlin.random.Random
import kotlin.random.nextInt

object FluidGenerator {

    private const val SHIFT_REGION_FROM_CHUNK = 5
    private const val SHIFT_VEIN_FROM_REGION = 3
    private const val SHIFT_CHUNK_FROM_VEIN = 2
    private const val CHUNK_COUNT_IN_VEIN_COORDINATE = 4
    private const val VEIN_COUNT_IN_REGIN_COORDINATE = 8

    fun Chunk.createFluidRegion(): RegionFluid {
        val dim = worldObj.provider.dimensionId
        RegionFluid(
            xPosition shr SHIFT_REGION_FROM_CHUNK, zPosition shr SHIFT_REGION_FROM_CHUNK, dim
        ).let { reg ->
            val hash = Objects.hash(reg.xRegion, reg.zRegion, dim)
            GENERATED_REGIONS_VIRTUAL_FLUIDS[dim]?.let {
                if (!it.contains(hash)) {
                    reg.generate()
                    it[hash] = reg
                } else {
                    return it[hash]!!
                }
            } ?: apply {
                reg.generate()
                GENERATED_REGIONS_VIRTUAL_FLUIDS[dim] = hashMapOf(hash to reg)
            }
            return reg
        }
    }

    /**
     * Set size of Virtual Fluid
     *
     * @param vein virtual fluid
     */
    private fun VeinFluid.setSize(vein: VirtualFluidVein) {
        size = Random.nextInt(vein.rangeSize) * 1000
    }

    /**
     * Generate Ore Vein by Virtual Fluid
     *
     * @param vein virtual fluid
     */
    private fun VeinFluid.generate(vein: VirtualFluidVein) {
        for (x in 0 until CHUNK_COUNT_IN_VEIN_COORDINATE) {
            for (z in 0 until CHUNK_COUNT_IN_VEIN_COORDINATE) {
                ChunkFluid(
                    x = (xVein shl SHIFT_CHUNK_FROM_VEIN) + x,
                    z = (zVein shl SHIFT_CHUNK_FROM_VEIN) + z,
                ).apply {
                    setSize(vein)
                    oreChunks += this
                }
            }
        }
    }

    /**
     * Generate Fluid Region
     */
    private fun RegionFluid.generate() {
        for (xx in 0 until VEIN_COUNT_IN_REGIN_COORDINATE) {
            for (zz in 0 until VEIN_COUNT_IN_REGIN_COORDINATE) {
                VirtualAPI.getRandomVirtualFluid(dim)?.also { ore ->
                    this.veins += VeinFluid(
                        xVein = (xRegion shl SHIFT_VEIN_FROM_REGION) + xx,
                        zVein = (zRegion shl SHIFT_VEIN_FROM_REGION) + zz,
                        fluidId = ore.id,
                    ).also { vein ->
                        vein.generate(ore)
                    }
                }
            }
        }
    }

    /**
     * Get Vein Fluid
     */
    fun Chunk.getFluidVein(): VeinFluid? {
        return createFluidRegion().getVein(this)
    }

    /**
     * Get Vein Fluid
     *
     * @param chunk current chunk
     */
    fun RegionFluid.getVein(chunk: Chunk): VeinFluid? {
        veins.forEach { vein ->
            vein.oreChunks.forEach { ch ->
                if (ch.x == chunk.xPosition && ch.z == chunk.zPosition) {
                    return vein
                }
            }
        }
        return null
    }
}