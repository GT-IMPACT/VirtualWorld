package space.gtimpact.virtual_world.api.services.scanning

import space.gtimpact.virtual_world.api.core.WorldPos
import space.gtimpact.virtual_world.api.services.mining.MiningService
import space.gtimpact.virtual_world.api.services.scanning.fluids.FluidScanReport
import space.gtimpact.virtual_world.api.services.scanning.fluids.FluidVeinScanResult
import space.gtimpact.virtual_world.api.services.scanning.ores.OreScanReport
import space.gtimpact.virtual_world.api.services.scanning.ores.OreVeinScanResult
import space.gtimpact.virtual_world.api.services.storage.StorageService
import space.gtimpact.virtual_world.api.services.storage.getOreVeinAtVein

class ScanningService(
    private val regions: StorageService,
    private val mining: MiningService,
) {

    @JvmOverloads
    fun scanOreAroundBlock(
        dimensionId: Int,
        centerBlockPos: WorldPos,
        layerIndex: Int = 0,
        radiusVeins: Int = 2,
        mode: ScanMode = ScanMode.BOUNDARY_ONLY,
    ): OreScanReport {
        val veinPositions = calculateVeinAreaAroundBlock(
            centerBlockPos = centerBlockPos,
            radiusVeins = radiusVeins,
        )

        regions.preloadRegionsForVeins(
            dimensionId = dimensionId,
            veinPositions = veinPositions,
        )

        val results = mutableListOf<OreVeinScanResult>()

        for (veinPos in veinPositions) {
            val oreVein = regions.getOreVeinAtVein(
                dimensionId = dimensionId,
                pos = veinPos,
            ) ?: continue

            val oreLayer = oreVein.layers
                .firstOrNull { layer -> layer.layerIndex == layerIndex }
                ?: continue

            val bounds = VeinBounds.fromResPos(veinPos)

            when (mode) {
                ScanMode.BOUNDARY_ONLY -> {
                    results += OreVeinScanResult(
                        dimensionId = dimensionId,
                        pos = veinPos,
                        bounds = bounds,
                        layerIndex = layerIndex,
                        ore = oreLayer.ore,
                        generatedAmount = null,
                        minedAmount = null,
                        remainingAmount = null,
                    )
                }

                ScanMode.WITH_AMOUNT -> {
                    val chunks = oreLayer.chunks.mapNotNull { deposit ->
                        mining.getOreChunkStateAtChunk(
                            dimensionId = dimensionId,
                            chunkPos = deposit.chunkPos,
                            layerIndex = layerIndex,
                        )
                    }
                    val generatedAmount = chunks.sumOf { it.generatedAmount }
                    val minedAmount = chunks.sumOf { it.minedAmount }
                    val remainingAmount = chunks.sumOf { it.remainingAmount }.coerceAtLeast(0)

                    if (remainingAmount < 0) {
                        continue
                    }

                    results += OreVeinScanResult(
                        dimensionId = dimensionId,
                        pos = veinPos,
                        bounds = bounds,
                        layerIndex = layerIndex,
                        ore = oreLayer.ore,
                        generatedAmount = generatedAmount,
                        minedAmount = minedAmount,
                        remainingAmount = remainingAmount,
                        chunks = chunks,
                    )
                }
            }
        }

        return OreScanReport(
            dimensionId = dimensionId,
            centerBlockPos = centerBlockPos,
            radiusVeins = radiusVeins,
            layerIndex = layerIndex,
            mode = mode,
            results = results,
        )
    }

    @JvmOverloads
    fun scanFluidsAroundBlock(
        centerBlockPos: WorldPos,
        dimensionId: Int,
        radiusVeins: Int = 2,
        mode: ScanMode = ScanMode.BOUNDARY_ONLY,
    ): FluidScanReport {
        val veinPositions = calculateVeinAreaAroundBlock(
            centerBlockPos = centerBlockPos,
            radiusVeins = radiusVeins,
        )

        regions.preloadRegionsForVeins(
            dimensionId = dimensionId,
            veinPositions = veinPositions,
        )

        val results = mutableListOf<FluidVeinScanResult>()

        for (veinPos in veinPositions) {
            val state = mining.getFluidVeinStateAtVein(
                dimensionId = dimensionId,
                veinPos = veinPos,
            ) ?: continue

            val bounds = VeinBounds.fromResPos(veinPos)

            when (mode) {
                ScanMode.BOUNDARY_ONLY -> {
//                    if (state.remainingVolume <= 0) {
//                        continue
//                    }

                    results += FluidVeinScanResult(
                        dimensionId = dimensionId,
                        pos = veinPos,
                        bounds = bounds,
                        fluid = state.fluid,
                        generatedAmount = null,
                        extractedAmount = null,
                        remainingAmount = null,
                    )
                }

                ScanMode.WITH_AMOUNT -> {
//                    if (state.remainingVolume <= 0) {
//                        continue
//                    }

                    results += FluidVeinScanResult(
                        dimensionId = dimensionId,
                        pos = veinPos,
                        bounds = bounds,
                        fluid = state.fluid,
                        generatedAmount = state.generatedVolume,
                        extractedAmount = state.extractedVolume,
                        remainingAmount = state.remainingVolume,
                    )
                }
            }
        }

        return FluidScanReport(
            dimensionId = dimensionId,
            centerBlockPos = centerBlockPos,
            radiusVeins = radiusVeins,
            mode = mode,
            results = results
        )
    }
}
