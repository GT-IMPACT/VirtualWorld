package space.gtimpact.virtual_world.proxy

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.client.event.GuiOpenEvent
import space.gtimpact.virtual_world.addon.visual_prospecting.MapSettings

object ClientGuiEvents {

    @Suppress("unused")
    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        if (event.gui == null) {
            MapSettings.isFullscreenMap = false
        }
    }
}
