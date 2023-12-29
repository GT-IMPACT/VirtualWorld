package space.gtimpact.virtual_world.addon.nei

import codechicken.nei.NEIModContainer
import codechicken.nei.api.IConfigureNEI
import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.Optional
import space.gtimpact.virtual_world.addon.nei.handlers.NeiFluidHandler
import space.gtimpact.virtual_world.addon.nei.handlers.NeiOreHandler

@Optional.Interface(iface = "codechicken.nei.api.API", modid = "NotEnoughItems")
class NEIBoostrapConfig : IConfigureNEI {

    companion object {
        internal var isAdded: Boolean = false
    }

    @Optional.Method(modid = "NotEnoughItems")
    override fun loadConfig() {
        if (NEIModContainer.plugins.contains(this)) return

        isAdded = false

        if (FMLCommonHandler.instance().getEffectiveSide().isClient)
            registerHandlers()

        isAdded = true
    }

    @Optional.Method(modid = "NotEnoughItems")
    override fun getName() = "VirtualWorld NEI Plugin"

    @Optional.Method(modid = "NotEnoughItems")
    override fun getVersion() = "1.0"

    private fun registerHandlers() {
        NeiOreHandler()
        NeiFluidHandler()
    }
}