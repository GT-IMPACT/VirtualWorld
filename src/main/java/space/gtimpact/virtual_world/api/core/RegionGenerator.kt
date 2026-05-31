package space.gtimpact.virtual_world.api.core

import space.gtimpact.virtual_world.api.resources.VirtualResource
import space.gtimpact.virtual_world.api.resources.VirtualResourcesGenerator

class RegionGenerator(
    private val generators: List<VirtualResourcesGenerator>,
) {

    fun generateRegion(
        dimensionId: Int,
        pos: RegionPos,
    ): GeneratedRegion {
        val origin = pos.toWorldOrigin()

        val resources = mutableListOf<VirtualResource>()

        val baseResourceX = pos.x shl REGION_TO_VEIN_BITS
        val baseResourceZ = pos.z shl REGION_TO_VEIN_BITS

        for (localVeinX in 0 until WorldGrid.REGION_VEINS) {
            for (localVeinZ in 0 until WorldGrid.REGION_VEINS) {

                val resourcePos = ResourcePos(
                    x = baseResourceX + localVeinX,
                    z = baseResourceZ + localVeinZ,
                )

                generators.forEach { generator ->
                    val resource = generator.generate(
                        dimensionId = dimensionId,
                        pos = resourcePos,
                    )
                    if (resource != null) {
                        resources += resource
                    }
                }
            }
        }

        return GeneratedRegion(
            dimensionId = dimensionId,
            pos = pos,
            origin = origin,
            resources = resources,
        )
    }

    private companion object {
        const val REGION_TO_VEIN_BITS = 3
    }
}
