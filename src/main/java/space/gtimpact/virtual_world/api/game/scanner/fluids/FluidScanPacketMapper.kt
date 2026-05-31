package space.gtimpact.virtual_world.api.game.scanner.fluids

import space.gtimpact.virtual_world.api.VirtualAPI
import space.gtimpact.virtual_world.api.core.ResourcePos
import space.gtimpact.virtual_world.api.services.scanning.fluids.FluidScanReport

internal fun FluidScanReport.toPacket(): FluidScanPacketDto {
    val report = this
    return FluidScanPacketDto(
        dimensionId = report.dimensionId,
        veins = report.results.map { vein ->
            val hasAmounts = vein.generatedAmount != null
            FluidScanPacketDto.FluidVeinPacketDto(
                veinX = vein.pos.x,
                veinZ = vein.pos.z,
                resourceId = vein.fluid.id.toShort(),
                totalGenerated = if (hasAmounts) vein.generatedAmount else null,
                totalMined = if (hasAmounts) vein.extractedAmount else null,
                totalRemaining = if (hasAmounts) vein.remainingAmount else null,
            )
        }
    )
}

internal fun FluidScanPacketDto.toData(): ClientFluidScanState {
    val packet = this
    val veins = packet.veins.mapNotNull { veinPacket ->

        val vein = VirtualAPI.resourcesRegistry
            .getFluidVein(veinPacket.resourceId.toInt())
            ?: return@mapNotNull null

        ClientFluidScanState.ClientFluidVeinState(
            vein = vein,

            generatedAmount = veinPacket.totalGenerated ?: 0,
            minedAmount = veinPacket.totalMined ?: 0,
            remainingAmount = veinPacket.totalRemaining ?: 0,

            position = ResourcePos(
                x = veinPacket.veinX,
                z = veinPacket.veinZ,
            ),

        )
    }

    return ClientFluidScanState(
        dimensionId = packet.dimensionId,
        veins = veins,
    )
}

internal fun ClientFluidScanState.toDto(): FluidScanPacketDto {
    val scan = this
    return FluidScanPacketDto(
        dimensionId = scan.dimensionId,
        veins = scan.veins.map { vein ->
            FluidScanPacketDto.FluidVeinPacketDto(
                veinX = vein.position.x,
                veinZ = vein.position.z,
                resourceId = vein.vein.id.toShort(),
                totalGenerated = vein.generatedAmount,
                totalMined = vein.minedAmount,
                totalRemaining = vein.remainingAmount,
            )
        }
    )
}
