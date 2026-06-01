package space.gtimpact.virtual_world.api.game.scanner

import space.gtimpact.virtual_world.api.game.scanner.fluids.ClientFluidScanState
import space.gtimpact.virtual_world.api.game.scanner.fluids.FluidScanMapEntry
import space.gtimpact.virtual_world.api.game.scanner.fluids.toVeinKey
import space.gtimpact.virtual_world.api.game.scanner.ores.ClientOreScanState
import space.gtimpact.virtual_world.api.game.scanner.ores.OreScanMapEntry
import space.gtimpact.virtual_world.api.game.scanner.ores.mergeOreVein
import space.gtimpact.virtual_world.api.game.scanner.ores.toVeinKey
import java.util.concurrent.ConcurrentHashMap

object ScannerClientStateManager {

    internal val scanOreMap = ConcurrentHashMap<ScanMapKey, OreScanMapEntry>()
    internal val scanFluidMap = ConcurrentHashMap<ScanMapKey, FluidScanMapEntry>()

    fun updateOres(state: ClientOreScanState) {
        mergeOreState(state = state)
    }

    fun updateFluids(state: ClientFluidScanState) {
        mergeFluids(state = state)
    }

    fun getOreState(
        dimensionId: Int,
        layer: Int,
    ): ClientOreScanState {
        val key = ScanMapKey(
            dimension = dimensionId,
            layer = layer
        )
        return ClientOreScanState(
            dimensionId = dimensionId,
            layer = layer,
            veins = scanOreMap[key]?.veins.orEmpty(),
        )
    }

    fun getFluidState(
        dimensionId: Int,
    ): ClientFluidScanState {
        val key = ScanMapKey(
            dimension = dimensionId,
            layer = 0,
        )
        return ClientFluidScanState(
            dimensionId = dimensionId,
            veins = scanFluidMap[key]?.veins.orEmpty(),
        )
    }

    private fun mergeOreState(state: ClientOreScanState) {
        val key = ScanMapKey(
            dimension = state.dimensionId,
            layer = state.layer
        )

        val entry = OreScanMapEntry(
            dimension = state.dimensionId,
            layer = state.layer,
            veins = state.veins
        )

        scanOreMap.merge(key, entry) { oldEntry, newEntry ->
            val mergedVeins = oldEntry.veins
                .associateBy { it.toVeinKey() }
                .toMutableMap()

            for (newVein in newEntry.veins) {
                val veinKey = newVein.toVeinKey()
                val oldVein = mergedVeins[veinKey]

                mergedVeins[veinKey] = mergeOreVein(oldVein, newVein)
            }

            oldEntry.copy(
                veins = mergedVeins.values.toList(),
            )
        }
    }

    private fun mergeFluids(state: ClientFluidScanState) {
        val key = ScanMapKey(
            dimension = state.dimensionId,
            layer = 0,
        )

        val entry = FluidScanMapEntry(
            dimension = state.dimensionId,
            veins = state.veins,
        )

        scanFluidMap.merge(key, entry) { oldEntry, newEntry ->
            val mergedVeins = oldEntry.veins
                .associateBy { it.toVeinKey() }
                .toMutableMap()

            for (newVein in newEntry.veins) {
                val veinKey = newVein.toVeinKey()
                mergedVeins[veinKey] = newVein
            }

            oldEntry.copy(
                veins = mergedVeins.values.toList(),
            )
        }
    }
}
