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
import org.lwjgl.input.Keyboard.KEY_LSHIFT
import org.lwjgl.input.Keyboard.isKeyDown
import space.gtimpact.virtual_world.addon.visual_prospecting.cache.CacheOreVein
import space.gtimpact.virtual_world.addon.visual_prospecting.cache.ClientVirtualWorldCache
import space.gtimpact.virtual_world.api.ResourceGenerator
import space.gtimpact.virtual_world.util.Math.repeatOffset
import java.awt.geom.Point2D
import kotlin.math.pow

class VirtualOresLayerManager(private val layer: Int, buttonManager: ButtonManager) : LayerManager(buttonManager) {

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

        val locations = arrayListOf<VirtualOresLocation>()

        repeatOffset(minX, maxX, 4) { chunkX ->
            repeatOffset(minZ, maxZ, 4) { chunkZ ->

                ClientVirtualWorldCache.getOre(layer, dim, chunkX, chunkZ)?.also { cache ->

                    locations += VirtualOresLocation(
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

        val isShiftKey = isKeyDown(KEY_LSHIFT)

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

class VirtualOresLayerRender(layerManager: LayerManager) : LayerRenderer(layerManager) {

    override fun mapLocationProviderToDrawStep(visibleElements: List<ILocationProvider>): List<DrawStep> {
        return visibleElements
            .mapNotNull { it as? VirtualOresLocation }
            .map { VirtualOresDrawStep(it) }
    }

}

class VirtualOresLocation(val pos: CacheOreVein) : ILocationProvider {


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

class VirtualOresDrawStep(private val location: VirtualOresLocation) : DrawStep {

    companion object {
        const val WEIGHT = .5
    }

    override fun draw(draggedPixelX: Double, draggedPixelY: Double, gridRenderer: GridRenderer, drawScale: Float, fontScale: Double, rotation: Double) {

        val vein = location.pos.vein ?: return

        val hasShow = location.pos.chunks
            .takeIf { !it.all { it.size == -1 } }
            ?.sumOf { it.size }?.let { it > 0 } ?: true

        if (!vein.isHidden && vein.rangeSize.last > 0 && hasShow) {

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

            location.pos.chunks.chunked(4).forEachIndexed { row, list ->

                for ((col, data) in list.withIndex()) {

                    if (data.size <= 0) continue

                    val x = pixel.getX() + row * blockSize * 16
                    val y = pixel.getY() + col * blockSize * 16

                    DrawUtil.drawRectangle(
                        x + halfBlock * 2,
                        y + halfBlock * 2,
                        blockSize * 16,
                        blockSize * 16,
                        borderColor,
                        225 * data.size / 100
                    )

                    if (isKeyDown(KEY_LSHIFT)) DrawUtil.drawLabel(
                        "${data.size}%",
                        x + 2 * blockSize,
                        y + 10 * blockSize,
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

            DrawUtil.drawLabel(
                "${vein.name}, \n${vein.rangeSize.first}k - ${vein.rangeSize.last}k",
                pixel.getX() + 2 * blockSize,
                pixel.getY() + 2 * blockSize,
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
