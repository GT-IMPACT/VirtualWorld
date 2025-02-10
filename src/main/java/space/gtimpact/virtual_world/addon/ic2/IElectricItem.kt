package ic2.api.item

import net.minecraft.item.Item
import net.minecraft.item.ItemStack

interface IElectricItem {
    
    fun canProvideEnergy(stack: ItemStack): Boolean

    fun getChargedItem(stack: ItemStack): Item

    fun getEmptyItem(stack: ItemStack): Item

    fun getMaxCharge(stack: ItemStack): Double

    fun getTier(stack: ItemStack): Int

    fun getTransferLimit(stack: ItemStack): Double
}
