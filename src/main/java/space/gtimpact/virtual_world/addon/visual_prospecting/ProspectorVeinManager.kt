package space.gtimpact.virtual_world.addon.visual_prospecting

import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteArrayDataOutput
import com.google.common.io.ByteStreams
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.chunk.Chunk
import space.gtimpact.virtual_world.api.RegionRes
import space.gtimpact.virtual_world.api.ResourceGenerator
import space.gtimpact.virtual_world.api.VirtualOreVein
import space.gtimpact.virtual_world.api.ores.ChunkOre
import space.gtimpact.virtual_world.api.ores.VeinOre
import space.gtimpact.virtual_world.network.prospectorPacketOre
import space.impact.packet_network.network.NetworkHandler.sendToPlayer

object ProspectorVeinManager {

    fun createArea(chunks: ArrayList<VirtualOreVeinPosition>, current: Chunk, player: EntityPlayer, layer: Int) {

        val chunkGroup = chunks.groupBy {
            (it.x shr ResourceGenerator.SHIFT_CHUNK_FROM_VEIN) to (it.z shr ResourceGenerator.SHIFT_CHUNK_FROM_VEIN)
        }.filter {
            it.value.size == 16
        }.map { (pair, chunks) ->
            PacketDataOreVein(
                x = pair.first,
                z = pair.second,
                veinId = chunks.first().vein.id,
                chunks = chunks.map {
                    PacketDataOreVeinChunk(
                        x = it.x,
                        z = it.z,
                        size = it.size,
                    )
                }
            )
        }.let {
            PacketDataOreVeinList(layer, it)
        }

        @Suppress("UnstableApiUsage")
        val buf = ByteStreams.newDataOutput()

        buf.writeByte(chunkGroup.layer)
        buf.writeShort(chunkGroup.veins.size)
        chunkGroup.veins.forEach { it.write(buf) }

        if (!current.worldObj.isRemote)
            player.sendToPlayer(prospectorPacketOre.transaction(buf))
    }
}

fun PacketDataOreVein.write(buf: ByteArrayDataOutput) {
    buf.writeShort(veinId)
    buf.writeInt(x)
    buf.writeInt(z)
    buf.writeShort(chunks.size)
    for (chunk in chunks) {
        buf.writeInt(chunk.x)
        buf.writeInt(chunk.z)
        buf.writeByte(chunk.size)
    }
}

fun ByteArrayDataInput.readPacketDataOreVein(): PacketDataOreVeinList {
    return PacketDataOreVeinList(
        layer = readByte().toInt(),
        veins = Array(readShort().toInt()) {
            PacketDataOreVein(
                veinId = readShort().toInt(),
                x = readInt(),
                z = readInt(),
                chunks = Array(readShort().toInt()) {
                    PacketDataOreVeinChunk(
                        x = readInt(),
                        z = readInt(),
                        size = readByte().toInt(),
                    )
                }.toList()
            )
        }.toList()
    )
}

data class PacketDataOreVeinList(
    val layer: Int, //byte (0..1)
    val veins: List<PacketDataOreVein>
)

data class PacketDataOreVein(
    val veinId: Int, //short
    val x: Int,
    val z: Int,
    val chunks: List<PacketDataOreVeinChunk>,
) {
    var dimension: Int = 0
    var vein: VirtualOreVein? = null
}

data class PacketDataOreVeinChunk(
    val x: Int,
    val z: Int,
    val size: Int, //byte (percent 0..100)
)
