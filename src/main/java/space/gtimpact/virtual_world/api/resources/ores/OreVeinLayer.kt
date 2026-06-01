package space.gtimpact.virtual_world.api.resources.ores

data class OreVeinLayer(
    val layerIndex: Int,
    val ore: OreVein,
    val chunks: List<OreChunkDeposit>,
)
