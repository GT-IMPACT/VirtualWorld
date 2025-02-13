package space.gtimpact.virtual_world.proxy

import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.event.*
import net.minecraftforge.common.MinecraftForge
import space.gtimpact.virtual_world.common.world.VirtualWorldSaveData
import space.gtimpact.virtual_world.config.Config

open class CommonProxy {

    open fun preInit(event: FMLPreInitializationEvent) {
        Config.createConfig(event.modConfigurationDirectory)
    }

    open fun init(event: FMLInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(VirtualWorldSaveData())
        FMLCommonHandler.instance().bus().register(VirtualWorldSaveData())
    }

    open fun postInit(event: FMLPostInitializationEvent) = Unit

    open fun serverAboutToStart(event: FMLServerAboutToStartEvent) = Unit

    open fun serverStarting(event: FMLServerStartingEvent) = Unit

    open fun serverStarted(event: FMLServerStartedEvent) = Unit

    open fun serverStopping(event: FMLServerStoppingEvent) = Unit

    open fun serverStopped(event: FMLServerStoppedEvent) = Unit
}