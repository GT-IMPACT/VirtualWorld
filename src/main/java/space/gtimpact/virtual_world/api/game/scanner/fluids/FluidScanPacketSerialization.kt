package space.gtimpact.virtual_world.api.game.scanner.fluids

import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteArrayDataOutput

internal fun FluidScanPacketDto.toDO(out: ByteArrayDataOutput) {
    val packet = this

    out.writeInt(packet.dimensionId)

    out.writeInt(packet.veins.size)

    for (vein in packet.veins) {

        out.writeInt(vein.veinX)
        out.writeInt(vein.veinZ)

        out.writeShort(vein.resourceId.toInt())

        val hasAmounts = vein.totalGenerated != null
        out.writeBoolean(hasAmounts)

        if (hasAmounts) {
            out.writeInt(vein.totalGenerated)
            out.writeInt(vein.totalMined ?: 0)
            out.writeInt(vein.totalRemaining ?: 0)
        }
    }
}

internal fun ByteArrayDataInput.toPacketDataFluidVein(): FluidScanPacketDto {
    val input = this

    val dimensionId = input.readInt()

    val veinCount = input.readInt()

    val veins = ArrayList<FluidScanPacketDto.FluidVeinPacketDto>(veinCount)

    repeat(veinCount) {

        val veinX = input.readInt()
        val veinZ = input.readInt()

        val resourceId = input.readShort()

        val hasAmounts = input.readBoolean()

        val totalGenerated = if (hasAmounts) input.readInt() else null
        val totalMined = if (hasAmounts) input.readInt() else null
        val totalRemaining = if (hasAmounts) input.readInt() else null

        veins += FluidScanPacketDto.FluidVeinPacketDto(
            veinX = veinX,
            veinZ = veinZ,
            resourceId = resourceId,
            totalGenerated = totalGenerated,
            totalMined = totalMined,
            totalRemaining = totalRemaining,
        )
    }

    return FluidScanPacketDto(
        dimensionId = dimensionId,
        veins = veins,
    )
}
