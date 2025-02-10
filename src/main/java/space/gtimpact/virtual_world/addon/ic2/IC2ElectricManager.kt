package space.gtimpact.virtual_world.addon.ic2

import net.minecraft.item.ItemStack

object IC2ElectricManager {

    private val electricItemClass: Class<*>? = try {
        Class.forName("ic2.api.item.ElectricItem")
    } catch (e: Exception) {
        null
    }

    private val managerField = electricItemClass
        ?.getDeclaredField("manager")
        ?.apply { isAccessible = true }

    private val managerInstance = managerField?.get(null)

    private val getChargeMethod = managerInstance?.javaClass?.methods?.find {
        it.name == "getCharge" && it.parameterTypes.contentEquals(arrayOf(ItemStack::class.java))
    }

    private val dischargeMethod = managerInstance?.javaClass?.methods?.find {
        it.name == "discharge" && it.parameterTypes.size == 6
    }

    fun getCharge(stack: ItemStack): Double {
        return getChargeMethod?.invoke(managerInstance, stack) as? Double ?: 0.0
    }

    fun discharge(
        stack: ItemStack,
        amount: Double,
        tier: Int,
        ignoreTransferLimit: Boolean,
        simulate: Boolean,
        externally: Boolean,
    ): Double {
        return dischargeMethod?.invoke(managerInstance, stack, amount, tier, ignoreTransferLimit, simulate, externally) as? Double ?: 0.0
    }
}
