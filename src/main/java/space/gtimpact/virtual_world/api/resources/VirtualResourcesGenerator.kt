package space.gtimpact.virtual_world.api.resources

import space.gtimpact.virtual_world.api.core.ResourcePos

interface VirtualResourcesGenerator {

    fun generate(dimensionId: Int, pos: ResourcePos): VirtualResource?
}
