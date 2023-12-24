package space.gtimpact.virtual_world.integration.visual_prospecting

import com.sinthoras.visualprospecting.Utils
import com.sinthoras.visualprospecting.integration.model.buttons.ButtonManager
import com.sinthoras.visualprospecting.integration.model.layers.LayerManager
import com.sinthoras.visualprospecting.integration.model.locations.ILocationProvider
import net.minecraft.client.Minecraft
import space.gtimpact.virtual_world.api.VirtualOreVein

class VirtualOreLayer0() : LayerManager(VirtualOreLayer0Button.instance) {

    companion object {
        val instance: VirtualOreLayer0 = VirtualOreLayer0()
    }


    override fun generateVisibleElements(minBlockX: Int, minBlockZ: Int, maxBlockX: Int, maxBlockZ: Int): MutableList<out ILocationProvider> {

        val minFluidX = Utils.mapToCornerUndergroundFluidChunkCoord(Utils.coordBlockToChunk(minBlockX))
        val minFluidZ = Utils.mapToCornerUndergroundFluidChunkCoord(Utils.coordBlockToChunk(minBlockZ))
        val maxFluidX = Utils.mapToCornerUndergroundFluidChunkCoord(Utils.coordBlockToChunk(maxBlockX))
        val maxFluidZ = Utils.mapToCornerUndergroundFluidChunkCoord(Utils.coordBlockToChunk(maxBlockZ))
        val playerDimensionId = Minecraft.getMinecraft().thePlayer.dimension

        val undergroundFluidPositions = ArrayList<VirtualOreLayer0ChunkLocation>()

        repeat(4) {
            repeat(4) {
                
            }
        }

        return undergroundFluidPositions
    }
}

class VirtualOreLayer0Button : ButtonManager("visualprospecting.button.virtualiorelayer0", "virtualiorelayer0") {
    companion object {
        val instance: VirtualOreLayer0Button = VirtualOreLayer0Button()
    }
}

class VirtualOreLayer0ChunkLocation(
    private val blockX: Int,
    private val blockZ: Int,
    private val dimId: Int,
    private val ore: VirtualOreVein,
    private val amount: Int,
) : ILocationProvider {


    override fun getDimensionId(): Int {
        return dimId
    }

    override fun getBlockX(): Double {
        return blockX + .5
    }

    override fun getBlockZ(): Double {
        return blockZ + .5
    }

    fun getAmountFormatted(): String {
        return when(amount) {
            in 0..999 -> "$amount"
            in 1_000..999_999 -> "${(amount / 1_000)}k"
            else -> "${(amount / 1_000_000)}m"
        }
    }

    fun getVein(): VirtualOreVein {
        return ore
    }
}
