package space.gtimpact.virtual_world.proxy

import cpw.mods.fml.common.Loader
import cpw.mods.fml.common.event.FMLInitializationEvent
import cpw.mods.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.common.MinecraftForge
import space.gtimpact.virtual_world.addon.visual_prospecting.VirtualProspectingIntegration
import space.gtimpact.virtual_world.config.Config
import space.gtimpact.virtual_world.proxy.debug.ClientGuiOpenHandler

class ClientProxy : CommonProxy() {

    override fun postInit(event: FMLPostInitializationEvent) {
        if (Loader.isModLoaded("visualprospecting"))
            VirtualProspectingIntegration.postInit()
    }

    override fun init(event: FMLInitializationEvent) {
        super.init(event)
        MinecraftForge.EVENT_BUS.register(ClientGuiEvents)
        if (Config.enableDebug) {
            MinecraftForge.EVENT_BUS.register(ClientGuiOpenHandler())
        }
    }
}
