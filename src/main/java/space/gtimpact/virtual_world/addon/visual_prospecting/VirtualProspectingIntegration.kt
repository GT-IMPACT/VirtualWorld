package space.gtimpact.virtual_world.addon.visual_prospecting

import com.sinthoras.visualprospecting.VisualProspecting_API
import com.sinthoras.visualprospecting.integration.journeymap.buttons.LayerButton
import com.sinthoras.visualprospecting.integration.model.buttons.ButtonManager
import space.gtimpact.virtual_world.addon.visual_prospecting.layer.*

object VirtualProspectingIntegration {

    private val virtualOresL0ButtonManager = ButtonManager("visualprospecting.button.virtualores.l0", "oreveins")
    private val virtualOresL0LayerButton = LayerButton(virtualOresL0ButtonManager)

    private val virtualOresL1ButtonManager = ButtonManager("visualprospecting.button.virtualores.l1", "oreveins")
    private val virtualOresL1LayerButton = LayerButton(virtualOresL1ButtonManager)

    private val virtualFluidsButtonManager = ButtonManager("visualprospecting.button.virtualfluids", "undergroundfluid")
    private val virtualFluidsButton = LayerButton(virtualFluidsButtonManager)

    private val objectsButtonManager = ButtonManager("visualprospecting.button.objects", "gps")
    private val objectsButton = LayerButton(objectsButtonManager)

    private val virtualOresL0Manager = VirtualOresLayerManager(0, virtualOresL0ButtonManager)
    private val virtualOresL1Manager = VirtualOresLayerManager(1, virtualOresL1ButtonManager)

    private val virtualOresL0Render = VirtualOresLayerRender(virtualOresL0Manager)
    private val virtualOresL1Render = VirtualOresLayerRender(virtualOresL1Manager)

    private val virtualFluidsManager = VirtualFluidsLayerManager(virtualFluidsButtonManager)
    private val virtualFluidsRender = VirtualFluidsLayerRender(virtualFluidsManager)

    private val objectsManager = ObjectsLayerManager(objectsButtonManager)
    private val objectsRender = ObjectsLayerRender(objectsManager)

    private fun register() {
        VisualProspecting_API.LogicalClient.registerCustomButtonManager(virtualOresL0ButtonManager)
        VisualProspecting_API.LogicalClient.registerJourneyMapButton(virtualOresL0LayerButton)
        VisualProspecting_API.LogicalClient.registerCustomLayer(virtualOresL0Manager)
        VisualProspecting_API.LogicalClient.registerJourneyMapRenderer(virtualOresL0Render)

        VisualProspecting_API.LogicalClient.registerCustomButtonManager(virtualOresL1ButtonManager)
        VisualProspecting_API.LogicalClient.registerJourneyMapButton(virtualOresL1LayerButton)
        VisualProspecting_API.LogicalClient.registerCustomLayer(virtualOresL1Manager)
        VisualProspecting_API.LogicalClient.registerJourneyMapRenderer(virtualOresL1Render)

        VisualProspecting_API.LogicalClient.registerCustomButtonManager(virtualFluidsButtonManager)
        VisualProspecting_API.LogicalClient.registerJourneyMapButton(virtualFluidsButton)
        VisualProspecting_API.LogicalClient.registerCustomLayer(virtualFluidsManager)
        VisualProspecting_API.LogicalClient.registerJourneyMapRenderer(virtualFluidsRender)

        VisualProspecting_API.LogicalClient.registerCustomButtonManager(objectsButtonManager)
        VisualProspecting_API.LogicalClient.registerJourneyMapButton(objectsButton)
        VisualProspecting_API.LogicalClient.registerCustomLayer(objectsManager)
        VisualProspecting_API.LogicalClient.registerJourneyMapRenderer(objectsRender)
    }

    fun postInit() {
        register()
    }
}
