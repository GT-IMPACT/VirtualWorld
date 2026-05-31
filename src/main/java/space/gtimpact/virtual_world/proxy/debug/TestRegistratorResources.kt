package space.gtimpact.virtual_world.proxy.debug

import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import space.gtimpact.virtual_world.api.VirtualAPI
import space.gtimpact.virtual_world.api.core.DimensionGen
import space.gtimpact.virtual_world.api.resources.fluids.FluidVein
import space.gtimpact.virtual_world.api.resources.ores.OreVein
import java.awt.Color

object TestRegistratorResources {

    fun register() {
        listOf(
            OreVein(
                id = 6,
                name = "Small Coal",
                color = 0x222222,
                dimensions = setOf(DimensionGen(0, "Overworld")),
                ores = listOf(
                    OreVein.OreVeinOut(
                        stack = ItemStack(Items.coal, 1),
                        chance = 100,
                    )
                ),
                layer = 0,
                weight = 38.0,
                rangeSize = 3..10,
                special = null,
                isHidden = false,
            ),
            OreVein(
                id = 5,
                name = "Small Iron",
                color = 0xB36B3C,
                dimensions = setOf(DimensionGen(0, "Overworld")),
                ores = listOf(
                    OreVein.OreVeinOut(
                        stack = ItemStack(Items.iron_ingot, 1),
                        chance = 100,
                    )
                ),
                layer = 0,
                weight = 24.0,
                rangeSize = 2..8,
                special = null,
                isHidden = false,
            ),
            OreVein(
                id = 4,
                name = "Large Coal",
                color = 0x222222,
                dimensions = setOf(DimensionGen(0, "Overworld")),
                ores = listOf(
                    OreVein.OreVeinOut(
                        stack = ItemStack(Items.coal, 9),
                        chance = 100,
                    )
                ),
                layer = 1,
                weight = 28.0,
                rangeSize = 18..48,
                special = null,
                isHidden = false,
            ),
            OreVein(
                id = 3,
                name = "Large Iron",
                color = 0xB36B3C,
                dimensions = setOf(DimensionGen(0, "Overworld")),
                ores = listOf(
                    OreVein.OreVeinOut(
                        stack = ItemStack(Items.iron_ingot, 9),
                        chance = 100,
                    )
                ),
                layer = 1,
                weight = 18.0,
                rangeSize = 14..36,
                special = null,
                isHidden = false,
            ),
            OreVein(
                id = 2,
                name = "Exotic Crystal",
                color = 0xCC33FF,
                dimensions = setOf(DimensionGen(0, "Overworld")),
                ores = listOf(
                    OreVein.OreVeinOut(
                        stack = ItemStack(Items.diamond, 1),
                        chance = 100,
                    )
                ),
                layer = 1,
                weight = 12.0,
                rangeSize = 6..22,
                special = null,
                isHidden = false,
            ),
            OreVein(
                id = 1,
                name = "Dense Titanium",
                color = 0xAAAAAA,
                dimensions = setOf(DimensionGen(0, "Overworld")),
                ores = listOf(
                    OreVein.OreVeinOut(
                        stack = ItemStack(Items.gold_ingot, 9),
                        chance = 100,
                    )
                ),
                layer = 1,
                weight = 9.0,
                rangeSize = 10..30,
                special = null,
                isHidden = false,
            )
        ).forEach {
            VirtualAPI.resourcesRegistry.registerOreVein(it)
        }

        listOf(
            FluidVein(
                id = 10001,
                name = "Oil",
                weight = 16.0,
                rangeSize = 1..10,
                color = Color.BLACK.hashCode(),
                dimensions = setOf(DimensionGen(0, "Overworld")),
                fluid = FluidStack(FluidRegistry.LAVA, 50),
            ),
            FluidVein(
                id = 10002,
                name = "Heavy Oil",
                weight = 16.0,
                rangeSize = 1..10,
                color = color(255, 0, 255),
                dimensions = setOf(DimensionGen(0, "Overworld")),
                fluid = FluidStack(FluidRegistry.LAVA, 50),
            ),
            FluidVein(
                id = 10003,
                name = "Medium Oil",
                weight = 16.0,
                rangeSize = 1..10,
                color = color(0, 255, 0),
                dimensions = setOf(DimensionGen(0, "Overworld")),
                fluid = FluidStack(FluidRegistry.LAVA, 50),
            ),
            FluidVein(
                id = 10004,
                name = "Light Oil",
                weight = 16.0,
                rangeSize = 1..10,
                color = color(255, 255, 0),
                dimensions = setOf(DimensionGen(0, "Overworld")),
                fluid = FluidStack(FluidRegistry.LAVA, 50),
            ),
            FluidVein(
                id = 10005,
                name = "Light Oil",
                weight = 16.0,
                rangeSize = 1..10,
                color = color(255, 255, 0),
                dimensions = setOf(DimensionGen(0, "Overworld")),
                fluid = FluidStack(FluidRegistry.WATER, 50),
            ),
        ).forEach {
            VirtualAPI.resourcesRegistry.registerFluidVein(it)
        }
    }

    fun color(r: Int, g: Int, b: Int): Int {
        return Color(r, g, b).hashCode()
    }
}
