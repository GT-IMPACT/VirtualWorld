package space.gtimpact.virtual_world.client.gui

import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.ResourceLocation
import net.minecraft.world.ChunkCoordIntPair
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import space.gtimpact.virtual_world.client.gui.widgets.RenderMapTexture
import space.gtimpact.virtual_world.client.gui.widgets.VeinsGuiScrollingList
import space.gtimpact.virtual_world.ASSETS
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class ScannerGui : GuiScreen() {

    companion object {
        const val GUI_ID = 1
        const val MIN_HEIGHT = 128
        const val MAX_HEIGHT = 1024
        const val MIN_WIDTH = 128
        val BG = ResourceLocation(ASSETS, "textures/gui/bg.png")
        var map: RenderMapTexture? = null

        fun create(newMap: RenderMapTexture) {
            map?.deleteGlTexture()
            map = null
            map = newMap.apply {
                loadTexture(null)
            }
        }
    }

    var veinList: VeinsGuiScrollingList? = null
    private var prevW = 0
    private var prevH = 0

    override fun drawScreen(x: Int, y: Int, f: Float) {
        drawDefaultBackground()
        map?.let { map ->
            val currentWidth = max(map.width, MIN_WIDTH)
            val currentHeight = max(map.height, MIN_HEIGHT)

            val aX = (width - currentWidth - 100) / 2
            val aZ = (height - currentHeight) / 2

            if (veinList == null || (prevW != width || prevH != height)) {
                veinList = VeinsGuiScrollingList(
                    parent = this,
                    width = 100,
                    height = currentHeight,
                    top = aZ,
                    bottom = aZ + currentHeight,
                    left = aX + currentWidth,
                    entryHeight = 10,
                    veins = map.packet.ores,
                    onSelected = { name, invert ->
                        map.loadTexture(null, name, invert)
                    }
                )
                prevW = width
                prevH = height
            }

            // draw back for ores
            drawRect(aX, aZ, aX + currentWidth + 100, aZ + currentHeight, -0x39393a)
            map.glBindTexture()
            map.draw(aX, aZ)
            veinList?.drawScreen(x, y, f)
            mc.textureManager.bindTexture(BG)
            GL11.glColor4f(15F, 15F, 15F, 15F)

            // draw corners
            // left top
            drawTexturedModalRect(aX - 5, aZ - 5, 0, 0, 5, 5)
            // right top
            drawTexturedModalRect(aX + currentWidth + 100, aZ - 5, 171, 0, 5, 5)
            // left down
            drawTexturedModalRect(aX - 5, aZ + currentHeight, 0, 161, 5, 5)
            // right down
            drawTexturedModalRect(aX + currentWidth + 100, aZ + currentHeight, 171, 161, 5, 5)

            // draw edges
            //top
            var i = aX
            while (i < aX + currentWidth + 100) {
                drawTexturedModalRect(i, aZ - 5, 5, 0, min(128, aX + currentWidth + 100 - i), 5)

                i += 128
            }
            //down
            i = aX
            while (i < aX + currentWidth + 100) {
                drawTexturedModalRect(i, aZ + currentHeight, 5, 161, min(128, aX + currentWidth + 100 - i), 5)
                i += 128
            }
            //left
            i = aZ
            while (i < aZ + currentHeight) {
                drawTexturedModalRect(aX - 5, i, 0, 5, 5, min(128, aZ + currentHeight - i))
                i += 128
            }
            //right
            i = aZ
            while (i < aZ + currentHeight) {
                drawTexturedModalRect(aX + currentWidth + 100, i, 171, 5, 5, min(128, aZ + currentHeight - i))
                i += 128
            }
            renderComponents(map, aX, aZ)
        }
    }

    private fun renderComponents(map: RenderMapTexture, aX: Int, aZ: Int) {
        val radius = (map.packet.radius * 2 + 1) * 16
        for (z in 0 until radius) {
            for (x in 0 until radius) {

                if (x % 16 == 0 && z % 16 == 0 && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    val coordinates = ChunkCoordIntPair(x, z)
                    map.packet.map[coordinates]?.apply {
                        if (amount <= 0) return@apply
                        val name = map.packet.metaMap[idComponent.toShort()] ?: "ERROR"
                        if (map.selected == "All" || map.selected == name) {
                            GL11.glPushMatrix()
                            GL11.glScaled(.5, .5, .5)
                            fontRendererObj.drawString(
                                "$amount%",
                                ((x + aX + 2) * 10.0 / 5.0).toInt(),
                                ((z + aZ + 19 - fontRendererObj.FONT_HEIGHT) * 10.0 / 5.0).toInt(),
                                Color.WHITE.hashCode(),
                                true
                            )
                            GL11.glPopMatrix()
                        }
                    }
                }
            }
        }
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }
}