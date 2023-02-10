package space.gtimpact.virtual_world.client.gui.widgets

import cpw.mods.fml.client.GuiScrollingList
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.Tessellator
import java.util.function.BiConsumer

class VeinsGuiScrollingList(
    private val parent: GuiScreen,
    width: Int, height: Int,
    top: Int, bottom: Int, left: Int, entryHeight: Int,
    private val veins: HashMap<String, Int>,
    private val onSelected: BiConsumer<String, Boolean>?,
) : GuiScrollingList(parent.mc, width, height, top, bottom, left, entryHeight) {

    private val keys: List<String>
    private var invert = false
    private var selected = 0

    init {
        keys = ArrayList(veins.keys)
        keys.sorted()
        if (keys.size > 1) {
            keys.add(0, "All")
        }
    }

    override fun getSize(): Int {
        return keys.size
    }

    override fun elementClicked(index: Int, doubleClick: Boolean) {
        selected = index
        if (doubleClick) {
            invert = !invert
        }
        onSelected?.accept(keys[index], invert)
    }

    override fun isSelected(index: Int): Boolean {
        return selected == index
    }

    override fun drawBackground() {}

    override fun drawSlot(slotIdx: Int, entryRight: Int, slotTop: Int, slotBuffer: Int, tess: Tessellator) {
        parent.drawString(
            parent.mc.fontRenderer,
            parent.mc.fontRenderer.trimStringToWidth(keys[slotIdx], listWidth - 10),
            left + 3,
            slotTop - 1,
            veins.getOrDefault(keys[slotIdx], 0x7d7b76),
        )
    }
}