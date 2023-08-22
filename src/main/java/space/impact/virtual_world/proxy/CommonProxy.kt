package space.impact.virtual_world.proxy

import cpw.mods.fml.common.event.*
import net.minecraftforge.common.MinecraftForge
import space.impact.virtual_world.JsonManager
import space.impact.virtual_world.api.*
import space.impact.virtual_world.common.items.ScannerTool
import space.impact.virtual_world.common.world.VWorldSaveData
import space.impact.virtual_world.config.Config
import java.util.Random

open class CommonProxy {

    open fun preInit(event: FMLPreInitializationEvent) {
        Config.createConfig(event.modConfigurationDirectory)
    }

    open fun init(event: FMLInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(VWorldSaveData())
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
    }

    open fun serverStarted(event: FMLServerStartedEvent) {
        JsonManager.load()
    }

    open fun serverStopping(event: FMLServerStoppingEvent) {
        JsonManager.save()
        VirtualAPI.random = null
    }

    open fun serverStopped(event: FMLServerStoppedEvent) {
    }

    open fun openGui() {
    }
}