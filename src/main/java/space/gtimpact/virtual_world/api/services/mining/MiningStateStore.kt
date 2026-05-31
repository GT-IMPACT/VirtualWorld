package space.gtimpact.virtual_world.api.services.mining

import space.gtimpact.virtual_world.api.services.mining.fluids.FluidVeinKey
import space.gtimpact.virtual_world.api.services.mining.ores.OreChunkKey

interface MiningStateStore {

    fun getMinedOreAmount(key: OreChunkKey): Int

    fun addMinedOreAmount(key: OreChunkKey, amount: Int)

    fun getExtractedFluidVolume(key: FluidVeinKey): Int

    fun addExtractedFluidVolume(key: FluidVeinKey, amount: Int)

    fun flush()
}
