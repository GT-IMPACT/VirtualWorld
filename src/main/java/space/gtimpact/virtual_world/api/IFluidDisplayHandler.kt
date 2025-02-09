package space.gtimpact.virtual_world.api

import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack

interface IFluidDisplayHandler {
    fun getItemFromFluid(stack: FluidStack?, useStackSize: Boolean): ItemStack?
    fun getFluidFromStack(stack: ItemStack?): FluidStack?
    fun getDrillFluid(): Fluid?
    val itemDisplay: ItemStack?
    val isModified: Boolean
}

var virtualWorldNeiFluidHandler: IFluidDisplayHandler = object : IFluidDisplayHandler {
    override fun getItemFromFluid(stack: FluidStack?, useStackSize: Boolean): ItemStack? = null
    override fun getFluidFromStack(stack: ItemStack?): FluidStack? = null
    override fun getDrillFluid(): Fluid? = null
    override val itemDisplay: ItemStack? = null
    override val isModified: Boolean = true
}
