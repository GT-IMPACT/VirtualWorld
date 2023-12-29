package space.gtimpact.virtual_world.addon.nei.handlers

import codechicken.lib.gui.GuiDraw
import codechicken.nei.PositionedStack
import codechicken.nei.recipe.*
import cpw.mods.fml.common.event.FMLInterModComms
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import space.gtimpact.virtual_world.VirtualOres
import space.gtimpact.virtual_world.addon.nei.NEIBoostrapConfig
import space.gtimpact.virtual_world.addon.nei.other.FixedPositionedStack
import space.gtimpact.virtual_world.addon.nei.other.areUnificationsEqual
import space.gtimpact.virtual_world.api.TypeFluidVein
import space.gtimpact.virtual_world.api.VirtualAPI
import space.gtimpact.virtual_world.api.VirtualFluidVein
import space.gtimpact.virtual_world.api.virtualWorldNeiFluidHandler
import space.gtimpact.virtual_world.config.Config
import space.gtimpact.virtual_world.extras.drawText
import space.impact.impact_vw.ASSETS
import space.impact.impact_vw.MODID
import space.impact.impact_vw.MODNAME
import java.awt.Color
import java.awt.Rectangle
import java.text.NumberFormat

class NeiFluidHandler : TemplateRecipeHandler() {

    private val registerFluids = VirtualAPI.VIRTUAL_FLUIDS
        .filter { !it.isHidden }
        .sortedBy { it.name }

    init {
        transferRects += RecipeTransferRect(Rectangle(4, 0, 50, 16), overlayIdentifier)

        if (!NEIBoostrapConfig.isAdded) {
            FMLInterModComms.sendRuntimeMessage(
                VirtualOres, "NEIPlugins", "register-crafting-handler",
                "VirtualWorld@$recipeName@$overlayIdentifier"
            )
            GuiCraftingRecipe.craftinghandlers.add(this)
            GuiUsageRecipe.usagehandlers.add(this)
        }

        val handlerInfo = HandlerInfo.Builder(overlayIdentifier, MODNAME, MODID)
            .setDisplayStack(ItemStack(Blocks.flowing_lava))
            .setMaxRecipesPerPage(2)
            .setHeight(165)
            .setWidth(200)
            .setShiftY(6)
            .build()

        GuiRecipeTab.handlerMap[handlerInfo.handlerName] = handlerInfo
    }

    override fun newInstance(): TemplateRecipeHandler {
        return NeiFluidHandler()
    }

    override fun recipiesPerPage(): Int {
        return 1
    }

    override fun getRecipeName(): String {
        return "World Fluids"
    }

    override fun getGuiTexture(): String {
        return "$ASSETS:textures/gui/guiBase.png"
    }

    private var ttDisplayed: Boolean = false

