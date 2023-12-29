package space.gtimpact.virtual_world.proxy

import cpw.mods.fml.common.event.*
import net.minecraftforge.common.MinecraftForge
import space.gtimpact.virtual_world.api.*
import space.gtimpact.virtual_world.common.items.ScannerTool
import space.gtimpact.virtual_world.common.world.VirtualWorldSaveData
import space.gtimpact.virtual_world.config.Config
import java.util.Random

open class CommonProxy {

    open fun preInit(event: FMLPreInitializationEvent) {
        Config.createConfig(event.modConfigurationDirectory)
    }

    open fun init(event: FMLInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(VirtualWorldSaveData())
        ScannerTool()
    }

    open fun postInit(event: FMLPostInitializationEvent) {
    }

    open fun serverAboutToStart(event: FMLServerAboutToStartEvent) {
    }

    open fun serverStarting(event: FMLServerStartingEvent) {
        event.server?.worldServers
            ?.find { it?.provider?.dimensionId == 0 }
            ?.also { VirtualAPI.random = Random(it.seed) }
        VirtualAPI.resizeOreVeins()
        VirtualAPI.resizeFluidVeins()
    }

    open fun serverStarted(event: FMLServerStartedEvent) {
    }

    open fun serverStopping(event: FMLServerStoppingEvent) {
        VirtualAPI.random = null
    }

    open fun serverStopped(event: FMLServerStoppedEvent) {
    }

    open fun openGui() {
    }
}