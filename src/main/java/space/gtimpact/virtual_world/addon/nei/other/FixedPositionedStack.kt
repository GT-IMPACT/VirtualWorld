package space.gtimpact.virtual_world.addon.nei.other

import codechicken.nei.ItemList
import codechicken.nei.PositionedStack
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemStack

class FixedPositionedStack @JvmOverloads constructor(stack: ItemStack?, x: Int, y: Int, val chance: Int = 0) : PositionedStack(stack, x, y, true) {
    private var perMutated: Boolean = false

    override fun generatePermutations() {
        if (this.perMutated) {
            return
        }
        val tDisplayStacks = ArrayList<ItemStack?>()
        for (tStack in this.items) {
            if (isStackValid(tStack)) {
                if (tStack.itemDamage == 32767) {
                    val permutations = ItemList.itemMap[tStack.item]
                    if (permutations.isNotEmpty()) {
                        var stack: ItemStack
                        val iterator: Iterator<ItemStack> = permutations.iterator()
                        while (iterator.hasNext()) {
                            stack = iterator.next()
                            tDisplayStacks.add(copyAmount(tStack.stackSize, stack))
                        }
                    } else {
                        val base = ItemStack(tStack.item, tStack.stackSize)
                        base.stackTagCompound = tStack.stackTagCompound
                        tDisplayStacks.add(base)
                    }
                } else {
                    tDisplayStacks.add(copy(tStack))
                }
            }
        }
        this.items = (tDisplayStacks.toTypedArray<ItemStack?>())
        if (items.isEmpty()) {
            this.items = arrayOf(ItemStack(Blocks.fire))
        }
        this.perMutated = true
        setPermutationToRender(0)
    }
}

fun isStackValid(stack: ItemStack?): Boolean {
    return (stack is ItemStack) && stack.item != null && stack.stackSize >= 0
}

fun copyAmount(amount: Int, vararg stacks: ItemStack?): ItemStack? {
    var resizedAmount = amount
    val rStack = copy(*stacks)
    if (!isStackValid(rStack)) {
        return null
    } else {
        if (resizedAmount > 64L) {
            resizedAmount = 64
        } else if (resizedAmount == -1) {
            resizedAmount = 111
        } else if (resizedAmount < 0) {
            resizedAmount = 0
        }

        rStack!!.stackSize = resizedAmount
        return rStack
    }
}

fun copy(vararg stacks: ItemStack?): ItemStack? {
    val var2 = stacks.size
    for (var3 in 0 until var2) {
        val tStack = stacks[var3]
        if (isStackValid(tStack)) {
            return tStack!!.copy()
        }
    }
    return null
}
