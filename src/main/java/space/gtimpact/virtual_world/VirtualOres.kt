@file:Suppress("unused")

package space.gtimpact.virtual_world

import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.SidedProxy
import cpw.mods.fml.common.event.*
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidRegistry
import space.gtimpact.virtual_world.api.VirtualFluidTypeComponent
import space.gtimpact.virtual_world.api.VirtualFluidVein
import space.gtimpact.virtual_world.api.VirtualOreComponent
import space.gtimpact.virtual_world.api.VirtualOreVein
import space.gtimpact.virtual_world.config.Config
import space.gtimpact.virtual_world.network.registerPackets
import space.gtimpact.virtual_world.proxy.CommonProxy
import java.awt.Color
import java.util.*
import kotlin.random.Random

@Mod(
    modid = MODID,
    version = VERSION,
    name = MODNAME,
    modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter",
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = "required-after:forgelin;"
)
object VirtualOres {

    init {
        registerPackets()
    }

    @SidedProxy(clientSide = "$GROUPNAME.proxy.ClientProxy", serverSide = "$GROUPNAME.proxy.CommonProxy")
    lateinit var proxy: CommonProxy

    @JvmStatic
    @Mod.InstanceFactory
    fun instance() = VirtualOres

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        proxy.preInit(event)
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        proxy.init(event)
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        proxy.postInit(event)
        if (Config.enableDebug) {

            val seed = 123456789
            val rand = Random(seed)

            (0..100).forEach {
                VirtualOreVein(
                    id = 20000 + it,
                    layer = if (it % 2 == 0) 1 else 0,
                    name = if (it == 0) "Empty" else "$it Ore ${if (it % 2 == 0) "L1" else "L0"}",
                    weight = rand.nextDouble(10.0, 30.0),
                    rangeSize = 50..300,
                    color = if (it == 0) Color.GREEN.hashCode() else UUID.randomUUID().hashCode(),
                    dimensions = listOf(0 to "Earth", -1 to "Nether", 1 to "End"),
                    ores = listOf(VirtualOreComponent(ItemStack(if (it % 2 == 0) Items.iron_ingot else Items.gold_ingot, 1), 0)),
                    special = null,
                    isHidden = it == 0
                )
            }

            VirtualOreVein(
                id = 30000,
                layer = 0,
                name = "Test Virtual Ore",
                weight = rand.nextDouble(10.0, 30.0),
                rangeSize = 50..300,
                color = Color.GREEN.hashCode(),
                dimensions = listOf(0 to "Earth", -1 to "Nether", 1 to "End"),
                ores = listOf(
                    VirtualOreComponent(ItemStack(Items.iron_ingot), 0),
                    VirtualOreComponent(ItemStack(Items.gold_ingot), 0),
                    VirtualOreComponent(ItemStack(Items.diamond), 0),
                    VirtualOreComponent(ItemStack(Item.getItemFromBlock(Blocks.stone)), 0)
                ),
                special = null,
                isHidden = false
            )
            VirtualFluidVein(
                id = 10000,
                name = "Empty",
                weight = 16.0,
                rangeSize = 1..2,
                color = Color.GREEN.hashCode(),
                dimensions = listOf(),
                fluid = VirtualFluidTypeComponent(FluidRegistry.LAVA, 0),
                isHidden = true
            )
            VirtualFluidVein(
                id = 10001,
                name = "Oil",
                weight = 16.0,
                rangeSize = 1..10,
                color = Color.BLACK.hashCode(),
                dimensions = listOf(0 to "Earth"),
                fluid = VirtualFluidTypeComponent(FluidRegistry.LAVA, 50)
            )
            VirtualFluidVein(
                id = 10002,
                name = "Heavy Oil",
                weight = 16.0,
                rangeSize = 1..10,
                color = color(255, 0, 255),
                dimensions = listOf(0 to "Earth"),
                fluid = VirtualFluidTypeComponent(FluidRegistry.LAVA, 50)
            )
            VirtualFluidVein(
                id = 10003,
                name = "Medium Oil",
                weight = 16.0,
                rangeSize = 1..10,
                color = color(0, 255, 0),
                dimensions = listOf(0 to "Earth"),
                fluid = VirtualFluidTypeComponent(FluidRegistry.LAVA, 50)
            )
            VirtualFluidVein(
                id = 10004,
                name = "Light Oil",
                weight = 16.0,
                rangeSize = 1..10,
                color = color(255, 255, 0),
                dimensions = listOf(0 to "Earth"),
                fluid = VirtualFluidTypeComponent(FluidRegistry.LAVA, 50)
            )
            VirtualFluidVein(
                id = 10005,
                name = "Light Oil",
                weight = 16.0,
                rangeSize = 1..10,
                color = color(255, 255, 0),
                dimensions = listOf(5 to "WTF Planet"),
                fluid = VirtualFluidTypeComponent(FluidRegistry.WATER, 50)
            )
        }
    }

    private fun color(r: Int, g: Int, b: Int): Int {
        return Color(r, g, b).hashCode()
    }

    @Mod.EventHandler
    fun serverAboutToStart(event: FMLServerAboutToStartEvent) {
        proxy.serverAboutToStart(event)
    }

    @Mod.EventHandler
    fun serverStarting(event: FMLServerStartingEvent) {
        proxy.serverStarting(event)
    }

    @Mod.EventHandler
    fun serverStarted(event: FMLServerStartedEvent) {
        proxy.serverStarted(event)
    }

    @Mod.EventHandler
    fun serverStopping(event: FMLServerStoppingEvent) {
        proxy.serverStopping(event)
    }

    @Mod.EventHandler
    fun serverStopped(event: FMLServerStoppedEvent) {
        proxy.serverStopped(event)
    }
}