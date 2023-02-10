package space.gtimpact.virtual_world.proxy

import cpw.mods.fml.common.event.*
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidRegistry
import space.gtimpact.virtual_world.JsonManager
import space.gtimpact.virtual_world.api.VirtualFluidTypeComponent
import space.gtimpact.virtual_world.api.VirtualFluidVein
import space.gtimpact.virtual_world.api.VirtualOreComponent
import space.gtimpact.virtual_world.api.VirtualOreVein
import space.gtimpact.virtual_world.common.items.ScannerTool
import space.gtimpact.virtual_world.config.Config
import java.awt.Color
import java.util.Random

open class CommonProxy {

    open fun preInit(event: FMLPreInitializationEvent) {
        Config.createConfig(event.modConfigurationDirectory)
    }

    open fun init(event: FMLInitializationEvent) {
        ScannerTool()

//        val rand = Random()
//
//        for (i in 0..30) {
//            VirtualOreVein(
//                i, 0, "kek0 $i",
//                50.0, 10..15,
//                Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)).hashCode(), listOf(0),
//                ores = listOf(
//                    VirtualOreComponent(ItemStack(Items.coal), 100)
//                )
//            )
//        }
//        for (i in 0..30) {
//            VirtualOreVein(
//                i + 31, 1, "kek1 $i",
//                50.0, 10..15,
//                Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)).hashCode(), listOf(0),
//                ores = listOf(
//                    VirtualOreComponent(ItemStack(Items.coal), 100)
//                )
//            )
//        }
//
//        for (i in 0..100) {
//            VirtualFluidVein(
//                i, 0, "kek fluid $i",
//                50.0, 10..15,
//                Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)).hashCode(), listOf(0),
//                fluid = VirtualFluidTypeComponent(FluidRegistry.WATER, 100)
//            )
//        }
    }

    open fun postInit(event: FMLPostInitializationEvent) {
    }

    open fun serverAboutToStart(event: FMLServerAboutToStartEvent) {
    }

    open fun serverStarting(event: FMLServerStartingEvent) {
    }

    open fun serverStarted(event: FMLServerStartedEvent) {
        space.gtimpact.virtual_world.JsonManager.load()
    }

    open fun serverStopping(event: FMLServerStoppingEvent) {
        space.gtimpact.virtual_world.JsonManager.save()
    }

    open fun serverStopped(event: FMLServerStoppedEvent) {
    }

    open fun openGui() {
    }
}