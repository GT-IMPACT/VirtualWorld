package space.gtimpact.virtual_world.addon.nei.handlers

import codechicken.lib.gui.GuiDraw
import codechicken.nei.PositionedStack
import codechicken.nei.recipe.*
import cpw.mods.fml.common.event.FMLInterModComms
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidStack
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import space.gtimpact.virtual_world.ASSETS
import space.gtimpact.virtual_world.MODID
import space.gtimpact.virtual_world.MODNAME
import space.gtimpact.virtual_world.VirtualOres
import space.gtimpact.virtual_world.addon.nei.NEIBoostrapConfig
import space.gtimpact.virtual_world.addon.nei.other.FixedPositionedStack
import space.gtimpact.virtual_world.api.VirtualAPI
import space.gtimpact.virtual_world.api.VirtualOreVein
import space.gtimpact.virtual_world.api.virtualWorldNeiFluidHandler
import space.gtimpact.virtual_world.extras.drawText
import java.awt.Color
import java.awt.Rectangle
import java.text.NumberFormat
import kotlin.math.max

class NeiOreHandler : TemplateRecipeHandler() {

    private val registerOres = VirtualAPI.VIRTUAL_ORES
        .filter { !it.isHidden }
        .sortedBy { it.layer }

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
            .setDisplayStack(ItemStack(Blocks.diamond_ore))
            .setMaxRecipesPerPage(2)
            .setHeight(165)
            .setWidth(200)
            .setShiftY(6)
            .build()

        GuiRecipeTab.handlerMap[handlerInfo.handlerName] = handlerInfo
    }

    override fun newInstance(): TemplateRecipeHandler {
        return NeiOreHandler()
    }

    override fun recipiesPerPage(): Int {
        return 1
    }

    override fun getRecipeName(): String {
        return "World Ores"
    }

    override fun getGuiTexture(): String {
        return "$ASSETS:textures/gui/guiBase.png"
    }

    private var ttDisplayed: Boolean = false

    override fun drawExtras(recipe: Int) {
        val cache = arecipes[recipe] as? VirtualOreVeinCachedRecipe
        val ore = cache?.ore ?: return

        val clr = Color.BLACK.hashCode()
        val vName: String = ore.name

        drawText(4, 0, "Show All", Color(84, 81, 81).hashCode())
        drawText(4, 12, "$vName Vein", clr)

        if (virtualWorldNeiFluidHandler.isModified) drawText(4, 48, "Need Special Fluid:", clr)
        val sizeVein = NumberFormat.getNumberInstance().format(ore.rangeSize.first) + " - " + NumberFormat.getNumberInstance().format(ore.rangeSize.last)
        drawText(4, 84, "Size: " + sizeVein + "k cycles", clr)
        drawText(4, 96, "Layer: ${ore.layer}", clr)

        drawText(164 - GuiDraw.getStringWidth("Use Shift"), 0, "Use Shift", Color(84, 81, 81).hashCode())
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
        return "virtual_world_ores_all"
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
            for (tStack in tObject.mInputs) {
                if (aStack?.item == tStack.item?.item)
                    currenttip.add("Per 1 cycle")
            }
        }
        return currenttip.distinct()
    }

    override fun loadCraftingRecipes(outputId: String, vararg results: Any?) {
        if (outputId == overlayIdentifier) {
            for (vein in registerOres) {
                arecipes.add(VirtualOreVeinCachedRecipe(vein))
            }
        } else {
            super.loadCraftingRecipes(outputId, *results)
        }
    }

    override fun loadCraftingRecipes(aResult: ItemStack) {
        val tResults = arrayListOf<ItemStack>()
        tResults.add(aResult)

        for (vein in registerOres) {
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

        for (vein in registerOres) {
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
        val ore: VirtualOreVein

        constructor(vein: VirtualOreVein) {
            this.ore = vein
            var x = 0

            val fs = virtualWorldNeiFluidHandler.getDrillFluid()
            val fluid = fs?.let { FluidStack(it, 50) }

            for ((i, component) in ore.ores.withIndex()) {
                if (i % 8 == 0) x++
                mOutputs.add(
                    FixedPositionedStack(
                        stack = component.ore,
                        x = 4 + i * 18,
                        y = 5 + x * 18,
                        chance = component.chance * 100
                    )
                )
            }

            virtualWorldNeiFluidHandler.getItemFromFluid(fluid, true)?.also { stack ->
                mInputs.add(FixedPositionedStack(stack = stack, x = 4, y = 41 + 18))
            }

            virtualWorldNeiFluidHandler.getItemFromFluid(ore.special, true)?.also { stack ->
                mInputs.add(FixedPositionedStack(stack = stack, x = 4 + 18, y = 41 + 18))
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

fun calculateMaxW(length: List<String?>): Int {
    var w = 0
    for (s in length) {
        w = max(GuiDraw.fontRenderer.getStringWidth(s).toDouble(), w.toDouble()).toInt()
    }
    return w
}