package space.gtimpact.virtual_world.addon.visual_prospecting.layer.ores

import com.sinthoras.visualprospecting.integration.journeymap.render.LayerRenderer
import com.sinthoras.visualprospecting.integration.model.layers.LayerManager
import com.sinthoras.visualprospecting.integration.model.locations.ILocationProvider
import journeymap.client.render.draw.DrawStep

class OresLayerRender(
    layerManager: LayerManager,
) : LayerRenderer(layerManager) {

    override fun mapLocationProviderToDrawStep(
        visibleElements: List<ILocationProvider>
    ): List<DrawStep> {
        return visibleElements
            .asSequence()
            .mapNotNull { it as? OresLocation }
            .sortedWith(compareBy { it.isVeinOrigin })
            .map { OresDrawStep(it) }
            .toList()
    }
}