package space.gtimpact.virtual_world.addon.visual_prospecting.layer

import com.sinthoras.visualprospecting.Utils
import com.sinthoras.visualprospecting.integration.journeymap.render.LayerRenderer
import com.sinthoras.visualprospecting.integration.model.buttons.ButtonManager
import com.sinthoras.visualprospecting.integration.model.layers.LayerManager
import com.sinthoras.visualprospecting.integration.model.locations.ILocationProvider
import journeymap.client.render.draw.DrawStep
import journeymap.client.render.draw.DrawUtil
import journeymap.client.render.map.GridRenderer
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack
import net.minecraft.util.StatCollector
import org.lwjgl.input.Keyboard.KEY_LSHIFT
import org.lwjgl.input.Keyboard.isKeyDown
import org.lwjgl.opengl.GL11
import space.gtimpact.virtual_world.addon.visual_prospecting.cache.CacheObjectPoint
import space.gtimpact.virtual_world.addon.visual_prospecting.cache.ClientVirtualWorldCache
import space.gtimpact.virtual_world.util.ItemStackRenderer
import space.gtimpact.virtual_world.util.Math.repeatOffset
import java.awt.geom.Point2D
import kotlin.math.roundToInt

class ObjectsLayerManager(buttonManager: ButtonManager) : LayerManager(buttonManager) {

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

        val locations = arrayListOf<ObjectsLocation>()

        repeatOffset(minBlockX, maxBlockX, 1) { x ->
            repeatOffset(minBlockZ, maxBlockZ, 1) { z ->
                ClientVirtualWorldCache.getObjectChunk(dim, x, z)?.also { cache ->
                    locations.add(ObjectsLocation(cache))
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

class ObjectsLayerRender(layerManager: LayerManager) : LayerRenderer(layerManager) {

    override fun mapLocationProviderToDrawStep(visibleElements: List<ILocationProvider>): List<DrawStep> {
        return visibleElements
            .mapNotNull { it as? ObjectsLocation }
            .map { ObjectsDrawStep(it) }
    }
}

class ObjectsLocation(val pos: CacheObjectPoint) : ILocationProvider {

    override fun getDimensionId(): Int {
        return pos.dimId
    }

    override fun getBlockX(): Double {
        return pos.coords.x.toDouble()
    }

    override fun getBlockZ(): Double {
        return pos.coords.z.toDouble()
    }
}

class ObjectsDrawStep(private val location: ObjectsLocation) : DrawStep {

    override fun draw(draggedPixelX: Double, draggedPixelY: Double, gridRenderer: GridRenderer, drawScale: Float, fontScale: Double, rotation: Double) {

        val blockAsPixel = gridRenderer.getBlockPixelInGrid(location.blockX + .25, location.blockZ + .25)
        val pixel = Point2D.Double(blockAsPixel.getX() + draggedPixelX, blockAsPixel.getY() + draggedPixelY)

        location.pos.elements.firstOrNull()?.also {  (name, stack) ->
            val x = pixel.getX()
            val y = pixel.getY()

            val item = ItemStack(stack.item, 0, stack.itemDamage)

            GL11.glPushMatrix()

            ItemStackRenderer.renderItemStack(item, x.roundToInt(), y.roundToInt(), overlayText = null)

            val text = StatCollector.translateToLocal(name).take(64)
            val sizeText = Minecraft.getMinecraft().fontRenderer.getStringWidth(text)

            DrawUtil.drawLabel(
                text,
                x - sizeText / 2.25,
                y - 12,
                DrawUtil.HAlign.Right,
                DrawUtil.VAlign.Middle,
                0,
                180,
                0x00FFFFFF,
                255,
                fontScale,
                false,
                rotation
            )

            GL11.glPopMatrix()
        }
    }
}
