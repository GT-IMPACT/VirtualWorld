package space.gtimpact.virtual_world.proxy

import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.event.FMLInitializationEvent
import cpw.mods.fml.common.event.FMLPostInitializationEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent
import cpw.mods.fml.common.event.FMLServerStartedEvent
import cpw.mods.fml.common.event.FMLServerStartingEvent
import cpw.mods.fml.common.event.FMLServerStoppedEvent
import cpw.mods.fml.common.event.FMLServerStoppingEvent
import net.minecraftforge.common.MinecraftForge
import space.gtimpact.virtual_world.api.core.VirtualWorldProvider
import space.gtimpact.virtual_world.common.world.VirtualWorldSaveData
import space.gtimpact.virtual_world.config.Config
import space.gtimpact.virtual_world.proxy.debug.TestRegistratorResources

open class CommonProxy {

    internal val virtualWorldProvider: VirtualWorldProvider = VirtualWorldProvider()

    open fun preInit(event: FMLPreInitializationEvent) {
        Config.createConfig(event.modConfigurationDirectory)
    }

    open fun init(event: FMLInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(VirtualWorldSaveData())
        FMLCommonHandler.instance().bus().register(VirtualWorldSaveData())

        if (Config.enableDebug && Config.hasTestResources) {
            TestRegistratorResources.register()
        }
    }

    open fun postInit(event: FMLPostInitializationEvent) = Unit

    open fun serverAboutToStart(event: FMLServerAboutToStartEvent) = Unit

    open fun serverStarting(event: FMLServerStartingEvent) {
        val seed = event.server.worldServers
            .find { it.provider.dimensionId == 0 }
            ?.seed ?: return

        print("serverStarting -> $seed")

        virtualWorldProvider.starting(seed = seed)
    }

    open fun serverStarted(event: FMLServerStartedEvent) {
        virtualWorldProvider.started()
    }

    open fun serverStopping(event: FMLServerStoppingEvent) {
        virtualWorldProvider.stopping()
    }

    open fun serverStopped(event: FMLServerStoppedEvent) {
        virtualWorldProvider.stopped()
    }
}
