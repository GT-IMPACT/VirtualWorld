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
import space.gtimpact.virtual_world.addon.visual_prospecting.VirtualFluidVeinPosition
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
                val fluid = ClientVirtualWorldCache.getFluid(dim, chunkX, chunkZ)
                if (fluid != null)
                    locations.add(VirtualFluidsLocation(fluid.copy(x = chunkX, z = chunkZ)))
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

class VirtualFluidsLocation(val pos: VirtualFluidVeinPosition) : ILocationProvider {


    override fun getDimensionId(): Int {
        return pos.dimId
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

        val vein = location.pos.vein

        if (!vein.isHidden && vein.rangeSize.last > 0 && location.pos.size > 0) {

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

            //full
            DrawUtil.drawRectangle(pixel.getX() + halfBlock * 2, pixel.getY() + halfBlock * 2, fullWidth, fullWidth, borderColor, 225 * location.pos.size / 100)

            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) DrawUtil.drawLabel(
                "${location.pos.size}%",
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
