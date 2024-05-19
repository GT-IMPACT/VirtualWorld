package space.gtimpact.virtual_world.util

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.entity.RenderItem
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.GL11

@SideOnly(Side.CLIENT)
object ItemStackRenderer {

    private val renderItem: RenderItem = RenderItem.getInstance()
    private val fontRenderer: FontRenderer = Minecraft.getMinecraft().fontRenderer

    fun renderItemStack(stack: ItemStack?, x: Int, y: Int, overlayText: String? = null) {
        if (stack == null) return

        GL11.glPushMatrix()
        GL11.glEnable(GL11.GL_BLEND)
        RenderHelper.enableGUIStandardItemLighting()
        GL11.glEnable(GL11.GL_ALPHA_TEST)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        renderItem.renderItemAndEffectIntoGUI(fontRenderer, Minecraft.getMinecraft().textureManager, stack, x, y)
        renderItem.renderItemOverlayIntoGUI(fontRenderer, Minecraft.getMinecraft().textureManager, stack, x, y, overlayText)

        RenderHelper.disableStandardItemLighting()
        GL11.glDisable(GL11.GL_ALPHA_TEST)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glPopMatrix()
    }
}
