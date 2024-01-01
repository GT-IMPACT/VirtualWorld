package space.gtimpact.virtual_world.addon.nei.handlers

import codechicken.lib.gui.GuiDraw
import codechicken.nei.PositionedStack
import codechicken.nei.recipe.*
import cpw.mods.fml.common.event.FMLInterModComms
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import space.gtimpact.virtual_world.addon.nei.NEIBoostrapConfig
import space.gtimpact.virtual_world.addon.nei.other.FixedPositionedStack
import space.gtimpact.virtual_world.api.VirtualAPI
import space.gtimpact.virtual_world.api.VirtualFluidVein
import space.gtimpact.virtual_world.api.virtualWorldNeiFluidHandler
import space.gtimpact.virtual_world.extras.drawText
import space.gtimpact.virtual_world.ASSETS
import space.gtimpact.virtual_world.MODID
import space.gtimpact.virtual_world.MODNAME
import space.gtimpact.virtual_world.VirtualOres
import java.awt.Color
import java.awt.Rectangle

class NeiFluidDimensionsHandler : TemplateRecipeHandler() {

    private val registerFluids = run {
        val map = hashMapOf<String, List<VirtualFluidVein>>()
        VirtualAPI.VIRTUAL_FLUIDS.flatMap { it.dimensions }.distinct().forEach {
            val list = arrayListOf<VirtualFluidVein>()
            VirtualAPI.VIRTUAL_FLUIDS.forEach { vein ->
                if (vein.dimensions.contains(it)) {
                    list += vein
                }
            }
            map[it.second] = list
        }
        map.map { it.key to it.value }
    }


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
        return NeiFluidDimensionsHandler()
    }

    override fun recipiesPerPage(): Int {
        return 1
    }

    override fun getRecipeName(): String {
        return "World Fluids Dimensions"
    }

    override fun getGuiTexture(): String {
        return "$ASSETS:textures/gui/guiBase.png"
    }

    private var ttDisplayed: Boolean = false

    override fun drawExtras(recipe: Int) {
        val cache = arecipes[recipe] as? VirtualOreVeinCachedRecipe
        val ore = cache?.ore ?: return

        val clr = Color.BLACK.hashCode()

        drawText(4, 0, "Show All", Color(84, 81, 81).hashCode())
        drawText(4, 10, "Dim Name: ${ore.first}", clr);

        drawText(164 - GuiDraw.getStringWidth("Use Shift"), 0, "Use Shift", Color(84, 81, 81).hashCode())
        val oreVeins = mutableListOf<String>()

        ore.second.take(29).forEach { virtualOreVein ->
            oreVeins.add(virtualOreVein.name.take(13) + if (virtualOreVein.name.length > 13) ".." else "")
        }

        if (ore.second.size >= 30)
            oreVeins.add("and more..")

        ttDisplayed = false
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            oreVeins.chunked(15).forEachIndexed { column, veins ->
                val w = if (column > 0) calculateMaxW(veins) else 0
                GuiDraw.drawMultilineTip( -2 + 15 * column + w, 0, veins)
            }
            ttDisplayed = true
        }
    }

    override fun getOverlayIdentifier(): String {
        return "virtual_world_fluids_dim"
    }

    override fun drawBackground(recipe: Int) {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        GuiDraw.changeTexture(guiTexture)
        GuiDraw.drawTexturedModalRect(-4, -8, 1, 3, 174, 80)
    }

    override fun handleItemTooltip(gui: GuiRecipe<*>?, aStack: ItemStack?, currenttip: MutableList<String?>, aRecipeIndex: Int): List<String?> {
        val tObject = arecipes[aRecipeIndex]
        if (tObject is VirtualOreVeinCachedRecipe) {
            for (tStack in tObject.mOutputs) {
                if (aStack == tStack.item) {
                    if ((tStack !is FixedPositionedStack) || (tStack.chance <= 0) || (tStack.chance == 10000)) break
                    currenttip.add((tStack.chance / 100).toString() + "." + (if (tStack.chance % 100 < 10) ("0" + tStack.chance % 100) else Integer.valueOf(tStack.chance % 100)) + "%")
                    break
                }
            }
        }
        return currenttip
    }

    override fun loadCraftingRecipes(outputId: String, vararg results: Any?) {
        if (outputId == overlayIdentifier) {
            for (vein in registerFluids) {
                arecipes.add(VirtualOreVeinCachedRecipe(vein))
            }
        } else {
            super.loadCraftingRecipes(outputId, *results)
        }
    }

    override fun loadCraftingRecipes(aResult: ItemStack) {
        val tResults = arrayListOf<ItemStack>()
        tResults.add(aResult)

        for (vein in registerFluids) {
            val tNEIRecipe = VirtualOreVeinCachedRecipe(vein)
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
            val tNEIRecipe = VirtualOreVeinCachedRecipe(vein)
            for (tStack in tInputs) {
                if (tNEIRecipe.contains(tNEIRecipe.mOutputs, tStack) || tNEIRecipe.contains(tNEIRecipe.mInputs, tStack)) {
                    arecipes.add(tNEIRecipe)
                    break
                }
            }
        }
    }

    @Suppress("ConvertSecondaryConstructorToPrimary")
    inner class VirtualOreVeinCachedRecipe : TemplateRecipeHandler.CachedRecipe {

        val mOutputs: MutableList<PositionedStack> = ArrayList()
        val mInputs: MutableList<PositionedStack> = ArrayList()
        val ore: Pair<String, List<VirtualFluidVein>>

        constructor(pair: Pair<String, List<VirtualFluidVein>>) {
            this.ore = pair

            val stacks = hashSetOf<Fluid>()
            for (vein in ore.second) {
                stacks.add(vein.fluid.getFluid())
            }
            var y = 0
            var count = 0

            stacks.forEachIndexed { index, itemStack ->
                virtualWorldNeiFluidHandler.getItemFromFluid(FluidStack(itemStack, 1), false)?.also { stack ->
                    mOutputs.add(FixedPositionedStack(stack, 4 + index % 9 * 18, 20 + y * 18))
                    count++
                    if (count == 9) {
                        y++
                        count = 0
                    }
                }
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
