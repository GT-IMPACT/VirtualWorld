package space.gtimpact.virtual_world.api.game.scanner.ores

import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteArrayDataOutput

internal fun OreScanPacketDto.toDO(out: ByteArrayDataOutput) {
    val packet = this

    out.writeInt(packet.dimensionId)
    out.writeInt(packet.layerIndex)

    out.writeInt(packet.veins.size)

    for (vein in packet.veins) {

        out.writeInt(vein.veinX)
        out.writeInt(vein.veinZ)

        out.writeShort(vein.resourceId.toInt())

        val hasAmounts = vein.totalGenerated != null
        val hasChunks = vein.chunks.isNotEmpty()

        var flags = 0
        if (hasAmounts) flags = flags or 1
        if (hasChunks) flags = flags or 2

        out.writeByte(flags)

        if (hasAmounts) {
            out.writeInt(vein.totalGenerated)
            out.writeInt(vein.totalMined ?: 0)
            out.writeInt(vein.totalRemaining ?: 0)
        }

        if (hasChunks) {
            out.writeInt(vein.chunks.size)

            for (chunk in vein.chunks) {

                out.writeInt(chunk.chunkX)
                out.writeInt(chunk.chunkZ)

                out.writeInt(chunk.generatedAmount)
                out.writeInt(chunk.minedAmount)
                out.writeInt(chunk.remainingAmount)
            }
        }
    }
}

internal fun ByteArrayDataInput.toPacketDataOreVein(): OreScanPacketDto {
    val input = this

    val dimensionId = input.readInt()
    val layerIndex = input.readInt()

    val veinCount = input.readInt()

    val veins = ArrayList<OreScanPacketDto.OreVeinPacketDto>(veinCount)

    repeat(veinCount) {

        val veinX = input.readInt()
        val veinZ = input.readInt()

        val resourceId = input.readShort()

        val flags = input.readByte().toInt()

        val hasAmounts = flags and 1 != 0
        val hasChunks = flags and 2 != 0

        val totalGenerated = if (hasAmounts) input.readInt() else null
        val totalMined = if (hasAmounts) input.readInt() else null
        val totalRemaining = if (hasAmounts) input.readInt() else null

        val chunks = if (hasChunks) {

            val chunkCount = input.readInt()
            val list = ArrayList<OreScanPacketDto.OreChunkPacketDto>(chunkCount)

            repeat(chunkCount) {
                list += OreScanPacketDto.OreChunkPacketDto(
                    chunkX = input.readInt(),
                    chunkZ = input.readInt(),
                    generatedAmount = input.readInt(),
                    minedAmount = input.readInt(),
                    remainingAmount = input.readInt()
                )
            }

            list
        } else {
            emptyList()
        }

        veins += OreScanPacketDto.OreVeinPacketDto(
            veinX = veinX,
            veinZ = veinZ,
            resourceId = resourceId,
            totalGenerated = totalGenerated,
            totalMined = totalMined,
            totalRemaining = totalRemaining,
            chunks = chunks
        )
    }

    return OreScanPacketDto(
        dimensionId = dimensionId,
        layerIndex = layerIndex,
        veins = veins
    )
}