    override fun drawExtras(recipe: Int) {
        val cache = arecipes[recipe] as? VirtualFluidVeinCachedRecipe
        val ore = cache?.fluid ?: return

        val clr = Color.BLACK.hashCode()
        val vName: String = ore.name

        drawText(4, 0, "Show All", Color(84, 81, 81).hashCode())
        drawText(4, 12, "$vName Vein", clr)

        val sizeVein = NumberFormat.getNumberInstance().format(ore.rangeSize.first) + " - " + NumberFormat.getNumberInstance().format(ore.rangeSize.last)
        drawText(4, 50, "Size: " + sizeVein + "k cycles", clr)

        TypeFluidVein.values().forEach {
            when(it) {
                TypeFluidVein.LP -> if (Config.countWaterForLPDrill > 0) drawText(4, 65, "Low Pressure:", clr)
                TypeFluidVein.MP -> if (Config.countWaterForMPDrill > 0) drawText(4, 85 + 10, "Medium Pressure:", clr)
                TypeFluidVein.HP -> if (Config.countWaterForHPDrill > 0) drawText(4, 105 + 20, "High Pressure:", clr)
            }
        }

        drawText(164 - GuiDraw.fontRenderer.getStringWidth("Use Shift"), 0, "Use Shift", Color(84, 81, 81).hashCode())
        var dims = mutableListOf<String>()

        for ((i, dimension) in ore.dimensions.withIndex()) {
            dims.add((i + 1).toString() + ". " + dimension.second)
        }

        ttDisplayed = false
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            if (dims.size >= 15) {
                val dims2: List<String?> = dims.subList(15, dims.size)
                dims = dims.subList(0, 15)
                val w: Int = calculateMaxW(dims)
                GuiDraw.drawMultilineTip(w + 15, 0, dims2)
            }
            GuiDraw.drawMultilineTip(0, 0, dims)
            ttDisplayed = true
        }
    }

    override fun getOverlayIdentifier(): String {
        return "virtual_world_fluids"
    }

    override fun drawBackground(recipe: Int) {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        GuiDraw.changeTexture(guiTexture)
        GuiDraw.drawTexturedModalRect(-4, -8, 1, 3, 174, 80)
    }

    override fun handleItemTooltip(gui: GuiRecipe<*>?, aStack: ItemStack?, currenttip: MutableList<String?>, aRecipeIndex: Int): List<String?> {
        val tObject = arecipes[aRecipeIndex]
        if (tObject is VirtualFluidVeinCachedRecipe) {
            for (tStack in tObject.mOutputs) {
                if (aStack == tStack.item) {
                    if ((tStack !is FixedPositionedStack) || (tStack.chance <= 0) || (tStack.chance == 10000)) break
                    currenttip.add((tStack.chance / 100).toString() + "." + (if (tStack.chance % 100 < 10) ("0" + tStack.chance % 100) else Integer.valueOf(tStack.chance % 100)) + "%")
                    break
                }
            }
            for (tStack in tObject.mInputs) {
                if (aStack == tStack.item) {
                    if (areUnificationsEqual(virtualWorldNeiFluidHandler.itemDisplay, tStack.item, true)) {
                        currenttip.add("Per 1 cycle")
                    }
                }
            }
        }
        return currenttip
    }

    override fun loadCraftingRecipes(outputId: String, vararg results: Any?) {
        if (outputId == overlayIdentifier) {
            for (vein in registerFluids) {
                arecipes.add(VirtualFluidVeinCachedRecipe(vein))
            }
        } else {
            super.loadCraftingRecipes(outputId, *results)
        }
    }

    override fun loadCraftingRecipes(aResult: ItemStack) {
        val tResults = arrayListOf<ItemStack>()
        tResults.add(aResult)

        for (vein in registerFluids) {
            val tNEIRecipe = VirtualFluidVeinCachedRecipe(vein)
            for (tStack in tResults) {
                if (tNEIRecipe.contains(tNEIRecipe.mOutputs, tStack)) {
                    arecipes.add(tNEIRecipe)
                    break
                }
            }
        }
    }

    override fun loadUsageRecipes(ingredient: ItemStack) {
        val tInputs = arrayListOf<ItemStack>()
        tInputs.add(ingredient)

        for (vein in registerFluids) {
            val tNEIRecipe = VirtualFluidVeinCachedRecipe(vein)
            for (tStack in tInputs) {
                if (tNEIRecipe.contains(tNEIRecipe.mOutputs, tStack) || tNEIRecipe.contains(tNEIRecipe.mInputs, tStack)) {
                    arecipes.add(tNEIRecipe)
                    break
                }
            }
        }
    }

    @Suppress("ConvertSecondaryConstructorToPrimary")
    inner class VirtualFluidVeinCachedRecipe : TemplateRecipeHandler.CachedRecipe {

        val mOutputs: MutableList<PositionedStack> = ArrayList()
        val mInputs: MutableList<PositionedStack> = ArrayList()
        val fluid: VirtualFluidVein

        constructor(vein: VirtualFluidVein) {
            this.fluid = vein

            virtualWorldNeiFluidHandler.getItemFromFluid(fluid.fluid, false)?.also { stack ->
                mOutputs.add(FixedPositionedStack(stack = stack, x = 4, y = 25))
            }
            mOutputs.add(FixedPositionedStack(stack = ItemStack(Items.bucket), x = 4, y = 25))

            TypeFluidVein.values().forEachIndexed { i, type ->

                val fluidStack = when(type) {
                    TypeFluidVein.LP -> if (Config.countWaterForLPDrill > 0) FluidStack(FluidRegistry.WATER, Config.countWaterForLPDrill) else null
                    TypeFluidVein.MP -> if (Config.countWaterForMPDrill > 0) FluidStack(FluidRegistry.WATER, Config.countWaterForMPDrill) else null
                    TypeFluidVein.HP -> if (Config.countWaterForHPDrill > 0) FluidStack(FluidRegistry.WATER, Config.countWaterForHPDrill) else null
                }

                virtualWorldNeiFluidHandler.getItemFromFluid(fluidStack, true)?.also { stack ->
                    mInputs.add(FixedPositionedStack(stack = stack, x = 4, y = 72 + i * 30))
                }
                mInputs.add(FixedPositionedStack(stack = ItemStack(Items.bucket, fluidStack?.amount ?: 1), x = 4, y = 75 + i * 30))
            }
        }

        override fun getIngredients(): List<PositionedStack> {
            return getCycledIngredients(cycleticks / 10, this.mInputs)
        }

        override fun getResult(): PositionedStack? {
            return null
        }

        override fun getOtherStacks(): List<PositionedStack> {
            return getCycledIngredients(cycleticks / 10, this.mOutputs)
        }
    }
}
