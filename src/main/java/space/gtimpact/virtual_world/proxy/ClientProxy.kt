package space.gtimpact.virtual_world.proxy

import cpw.mods.fml.common.Loader
import cpw.mods.fml.common.event.FMLPostInitializationEvent
import space.gtimpact.virtual_world.addon.visual_prospecting.VirtualProspectingIntegration

class ClientProxy : CommonProxy() {

    override fun postInit(event: FMLPostInitializationEvent) {
        if (Loader.isModLoaded("visualprospecting"))
            VirtualProspectingIntegration.postInit()
    }
}
