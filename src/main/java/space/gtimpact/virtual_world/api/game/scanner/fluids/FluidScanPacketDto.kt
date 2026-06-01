package space.gtimpact.virtual_world.api.game.scanner.fluids

data class FluidScanPacketDto(
    val dimensionId: Int,
    val veins: List<FluidVeinPacketDto>,
) {
    data class FluidVeinPacketDto(
        val veinX: Int,
        val veinZ: Int,

        val resourceId: Short,

        val totalGenerated: Int?,
        val totalMined: Int?,
        val totalRemaining: Int?,
    )
}
