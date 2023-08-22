package space.impact.virtual_world.api

interface IChunkResourceOre {
    fun getAmountOreResource(): Int
    fun setAmountOreResource(amount: Int)
    fun setOreVein(id: VirtualOreVein)
    fun getOreVein(): VirtualOreVein
}

interface IChunkResourceFluid {
    fun getAmountFluidResource(): Int
    fun setAmountFluidResource(amount: Int)
    fun setFluidVein(id: VirtualFluidVein)
    fun getFluidVein(): VirtualFluidVein
}
