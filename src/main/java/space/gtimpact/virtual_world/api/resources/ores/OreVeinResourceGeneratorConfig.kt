package space.gtimpact.virtual_world.api.resources.ores

import space.gtimpact.virtual_world.api.VirtualAPI

class OreVeinResourceGeneratorConfig(
    val layers: List<OreVeinLayerConfig>,
) {

    fun getOresFor(
        dimensionId: Int,
        layerIndex: Int,
    ): List<OreVein> {
        return VirtualAPI.resourcesRegistry.oreVeinsMap.values.filter { ore ->
            ore.canGenerateIn(
                dimensionId = dimensionId,
                layerIndex = layerIndex,
            )
        }
    }
}
