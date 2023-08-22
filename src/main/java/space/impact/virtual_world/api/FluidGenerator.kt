package space.impact.virtual_world.api

import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraft.world.chunk.Chunk
import space.impact.virtual_world.api.VirtualAPI.GENERATED_REGIONS_VIRTUAL_FLUIDS
import space.impact.virtual_world.api.fluids.ChunkFluid
import space.impact.virtual_world.api.fluids.RegionFluid
import space.impact.virtual_world.api.fluids.VeinFluid
import java.util.*
import kotlin.random.Random

object FluidGenerator {

    private const val SHIFT_REGION_FROM_CHUNK = 5
    private const val SHIFT_VEIN_FROM_REGION = 3
    private const val SHIFT_CHUNK_FROM_VEIN = 2
    private const val CHUNK_COUNT_IN_VEIN_COORDINATE = 4
    private const val VEIN_COUNT_IN_REGIN_COORDINATE = 8

    @JvmStatic
    fun Chunk.createFluidRegion(w: World): RegionFluid {
        val dim = worldObj.provider.dimensionId
        val reg = RegionFluid(xPosition shr SHIFT_REGION_FROM_CHUNK, zPosition shr SHIFT_REGION_FROM_CHUNK, dim)
        reg.generate(w)
        return reg
    }

    /**
     * Generate Ore Vein by Virtual Fluid
     *
     * @param vein virtual fluid
     */
    private fun VeinFluid.generate(vein: VirtualFluidVein, w: World) {
        for (x in 0 until CHUNK_COUNT_IN_VEIN_COORDINATE) {
            for (z in 0 until CHUNK_COUNT_IN_VEIN_COORDINATE) {
                ChunkFluid(
                    x = (xVein shl SHIFT_CHUNK_FROM_VEIN) + x,
                    z = (zVein shl SHIFT_CHUNK_FROM_VEIN) + z,
                    size = Random.nextInt(vein.rangeSize.first * 1000, vein.rangeSize.last * 1000)
                ).apply {
                    oreChunks += this
                    val chunk = w.getChunkFromChunkCoords(this.x, this.z) as? IChunkResourceFluid
                    chunk?.setFluidVein(vein)
                    chunk?.setAmountFluidResource(this.size)
                }
            }
        }
    }

    /**
     * Generate Fluid Region
     */
    @JvmStatic
    fun RegionFluid.generate(w: World) {
        for (xx in 0 until VEIN_COUNT_IN_REGIN_COORDINATE) {
            for (zz in 0 until VEIN_COUNT_IN_REGIN_COORDINATE) {
                VirtualAPI.getRandomVirtualFluid(dim)?.also { ore ->
                    this.veins += VeinFluid(
                        xVein = (xRegion shl SHIFT_VEIN_FROM_REGION) + xx,
                        zVein = (zRegion shl SHIFT_VEIN_FROM_REGION) + zz,
                        fluidId = ore.id,
                    ).also { vein ->
                        vein.generate(ore, w)
                    }
                }
            }
        }
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