package space.gtimpact.virtual_world.api.services.mining

import space.gtimpact.virtual_world.api.core.ChunkPos
import space.gtimpact.virtual_world.api.core.ResourcePos
import space.gtimpact.virtual_world.api.core.WorldPos
import space.gtimpact.virtual_world.api.core.toChunkPos
import space.gtimpact.virtual_world.api.core.toResourcePos
import space.gtimpact.virtual_world.api.core.toWorldOrigin
import space.gtimpact.virtual_world.api.services.mining.fluids.FluidExtractionResult
import space.gtimpact.virtual_world.api.services.mining.fluids.FluidVeinKey
import space.gtimpact.virtual_world.api.services.mining.ores.OreChunkKey
import space.gtimpact.virtual_world.api.services.mining.ores.OreMiningResult
import space.gtimpact.virtual_world.api.services.scanning.fluids.FluidVeinResult
import space.gtimpact.virtual_world.api.services.scanning.ores.OreChunkResult
import space.gtimpact.virtual_world.api.services.storage.StorageService
import space.gtimpact.virtual_world.api.services.storage.getFluidVeinAtVein
import space.gtimpact.virtual_world.api.services.storage.getOreVeinAtVein

class MiningService(
    private val worldSeed: Long,
    private val regions: StorageService,
    private val state: MiningStateStore,
) {

    private val lock = Any()

    fun getOreChunkStateAtBlock(
        dimensionId: Int,
        blockPos: WorldPos,
        layerIndex: Int = 0,
    ): OreChunkResult? {
        return getOreChunkStateAtChunk(
            dimensionId = dimensionId,
            chunkPos = blockPos.toChunkPos(),
            layerIndex = layerIndex,
        )
    }

    fun getOreChunkStateAtChunk(
        dimensionId: Int,
        chunkPos: ChunkPos,
        layerIndex: Int = 0,
    ): OreChunkResult? {
        val oreVein = regions.getOreVeinAtVein(
            dimensionId = dimensionId,
            pos = chunkPos.toWorldOrigin().toResourcePos(),
        ) ?: return null

        val oreLayer = oreVein.layers.firstOrNull { layer ->
            layer.layerIndex == layerIndex
        } ?: return null

        val deposit = oreLayer.chunks.firstOrNull { chunkDeposit ->
            chunkDeposit.chunkPos == chunkPos
        } ?: return null

        val key = OreChunkKey(
            worldSeed = worldSeed,
            dimensionId = dimensionId,
            layerIndex = layerIndex,
            chunkPos = chunkPos,
        )

        val generatedAmount = deposit.amount

        val minedAmount = state
            .getMinedOreAmount(key)
            .coerceAtMost(generatedAmount)

        val remainingAmount = generatedAmount - minedAmount

        return OreChunkResult(
            ore = oreLayer.ore,
            vein = oreVein,
            layer = oreLayer,
            deposit = deposit,
            generatedAmount = generatedAmount,
            minedAmount = minedAmount,
            remainingAmount = remainingAmount,
        )
    }

    fun getAvailableOreChunkAtBlock(
        dimensionId: Int,
        blockPos: WorldPos,
        layerIndex: Int = 0,
    ): OreChunkResult? {
        val result = getOreChunkStateAtBlock(
            dimensionId = dimensionId,
            blockPos = blockPos,
            layerIndex = layerIndex,
        ) ?: return null

        return result.takeIf { it.remainingAmount > 0 }
    }

    fun getAvailableOreChunkAtChunk(
        dimensionId: Int,
        chunkPos: ChunkPos,
        layerIndex: Int = 0,
    ): OreChunkResult? {
        val result = getOreChunkStateAtChunk(
            dimensionId = dimensionId,
            chunkPos = chunkPos,
            layerIndex = layerIndex,
        ) ?: return null

        return result.takeIf { it.remainingAmount > 0 }
    }

    fun mineOreAtBlock(
        dimensionId: Int,
        blockPos: WorldPos,
        layerIndex: Int = 0,
        amount: Int
    ): OreMiningResult? {
        return mineOreAtChunk(
            dimensionId = dimensionId,
            chunkPos = blockPos.toChunkPos(),
            layerIndex = layerIndex,
            amount = amount
        )
    }

    fun mineOreAtChunk(
        dimensionId: Int,
        chunkPos: ChunkPos,
        layerIndex: Int = 0,
        amount: Int,
    ): OreMiningResult? {
        if (amount <= 0) return null

        val oreChunk = getOreChunkStateAtChunk(
            dimensionId = dimensionId,
            chunkPos = chunkPos,
            layerIndex = layerIndex,
        ) ?: return null

        val key = OreChunkKey(
            worldSeed = worldSeed,
            dimensionId = dimensionId,
            layerIndex = layerIndex,
            chunkPos = chunkPos,
        )

        val change = synchronized(lock) {
            val current = state.getMinedOreAmount(key).coerceAtMost(oreChunk.generatedAmount)
            val remaining = oreChunk.generatedAmount - current
            if (remaining <= 0) {
                null
            } else {
                val changedAmount = amount.coerceAtMost(remaining)
                state.addMinedOreAmount(key, changedAmount)
                MiningAmountChange(
                    changedAmount = changedAmount,
                    totalChangedAmount = current + changedAmount,
                    remainingAmount = oreChunk.generatedAmount - current - changedAmount,
                )
            }
        } ?: return null

        return OreMiningResult(
            ore = oreChunk.ore,
            chunkPos = chunkPos,
            layerIndex = layerIndex,
            requestedAmount = amount,
            minedAmount = change.changedAmount,
            remainingAmount = change.remainingAmount,
        )
    }

    fun getFluidVeinStateAtBlock(
        dimensionId: Int,
        blockPos: WorldPos,
    ): FluidVeinResult? {
        return getFluidVeinStateAtVein(
            dimensionId = dimensionId,
            veinPos = blockPos.toResourcePos(),
        )
    }

    fun getFluidVeinStateAtVein(
        dimensionId: Int,
        veinPos: ResourcePos,
    ): FluidVeinResult? {
        val fluidVein = regions.getFluidVeinAtVein(
            dimensionId = dimensionId,
            pos = veinPos,
        ) ?: return null

        val key = FluidVeinKey(
            worldSeed = worldSeed,
            dimensionId = dimensionId,
            pos = veinPos,
        )

        val generatedVolume = fluidVein.amount

        val extractedVolume = state
            .getExtractedFluidVolume(key)
            .coerceAtMost(generatedVolume)

        val remainingVolume = generatedVolume - extractedVolume

        return FluidVeinResult(
            vein = fluidVein,
            fluid = fluidVein.fluid,
            generatedVolume = generatedVolume,
            extractedVolume = extractedVolume,
            remainingVolume = remainingVolume,
        )
    }

    fun getAvailableFluidVeinAtBlock(
        dimensionId: Int,
        blockPos: WorldPos,
    ): FluidVeinResult? {
        val result = getFluidVeinStateAtBlock(
            dimensionId = dimensionId,
            blockPos = blockPos,
        ) ?: return null

        return result.takeIf { it.remainingVolume > 0 }
    }

    fun getAvailableFluidVeinAtVein(
        dimensionId: Int,
        veinPos: ResourcePos,
    ): FluidVeinResult? {
        val result = getFluidVeinStateAtVein(
            dimensionId = dimensionId,
            veinPos = veinPos,
        ) ?: return null

        return result.takeIf { it.remainingVolume > 0 }
    }

    fun extractFluidAtBlock(
        dimensionId: Int,
        blockPos: WorldPos,
        volume: Int,
    ): FluidExtractionResult? {
        return extractFluidAtVein(
            dimensionId = dimensionId,
            veinPos = blockPos.toResourcePos(),
            volume = volume,
        )
    }

    fun extractFluidAtVein(
        dimensionId: Int,
        veinPos: ResourcePos,
        volume: Int,
    ): FluidExtractionResult? {
        if (volume <= 0) return null

        val fluid = getFluidVeinStateAtVein(
            dimensionId = dimensionId,
            veinPos = veinPos,
        ) ?: return null

        val key = FluidVeinKey(
            worldSeed = worldSeed,
            dimensionId = dimensionId,
            pos = veinPos,
        )

        val change = synchronized(lock) {
            val current = state.getExtractedFluidVolume(key).coerceAtMost(fluid.generatedVolume)
            val remaining = fluid.generatedVolume - current
            if (remaining <= 0) {
                null
            } else {
                val changedVolume = volume.coerceAtMost(remaining)
                state.addExtractedFluidVolume(key, changedVolume)
                MiningAmountChange(
                    changedAmount = changedVolume,
                    totalChangedAmount = current + changedVolume,
                    remainingAmount = fluid.generatedVolume - current - changedVolume,
                )
            }
        } ?: return null

        return FluidExtractionResult(
            fluid = fluid.fluid,
            pos = veinPos,
            requestedVolume = volume,
            extractedVolume = change.changedAmount,
            remainingVolume = change.remainingAmount,
        )
    }
}