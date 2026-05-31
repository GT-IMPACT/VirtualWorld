package space.gtimpact.virtual_world.api.game.scanner.fluids

data class FluidScanMapEntry(
    val dimension: Int,
    val veins: List<ClientFluidScanState.ClientFluidVeinState>,
) {

    companion object {
        fun fromClientScanState(state: ClientFluidScanState): FluidScanMapEntry {
            return FluidScanMapEntry(
                dimension = state.dimensionId,
                veins = state.veins,
            )
        }
    }

    fun toData(): ClientFluidScanState {
        return ClientFluidScanState(
            dimensionId = dimension,
            veins = veins,
        )
    }
}
