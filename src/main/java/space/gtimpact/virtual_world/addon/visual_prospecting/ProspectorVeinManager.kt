package space.gtimpact.virtual_world.addon.visual_prospecting

import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteArrayDataOutput
import com.google.common.io.ByteStreams
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.chunk.Chunk
import space.gtimpact.virtual_world.addon.visual_prospecting.cache.*
import space.gtimpact.virtual_world.api.ResourceGenerator
import space.gtimpact.virtual_world.network.prospectorPacketFluid
import space.gtimpact.virtual_world.network.prospectorPacketOre
import space.gtimpact.virtual_world.network.sendPacket

object ProspectorVeinManager {

    fun createArea(chunks: ArrayList<VirtualOreVeinPosition>, current: Chunk, player: EntityPlayer, layer: Int) {

        val chunkGroup = chunks.groupBy {
            (it.x shr ResourceGenerator.SHIFT_CHUNK_FROM_VEIN) to (it.z shr ResourceGenerator.SHIFT_CHUNK_FROM_VEIN)
        }.filter {
            it.value.size == 16
        }.map { (pair, chunks) ->
            CacheOreVein(
                x = pair.first,
                z = pair.second,
                veinId = chunks.first().vein.id,
                chunks = chunks.map {
                    CacheOreVeinChunk(
                        x = it.x,
                        z = it.z,
                        size = it.size,
                    )
                }
            )
        }.let {
            CacheOreVeinList(layer, it)
        }

        @Suppress("UnstableApiUsage")
        val buf = ByteStreams.newDataOutput()

        buf.writeByte(chunkGroup.layer)
        buf.writeShort(chunkGroup.veins.size)
        chunkGroup.veins.forEach { it.write(buf) }

        if (!current.worldObj.isRemote)
            player.sendPacket(prospectorPacketOre.transaction(buf))
    }

    fun createArea(chunks: ArrayList<VirtualFluidVeinPosition>, current: Chunk, player: EntityPlayer) {

        val chunkGroup = chunks.groupBy {
            (it.x shr ResourceGenerator.SHIFT_CHUNK_FROM_VEIN) to (it.z shr ResourceGenerator.SHIFT_CHUNK_FROM_VEIN)
        }.filter {
            it.value.size == 16
        }.map { (pair, chunks) ->
            CacheFluidVein(
                x = pair.first,
                z = pair.second,
                veinId = chunks.first().vein.id,
                chunks = chunks.map {
                    CacheFluidVeinChunk(
                        x = it.x,
                        z = it.z,
                        size = it.size,
                    )
                }
            )
        }

        @Suppress("UnstableApiUsage")
        val buf = ByteStreams.newDataOutput()

        buf.writeShort(chunkGroup.size)
        chunkGroup.forEach { it.write(buf) }

        if (!current.worldObj.isRemote)
            player.sendPacket(prospectorPacketFluid.transaction(buf))
    }
}

fun CacheOreVein.write(buf: ByteArrayDataOutput) {
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

fun ByteArrayDataInput.readPacketDataOreVein(): CacheOreVeinList {
    return CacheOreVeinList(
        layer = readByte().toInt(),
        veins = List(readShort().toInt()) {
            CacheOreVein(
                veinId = readShort().toInt(),
                x = readInt(),
                z = readInt(),
                chunks = List(readShort().toInt()) {
                    CacheOreVeinChunk(
                        x = readInt(),
                        z = readInt(),
                        size = readByte().toInt(),
                    )
                }
            )
        }
    )
}

fun CacheFluidVein.write(buf: ByteArrayDataOutput) {
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

fun ByteArrayDataInput.readPacketDataFluidVein(): List<CacheFluidVein> {
    return List(readShort().toInt()) {
        CacheFluidVein(
            veinId = readShort().toInt(),
            x = readInt(),
            z = readInt(),
            chunks = List(readShort().toInt()) {
                CacheFluidVeinChunk(
                    x = readInt(),
                    z = readInt(),
                    size = readByte().toInt(),
                )
            }.toList()
        )
    }
}
