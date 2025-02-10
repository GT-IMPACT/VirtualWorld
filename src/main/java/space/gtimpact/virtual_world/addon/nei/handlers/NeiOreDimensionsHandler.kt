package space.gtimpact.virtual_world.addon.nei.handlers

import codechicken.lib.gui.GuiDraw
import codechicken.nei.PositionedStack
import codechicken.nei.recipe.*
import cpw.mods.fml.common.event.FMLInterModComms
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
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
import space.gtimpact.virtual_world.extras.drawText
import java.awt.Color
import java.awt.Rectangle

class NeiOreDimensionsHandler : TemplateRecipeHandler() {

    private val registerOres = run {
        val map = hashMapOf<String, List<VirtualOreVein>>()
        VirtualAPI.virtualOres
            .filter { !it.isHidden }
            .flatMap { it.dimensions }
            .distinct()
            .forEach {
                val list = arrayListOf<VirtualOreVein>()
                VirtualAPI.virtualOres.forEach { vein ->
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
            .setDisplayStack(ItemStack(Blocks.diamond_ore))
            .setMaxRecipesPerPage(2)
            .setHeight(165)
            .setWidth(174)
            .setShiftY(6)
            .build()

        GuiRecipeTab.handlerMap[handlerInfo.handlerName] = handlerInfo
    }

    override fun newInstance(): TemplateRecipeHandler {
        return NeiOreDimensionsHandler()
    }

    override fun recipiesPerPage(): Int {
        return 1
    }

    override fun getRecipeName(): String {
        return "World Ores Dimensions"
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

        ore.second.forEach { virtualOreVein ->
            oreVeins.add(virtualOreVein.name)
        }

        ttDisplayed = false
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            val nextPage = "Next page ->"
            val page = when {
                Keyboard.isKeyDown(Keyboard.KEY_2) -> 1 to nextPage
                Keyboard.isKeyDown(Keyboard.KEY_3) -> 2 to nextPage
                Keyboard.isKeyDown(Keyboard.KEY_4) -> 3 to nextPage
                Keyboard.isKeyDown(Keyboard.KEY_5) -> 4 to nextPage
                Keyboard.isKeyDown(Keyboard.KEY_6) -> 5 to nextPage
                Keyboard.isKeyDown(Keyboard.KEY_7) -> 6 to nextPage
                Keyboard.isKeyDown(Keyboard.KEY_8) -> 7 to nextPage
                Keyboard.isKeyDown(Keyboard.KEY_9) -> 8 to ""
                else -> 0 to "Next page, press key [1-9]"
            }

            oreVeins
                .chunked(12)
                .getOrNull(page.first)
                .orEmpty()
                .chunked(12)
                .forEachIndexed { index, veins ->
                    val data = if (veins.size == 12) veins + " " + page.second else veins
                    GuiDraw.drawMultilineTip(-2 + 15 * index, 0, data)
                }

            ttDisplayed = true
        }
    }

    override fun getOverlayIdentifier(): String {
        return "virtual_world_ores_dim"
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
        val ore: Pair<String, List<VirtualOreVein>>

        constructor(pair: Pair<String, List<VirtualOreVein>>) {
            this.ore = pair

            val stacks = hashSetOf<ItemStack>()
            for (vein in ore.second) {
                vein.ores.firstOrNull()?.also { component ->
                    stacks.add(component.ore)
                }
            }

            var y = 0
            var count = 0

            stacks.take(63).forEachIndexed { index, itemStack ->
                mOutputs.add(FixedPositionedStack(itemStack, 4 + index % 9 * 18, 20 + y * 18))
                count++
                if (count == 9) {
                    y++
                    count = 0
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
