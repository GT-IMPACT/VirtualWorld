package space.gtimpact.virtual_world.api.prospect

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import space.gtimpact.virtual_world.addon.visual_prospecting.ProspectorVeinManager
import space.gtimpact.virtual_world.addon.visual_prospecting.VirtualFluidVeinPosition
import space.gtimpact.virtual_world.addon.visual_prospecting.VirtualOreVeinPosition
import space.gtimpact.virtual_world.api.getFluidLayer
import space.gtimpact.virtual_world.api.getOreLayer0
import space.gtimpact.virtual_world.api.getOreLayer1
import space.gtimpact.virtual_world.common.items.ScannerTool
import space.gtimpact.virtual_world.network.FindVeinsPacket
import space.gtimpact.virtual_world.network.VirtualOresNetwork
import space.gtimpact.virtual_world.network.prospectorPacketFluid
import space.gtimpact.virtual_world.network.sendPacket

@JvmOverloads
fun scanOres(w: World, layer: Int, player: EntityPlayer, radius: Int, needShowGui: Boolean = true) {

    val chX = player.posX.toInt() shr 4
    val chZ = player.posZ.toInt() shr 4

    val packet = FindVeinsPacket(w.provider.dimensionId, chX, chZ, player.posX.toInt(), player.posZ.toInt(), radius - 1, ScannerTool.TYPE_ORES, layer)

    val chunksRes = arrayListOf<VirtualOreVeinPosition>()

    for (x in -radius..radius) {
        for (z in -radius..radius) {
            if (x != -radius && x != radius && z != -radius && z != radius) {
                val chunk = w.getChunkFromChunkCoords(chX + x, chZ + z)
                val veinPos = scanOreChunk(chunk, layer)
                if (veinPos != null) {
                    chunksRes += veinPos
                    if (needShowGui)
                        fillPacketForChunk(chunk, packet, veinPos.vein.id, veinPos.size)
                }
            }
        }
    }

    if (needShowGui)
        VirtualOresNetwork.sendToPlayer(packet, player)

    ProspectorVeinManager.createArea(chunksRes, w.getChunkFromChunkCoords(chX, chZ), player, layer)
}


private fun scanOreChunk(chunk: Chunk, layer: Int): VirtualOreVeinPosition? {
    val count = when (layer) {
        0 -> chunk.getOreLayer0()
        1 -> chunk.getOreLayer1()
        else -> null
    }

    return if (count != null) {
        VirtualOreVeinPosition(
            dimId = chunk.worldObj.provider.dimensionId,
            x = chunk.xPosition,
            z = chunk.zPosition,
            vein = count.vein,
            size = count.size * 100 / (count.vein.rangeSize.last * 1000)
        )
    } else null
}

@JvmOverloads
fun scanFluids(w: World, player: EntityPlayer, radius: Int, needShowGui: Boolean = true) {

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

    val packet = FindVeinsPacket(w.provider.dimensionId, chX, chZ, player.posX.toInt(), player.posZ.toInt(), radius - 1, ScannerTool.TYPE_FLUIDS)

    val list = arrayListOf<VirtualFluidVeinPosition>()

    for (chunk in chunks)
        scanFluidChunk(chunk, packet)?.also { list += it }

    if (needShowGui)
        VirtualOresNetwork.sendToPlayer(packet, player)

    val listInts = arrayListOf<Int>()

    listInts += list.size

    list.forEach {
        listInts += it.vein.id
        listInts += it.x
        listInts += it.z
        listInts += it.size
    }

    if (!w.isRemote)
        player.sendPacket(prospectorPacketFluid.transaction(*listInts.toIntArray()))
}

private fun scanFluidChunk(chunk: Chunk, packet: FindVeinsPacket): VirtualFluidVeinPosition? {
    val count = chunk.getFluidLayer()
    return if (count != null && count.vein.rangeSize.last > 0) {
        val size = count.size.toDouble() / count.vein.rangeSize.last.toDouble() * 100.0
        fillPacketForChunk(chunk, packet, count.vein.id, size.toInt() / 1000)
        VirtualFluidVeinPosition(
            dimId = chunk.worldObj.provider.dimensionId,
            x = chunk.xPosition,
            z = chunk.zPosition,
            vein = count.vein,
            size = count.size * 100 / (count.vein.rangeSize.last * 1000)
        )
    } else null
}

private fun fillPacketForChunk(chunk: Chunk, packet: FindVeinsPacket, idComponent: Int, size: Int) {
    packet.addRenderComponent(chunk.xPosition * 16, chunk.zPosition * 16, idComponent, size)
}
