package space.gtimpact.virtual_world.extras

import net.minecraft.client.Minecraft

fun drawText(x: Int, y: Int, text: String, aColor: Int) {
    Minecraft.getMinecraft().fontRenderer.drawString(text, x, y, aColor)
}
