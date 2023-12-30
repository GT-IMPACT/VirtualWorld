package space.gtimpact.virtual_world.api.prospect

import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import space.gtimpact.virtual_world.api.getFluidLayer
import space.gtimpact.virtual_world.api.getOreLayer0
import space.gtimpact.virtual_world.api.getOreLayer1
import space.gtimpact.virtual_world.common.items.ScannerTool
import space.gtimpact.virtual_world.network.FindVeinsPacket
import space.gtimpact.virtual_world.network.VirtualOresNetwork


fun scanOres(w: World, layer: Int, player: EntityPlayerMP, radius: Int) {

    val chX = player.posX.toInt() shr 4
    val chZ = player.posZ.toInt() shr 4

    val chunks: ArrayList<Chunk> = ArrayList()

    for (x in -radius..radius) {
        for (z in -radius..radius) {
            if (x != -radius && x != radius && z != -radius && z != radius) {
                chunks += w.getChunkFromChunkCoords(chX + x, chZ + z)
            }
        }
    }

    val packet = FindVeinsPacket(chX, chZ, player.posX.toInt(), player.posZ.toInt(), radius - 1, ScannerTool.TYPE_ORES)

    for (chunk in chunks)
        scanOreChunk(chunk, packet, layer)

    packet.level = radius - 1

    VirtualOresNetwork.sendToPlayer(packet, player)
}

private fun scanOreChunk(chunk: Chunk, packet: FindVeinsPacket, layer: Int) {
    val count = when(layer) {
        0 -> chunk.getOreLayer0()
        1 -> chunk.getOreLayer1()
        else -> null
    }

    if (count != null) {
        val size = count.size.toDouble() / count.vein.rangeSize.last.toDouble() * 100.0
        fillPacketForChunk(chunk, packet, count.vein.id, size.toInt() / 1000)
    }
}

fun scanFluids(w: World, player: EntityPlayerMP, radius: Int) {

    val chX = player.posX.toInt() shr 4
    val chZ = player.posZ.toInt() shr 4

    val chunks: ArrayList<Chunk> = ArrayList()

    for (x in -radius..radius) {
        for (z in -radius..radius) {
            if (x != -radius && x != radius && z != -radius && z != radius) {
                chunks += w.getChunkFromChunkCoords(chX + x, chZ + z)
            }
        }
    }

    val packet = FindVeinsPacket(chX, chZ, player.posX.toInt(), player.posZ.toInt(), radius - 1, ScannerTool.TYPE_FLUIDS)

    for (chunk in chunks)
        scanFluidChunk(chunk, packet)


    packet.level = radius - 1

    VirtualOresNetwork.sendToPlayer(packet, player)
}

private fun scanFluidChunk(chunk: Chunk, packet: FindVeinsPacket) {
    val count = chunk.getFluidLayer()
    if (count != null && count.vein.rangeSize.last > 0) {
        val size = count.size.toDouble() / count.vein.rangeSize.last.toDouble() * 100.0
        fillPacketForChunk(chunk, packet, count.vein.id, size.toInt() / 1000)
    }
}

private fun fillPacketForChunk(chunk: Chunk, packet: FindVeinsPacket, idComponent: Int, size: Int) {
    for (xx in 0..15) {
        for (zz in 0..15) {
            packet.addRenderComponent(chunk.xPosition * 16 + xx, chunk.zPosition * 16 + zz, idComponent, size)
        }
    }
}
