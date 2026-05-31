package space.gtimpact.virtual_world.api.game.scanner.fluids

import space.gtimpact.virtual_world.api.game.scanner.VeinKey

internal fun ClientFluidScanState.ClientFluidVeinState.toVeinKey(): VeinKey {
    return VeinKey(
        veinId = vein.id,
        veinX = position.x,
        veinZ = position.z,
    )
}
