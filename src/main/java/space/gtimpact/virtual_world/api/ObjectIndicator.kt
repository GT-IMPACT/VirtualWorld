package space.gtimpact.virtual_world.api

import net.minecraft.item.ItemStack

interface ObjectIndicator {
    fun getStack(): ItemStack
    fun getLabel(): String
    fun playersRecipients(): List<String>
}
