package space.gtimpact.virtual_world.addon.visual_prospecting.layer

import com.sinthoras.visualprospecting.Utils
import com.sinthoras.visualprospecting.VP
import com.sinthoras.visualprospecting.integration.journeymap.render.LayerRenderer
import com.sinthoras.visualprospecting.integration.model.buttons.ButtonManager
import com.sinthoras.visualprospecting.integration.model.layers.LayerManager
import com.sinthoras.visualprospecting.integration.model.locations.ILocationProvider
import journeymap.client.render.draw.DrawStep
import journeymap.client.render.draw.DrawUtil
import journeymap.client.render.map.GridRenderer
import net.minecraft.client.Minecraft
import org.lwjgl.input.Keyboard
import space.gtimpact.virtual_world.addon.visual_prospecting.cache.CacheFluidVein
import space.gtimpact.virtual_world.addon.visual_prospecting.cache.ClientVirtualWorldCache
import space.gtimpact.virtual_world.api.ResourceGenerator
import space.gtimpact.virtual_world.util.Math.repeatOffset
import java.awt.geom.Point2D
import kotlin.math.pow

class VirtualFluidsLayerManager(buttonManager: ButtonManager) : LayerManager(buttonManager) {

    private fun mapToCorner(chunkCoord: Int): Int {
        return chunkCoord and -0x4
    }

    private var oldMinX = 0
    private var oldMaxX = 0
    private var oldMinZ = 0
    private var oldMaxZ = 0
    private var oldShiftKey = false

    override fun generateVisibleElements(minBlockX: Int, minBlockZ: Int, maxBlockX: Int, maxBlockZ: Int): List<ILocationProvider> {
        val dim = Minecraft.getMinecraft().thePlayer.dimension

        val minX = mapToCorner(Utils.coordBlockToChunk(minBlockX))
        val minZ = mapToCorner(Utils.coordBlockToChunk(minBlockZ))
        val maxX = mapToCorner(Utils.coordBlockToChunk(maxBlockX))
        val maxZ = mapToCorner(Utils.coordBlockToChunk(maxBlockZ))

        val locations = arrayListOf<VirtualFluidsLocation>()

        repeatOffset(minX, maxX, 4) { chunkX ->
            repeatOffset(minZ, maxZ, 4) { chunkZ ->

                ClientVirtualWorldCache.getFluid(dim, chunkX, chunkZ)?.also { cache ->

                    locations += VirtualFluidsLocation(
                        pos = cache.copy(x = chunkX, z = chunkZ).apply {
                            dimension = cache.dimension
                            vein = cache.vein
                        }
                    )
                }
            }
        }

        return locations
    }

    override fun needsRegenerateVisibleElements(minBlockX: Int, minBlockZ: Int, maxBlockX: Int, maxBlockZ: Int): Boolean {
        val minX = mapToCorner(Utils.coordBlockToChunk(minBlockX))
        val minZ = mapToCorner(Utils.coordBlockToChunk(minBlockZ))
        val maxX = mapToCorner(Utils.coordBlockToChunk(maxBlockX))
        val maxZ = mapToCorner(Utils.coordBlockToChunk(maxBlockZ))

        val isShiftKey = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)

        if (minX != oldMinX || maxX != oldMaxX || minZ != oldMinZ || maxZ != oldMaxZ || isShiftKey != oldShiftKey) {
            oldMinX = minX
            oldMaxX = maxX
            oldMinZ = minZ
            oldMaxZ = maxZ
            oldShiftKey = isShiftKey
            return true
        }
        return false
    }
}

class VirtualFluidsLayerRender(layerManager: LayerManager) : LayerRenderer(layerManager) {

    override fun mapLocationProviderToDrawStep(visibleElements: List<ILocationProvider>): List<DrawStep> {
        return visibleElements
            .mapNotNull { it as? VirtualFluidsLocation }
            .map { VirtualFluidsDrawStep(it) }
    }

}

class VirtualFluidsLocation(val pos: CacheFluidVein) : ILocationProvider {


    override fun getDimensionId(): Int {
        return pos.dimension
    }

    override fun getBlockX(): Double {
        return pos.x * 16.0
    }

    override fun getBlockZ(): Double {
        return pos.z * 16.0
    }

}

class VirtualFluidsDrawStep(private val location: VirtualFluidsLocation) : DrawStep {

    companion object {
        const val WEIGHT = .5
    }

    override fun draw(draggedPixelX: Double, draggedPixelY: Double, gridRenderer: GridRenderer, drawScale: Float, fontScale: Double, rotation: Double) {

        val vein = location.pos.vein ?: return
        val size = location.pos.chunks.sumOf { it.size } / 16
        val hasShow = location.pos.chunks.all { it.size == -1 }

        if (!vein.isHidden && vein.rangeSize.last > 0 && (size > 0 || hasShow)) {

            val blockSize = 2.0.pow(gridRenderer.zoom)

            val blockAsPixel = gridRenderer.getBlockPixelInGrid(location.blockX, location.blockZ)

            val pixel = Point2D.Double(blockAsPixel.getX() + draggedPixelX, blockAsPixel.getY() + draggedPixelY)

            val borderColor = vein.color

            val borderAlpha = 250

            val halfBlock = WEIGHT * blockSize

            val chunk = ResourceGenerator.CHUNK_COUNT_IN_VEIN_COORDINATE * VP.chunkWidth * blockSize
            val fullWidth = chunk - halfBlock * 2

            //top
            DrawUtil.drawRectangle(pixel.getX() + halfBlock * 2, pixel.getY() + halfBlock, fullWidth, halfBlock, borderColor, borderAlpha)

            //right
            DrawUtil.drawRectangle(pixel.getX() + chunk, pixel.getY() + halfBlock, halfBlock, chunk, borderColor, borderAlpha)

            //bot
            DrawUtil.drawRectangle(pixel.getX() + halfBlock * 2, pixel.getY() + chunk, fullWidth, halfBlock, borderColor, borderAlpha)

            //left
            DrawUtil.drawRectangle(pixel.getX() + halfBlock, pixel.getY() + halfBlock, halfBlock, chunk, borderColor, borderAlpha)

            if (size > 0) {

                //full
                DrawUtil.drawRectangle(pixel.getX() + halfBlock * 2, pixel.getY() + halfBlock * 2, fullWidth, fullWidth, borderColor, 225 * size / 100)

                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) DrawUtil.drawLabel(
                    "${size}%",
                    pixel.getX() + 2 * 4 * blockSize,
                    pixel.getY() + 10 * 4 * blockSize,
                    DrawUtil.HAlign.Right,
                    DrawUtil.VAlign.Below,
                    0,
                    180,
                    0x00FFFFFF,
                    255,
                    fontScale,
                    false,
                    rotation
                )
            }

            DrawUtil.drawLabel(
                "${vein.name}, ${vein.rangeSize.first}k - ${vein.rangeSize.last}k",
                pixel.getX() + 8 * blockSize,
                pixel.getY() + 8 * blockSize,
                DrawUtil.HAlign.Right,
                DrawUtil.VAlign.Below,
                0,
                180,
                0x00FFFFFF,
                255,
                fontScale,
                false,
                rotation
            )
        }
    }
}
