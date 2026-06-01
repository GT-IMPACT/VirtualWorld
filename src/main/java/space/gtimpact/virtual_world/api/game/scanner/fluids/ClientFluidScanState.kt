package space.gtimpact.virtual_world.api.game.scanner.fluids

import space.gtimpact.virtual_world.api.core.ResourcePos
import space.gtimpact.virtual_world.api.resources.fluids.FluidVein

data class ClientFluidScanState(
    val dimensionId: Int,
    val veins: List<ClientFluidVeinState>,
) {

    companion object {
        val EMPTY = ClientFluidScanState(
            dimensionId = 0,
            veins = emptyList(),
        )
    }

    data class ClientFluidVeinState(
        val vein: FluidVein,
        val position: ResourcePos,
        val generatedAmount: Int,
        val minedAmount: Int,
        val remainingAmount: Int,
    )
}
