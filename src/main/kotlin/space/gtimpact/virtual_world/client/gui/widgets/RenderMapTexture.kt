package space.gtimpact.virtual_world.client.gui.widgets

import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.AbstractTexture
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraft.client.resources.IResourceManager
import org.lwjgl.opengl.GL11
import space.gtimpact.virtual_world.network.BlockCoordinates
import space.gtimpact.virtual_world.network.FindVeinsPacket
import java.awt.Color
import java.awt.image.BufferedImage

class RenderMapTexture(
    val packet: FindVeinsPacket
) : AbstractTexture() {

    var width = -1
    var height = -1
    var invert = false
    var selected = "All"

    private fun getImage(): BufferedImage {
        val backgroundColor = if (invert) Color.GRAY.rgb else Color.WHITE.rgb
        val radius = (packet.radius * 2 + 1) * 16
        val image = BufferedImage(radius, radius, BufferedImage.TYPE_INT_ARGB)
        val raster = image.raster

        val playerX = packet.centerX - (packet.chunkX - packet.radius) * 16 - 1
        val playerZ = packet.centerZ - (packet.chunkZ - packet.radius) * 16 - 1

        for (x in 0 until radius) {
            for (z in 0 until radius) {
                image.setRGB(x, z, backgroundColor)
                val chunk = BlockCoordinates(x, z)
                packet.map[chunk]?.apply {
                    val name = packet.metaMap[idComponent.toShort()] ?: "ERROR"

                    // Variables used to locate within a chunk.
                    val k: Int = x % 16 + 1
                    val l: Int = z % 16 + 1
                    val first = ((k.toDouble()) + (l.toDouble()) * 15.5)
                    val percentOre = (first / 256.0) * 100
                    // Variables used to locate within a chunk.
                    val isAmountValid = percentOre < amount
                    val isSelected = selected == "All" || selected == name

                    // draw render component
                    if (isAmountValid && isSelected) {
                        val color = packet.ores.getOrDefault(name, Color.BLACK.rgb or -0x1000000)
                        image.setRGB(x, z, color)
                    }
                }
                // draw player pos
                if (x == playerX || z == playerZ) {
                    raster.setSample(x, z, 0, (raster.getSample(x, z, 0) + 255) / 2)
                    raster.setSample(x, z, 1, raster.getSample(x, z, 1) / 2)
                    raster.setSample(x, z, 2, raster.getSample(x, z, 2) / 2)
                }
                if (x % 16 == 0 || z % 16 == 0) {
                    // draw grid
                    raster.setSample(x, z, 0, raster.getSample(x, z, 0) / 2)
                    raster.setSample(x, z, 1, raster.getSample(x, z, 1) / 2)
                    raster.setSample(x, z, 2, raster.getSample(x, z, 2) / 2)
                }
            }
        }
        return image
    }

    private fun loadTexture(resourceManager: IResourceManager?, invert: Boolean) {
        this.invert = invert
        loadTexture(resourceManager)
    }

    fun loadTexture(resourceManager: IResourceManager?, selected: String, invert: Boolean) {
        this.selected = selected
        loadTexture(resourceManager, invert)
    }

    override fun loadTexture(resourceManager: IResourceManager?) {
        deleteGlTexture()
        val tId = getGlTextureId()
        if (tId < 0) return
        TextureUtil.uploadTextureImageAllocate(getGlTextureId(), getImage(), false, false)
        width = packet.getSize()
        height = packet.getSize()
    }

    fun glBindTexture(): Int {
        if (glTextureId < 0) {
            return glTextureId
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, getGlTextureId())
        return glTextureId
    }

    fun draw(x: Int, y: Int) {
        val w: Float = 1f / width.toFloat()
        val h: Float = 1f / height.toFloat()
        val u = 0
        val v = 0
        Tessellator.instance.apply {
            startDrawingQuads()
            addVertexWithUV(
                x.toDouble(),
                (y + height).toDouble(),
                0.0,
                (u.toFloat() * w).toDouble(),
                ((v + height).toFloat() * h).toDouble()
            )
            addVertexWithUV(
                (x + width).toDouble(),
                (y + height).toDouble(),
                0.0,
                ((u + width).toFloat() * w).toDouble(),
                ((v + height).toFloat() * h).toDouble()
            )
            addVertexWithUV(
                (x + width).toDouble(),
                y.toDouble(),
                0.0,
                ((u + width).toFloat() * w).toDouble(),
                (v.toFloat() * h).toDouble()
            )
            addVertexWithUV(
                x.toDouble(),
                y.toDouble(),
                0.0,
                (u.toFloat() * w).toDouble(),
                (v.toFloat() * h).toDouble()
            )
            draw()
        }
    }
}