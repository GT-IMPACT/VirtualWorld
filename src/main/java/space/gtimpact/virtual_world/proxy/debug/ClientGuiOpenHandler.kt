package space.gtimpact.virtual_world.proxy.debug

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import net.minecraft.client.Minecraft
import net.minecraft.init.Items
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import space.gtimpact.virtual_world.config.Config

class ClientGuiOpenHandler {

    @Suppress("unused")
    @SubscribeEvent
    fun onRightClick(event: PlayerInteractEvent) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR && event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && Config.enableDebug) {
            return
        }

        val player = event.entityPlayer ?: return
        val world = player.worldObj ?: return

        if (!world.isRemote) return

        val held = player.heldItem ?: return
        if (held.item != Items.diamond) return

        event.isCanceled = true

        Minecraft.getMinecraft().displayGuiScreen(GuiOreScanState())
    }
}