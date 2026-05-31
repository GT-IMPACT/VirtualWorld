package space.gtimpact.virtual_world.api.game.scanner.ores

data class OreScanMapEntry(
    val dimension: Int,
    val layer: Int,
    val veins: List<ClientOreScanState.ClientOreVeinState>,
) {

    companion object {
        fun fromClientScanState(state: ClientOreScanState): OreScanMapEntry {
            return OreScanMapEntry(
                dimension = state.dimensionId,
                layer = state.layer,
                veins = state.veins,
            )
        }
    }

    fun toData(): ClientOreScanState {
        return ClientOreScanState(
            dimensionId = dimension,
            layer = layer,
            veins = veins,
        )
    }
}
