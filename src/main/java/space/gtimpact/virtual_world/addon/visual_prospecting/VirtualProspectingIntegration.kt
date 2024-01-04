package space.gtimpact.virtual_world.addon.visual_prospecting

import com.sinthoras.visualprospecting.VisualProspecting_API
import com.sinthoras.visualprospecting.integration.journeymap.JourneyMapState
import com.sinthoras.visualprospecting.integration.journeymap.buttons.LayerButton
import com.sinthoras.visualprospecting.integration.journeymap.buttons.OreVeinButton
import com.sinthoras.visualprospecting.integration.journeymap.buttons.UndergroundFluidButton
import com.sinthoras.visualprospecting.integration.model.buttons.ButtonManager
import space.gtimpact.virtual_world.addon.visual_prospecting.layer.VirtualFluidsLayerManager
import space.gtimpact.virtual_world.addon.visual_prospecting.layer.VirtualFluidsLayerRender
import space.gtimpact.virtual_world.addon.visual_prospecting.layer.VirtualOresLayerManager
import space.gtimpact.virtual_world.addon.visual_prospecting.layer.VirtualOresLayerRender

object VirtualProspectingIntegration {

    private val virtualOresL0ButtonManager = ButtonManager("visualprospecting.button.virtualores.l0", "oreveins")
    private val virtualOresL0LayerButton = LayerButton(virtualOresL0ButtonManager)

    private val virtualOresL1ButtonManager = ButtonManager("visualprospecting.button.virtualores.l1", "oreveins")
    private val virtualOresL1LayerButton = LayerButton(virtualOresL1ButtonManager)

    private val virtualFluidsButtonManager = ButtonManager("visualprospecting.button.virtualfluids", "undergroundfluid")
    private val virtualFluidsButton = LayerButton(virtualFluidsButtonManager)

    private val virtualOresL0Manager = VirtualOresLayerManager(0, virtualOresL0ButtonManager)
    private val virtualOresL1Manager = VirtualOresLayerManager(1, virtualOresL1ButtonManager)

    private val virtualOresL0Render = VirtualOresLayerRender(virtualOresL0Manager)
    private val virtualOresL1Render = VirtualOresLayerRender(virtualOresL1Manager)

    private val virtualFluidsManager = VirtualFluidsLayerManager(virtualFluidsButtonManager)
    private val virtualFluidsRender = VirtualFluidsLayerRender(virtualFluidsManager)

    private fun unRegisterGT() {
        JourneyMapState.instance.buttons.remove(UndergroundFluidButton.instance)
        JourneyMapState.instance.buttons.remove(OreVeinButton.instance)
    }

    private fun register() {

        VisualProspecting_API.LogicalClient.registerCustomButtonManager(virtualOresL1ButtonManager)
        VisualProspecting_API.LogicalClient.registerJourneyMapButton(virtualOresL1LayerButton)

        VisualProspecting_API.LogicalClient.registerCustomButtonManager(virtualOresL0ButtonManager)
        VisualProspecting_API.LogicalClient.registerJourneyMapButton(virtualOresL0LayerButton)

        VisualProspecting_API.LogicalClient.registerCustomLayer(virtualOresL0Manager)
        VisualProspecting_API.LogicalClient.registerCustomLayer(virtualOresL1Manager)

        VisualProspecting_API.LogicalClient.registerJourneyMapRenderer(virtualOresL0Render)
        VisualProspecting_API.LogicalClient.registerJourneyMapRenderer(virtualOresL1Render)

        VisualProspecting_API.LogicalClient.registerCustomButtonManager(virtualFluidsButtonManager)
        VisualProspecting_API.LogicalClient.registerJourneyMapButton(virtualFluidsButton)

        VisualProspecting_API.LogicalClient.registerCustomLayer(virtualFluidsManager)
        VisualProspecting_API.LogicalClient.registerJourneyMapRenderer(virtualFluidsRender)
    }

    fun postInit() {
        unRegisterGT()
        register()
    }
}
