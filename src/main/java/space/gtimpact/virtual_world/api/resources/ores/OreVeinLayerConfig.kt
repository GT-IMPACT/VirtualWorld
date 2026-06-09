package space.gtimpact.virtual_world.api.resources.ores

data class OreVeinLayerConfig(
    val layerIndex: Int,
    val balanceAreaVeins: Int = 128,
    val emptyWeight: Double = 0.0,
)
