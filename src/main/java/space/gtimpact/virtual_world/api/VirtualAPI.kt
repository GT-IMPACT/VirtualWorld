@file:Suppress("unused")

package space.gtimpact.virtual_world.api

import space.gtimpact.virtual_world.VirtualOres
import space.gtimpact.virtual_world.api.core.VirtualWorld
import space.gtimpact.virtual_world.api.game.scanner.ScannerManager
import space.gtimpact.virtual_world.api.resources.VirtualWorldResourcesRegistry
import space.gtimpact.virtual_world.api.services.mining.MiningService
import space.gtimpact.virtual_world.api.services.scanning.ScanningService

/**
 * Virtual Ore API
 */
object VirtualAPI {

    val virtualWorld: VirtualWorld?
        get() = VirtualOres.proxy.virtualWorldProvider.instance

    val mining: MiningService
        get() = requireNotNull(virtualWorld).mining

    val scanning: ScanningService
        get() = requireNotNull(virtualWorld).scanning

    val scannerManager: ScannerManager = ScannerManager()

    val resourcesRegistry = VirtualWorldResourcesRegistry()
}
