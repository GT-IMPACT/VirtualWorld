package space.gtimpact.virtual_world.addon.visual_prospecting.layer.fluid

import com.sinthoras.visualprospecting.integration.journeymap.render.LayerRenderer
import com.sinthoras.visualprospecting.integration.model.layers.LayerManager
import com.sinthoras.visualprospecting.integration.model.locations.ILocationProvider
import journeymap.client.render.draw.DrawStep

class FluidsLayerRender(
    layerManager: LayerManager,
) : LayerRenderer(layerManager) {

    override fun mapLocationProviderToDrawStep(
        visibleElements: List<ILocationProvider>
    ): List<DrawStep> {
        return visibleElements
            .asSequence()
            .mapNotNull { it as? FluidsLocation }
            .map { FluidsDrawStep(it) }
            .toList()
    }
}