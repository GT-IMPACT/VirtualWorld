package space.gtimpact.virtual_world.api.game.scanner.ores

import space.gtimpact.virtual_world.api.VirtualAPI
import space.gtimpact.virtual_world.api.core.ResourcePos
import space.gtimpact.virtual_world.api.services.scanning.ores.OreScanReport

internal fun OreScanReport.toPacket(): OreScanPacketDto {
    val report = this
    return OreScanPacketDto(
        dimensionId = report.dimensionId,
        layerIndex = report.layerIndex,
        veins = report.results.map { vein ->
            val hasAmounts = vein.generatedAmount != null
            OreScanPacketDto.OreVeinPacketDto(
                veinX = vein.pos.x,
                veinZ = vein.pos.z,
                resourceId = vein.ore.id.toShort(),
                totalGenerated = if (hasAmounts) vein.generatedAmount else null,
                totalMined = if (hasAmounts) vein.minedAmount else null,
                totalRemaining = if (hasAmounts) vein.remainingAmount else null,
                chunks = if (hasAmounts) {
                    vein.chunks.map { chunk ->
                        OreScanPacketDto.OreChunkPacketDto(
                            chunkX = chunk.deposit.chunkPos.x,
                            chunkZ = chunk.deposit.chunkPos.z,
                            generatedAmount = chunk.generatedAmount,
                            minedAmount = chunk.minedAmount,
                            remainingAmount = chunk.remainingAmount,
                        )
                    }
                } else {
                    emptyList()
                }
            )
        }
    )
}

internal fun OreScanPacketDto.toData(): ClientOreScanState {
    val packet = this
    val veins = packet.veins.mapNotNull { veinPacket ->

        val vein = VirtualAPI.resourcesRegistry
            .getOreVein(veinPacket.resourceId.toInt())
            ?: return@mapNotNull null

        ClientOreScanState.ClientOreVeinState(
            vein = vein,

            generatedAmount = veinPacket.totalGenerated ?: 0,
            minedAmount = veinPacket.totalMined ?: 0,
            remainingAmount = veinPacket.totalRemaining ?: 0,

            position = ResourcePos(
                x = veinPacket.veinX,
                z = veinPacket.veinZ,
            ),

            chunks = veinPacket.chunks.map { chunk ->
                ClientOreScanState.ClientOreChunkState(
                    chunkX = chunk.chunkX,
                    chunkZ = chunk.chunkZ,

                    generatedAmount = chunk.generatedAmount,
                    minedAmount = chunk.minedAmount,
                    remainingAmount = chunk.remainingAmount
                )
            }
        )
    }

    return ClientOreScanState(
        dimensionId = packet.dimensionId,
        layer = packet.layerIndex,
        veins = veins
    )
}

internal fun ClientOreScanState.toDto(): OreScanPacketDto {
    val scan = this
    return OreScanPacketDto(
        dimensionId = scan.dimensionId,
        layerIndex = scan.layer,
        veins = scan.veins.map { vein ->
            OreScanPacketDto.OreVeinPacketDto(
                veinX = vein.position.x,
                veinZ = vein.position.z,
                resourceId = vein.vein.id.toShort(),
                totalGenerated = vein.generatedAmount,
                totalMined = vein.minedAmount,
                totalRemaining = vein.remainingAmount,
                chunks = vein.chunks.map { chunk ->
                    OreScanPacketDto.OreChunkPacketDto(
                        chunkX = chunk.chunkX,
                        chunkZ = chunk.chunkZ,
                        generatedAmount = chunk.generatedAmount,
                        minedAmount = chunk.minedAmount,
                        remainingAmount = chunk.remainingAmount,
                    )
                }
            )
        },
    )
}
