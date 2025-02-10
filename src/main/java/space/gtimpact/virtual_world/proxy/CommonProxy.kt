package space.gtimpact.virtual_world.proxy

import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.event.*
import net.minecraftforge.common.MinecraftForge
import space.gtimpact.virtual_world.common.items.ScannerTool
import space.gtimpact.virtual_world.common.world.VirtualWorldSaveData
import space.gtimpact.virtual_world.config.Config

open class CommonProxy {

    open fun preInit(event: FMLPreInitializationEvent) {
        Config.createConfig(event.modConfigurationDirectory)
    }

    open fun init(event: FMLInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(VirtualWorldSaveData())
        FMLCommonHandler.instance().bus().register(VirtualWorldSaveData())
        ScannerTool.INSTANCE.registerItem()
    }

    open fun postInit(event: FMLPostInitializationEvent) {

    }

    open fun serverAboutToStart(event: FMLServerAboutToStartEvent) {
    }

    open fun serverStarting(event: FMLServerStartingEvent) {
    }

    open fun serverStarted(event: FMLServerStartedEvent) {
    }

    open fun serverStopping(event: FMLServerStoppingEvent) {
    }

    open fun serverStopped(event: FMLServerStoppedEvent) {
    }

    open fun openGui() {
    }
}
