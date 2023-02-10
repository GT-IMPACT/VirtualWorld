package space.gtimpact.virtual_world.extras

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ChatComponentText

fun EntityPlayer.send(msg: String) {
    addChatMessage(ChatComponentText(msg))
}