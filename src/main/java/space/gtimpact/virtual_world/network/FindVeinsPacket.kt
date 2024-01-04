package space.gtimpact.virtual_world.network

import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteStreams
import net.minecraft.world.ChunkCoordIntPair
import space.gtimpact.virtual_world.VirtualOres
import space.gtimpact.virtual_world.api.VirtualAPI
import space.gtimpact.virtual_world.client.gui.ScannerGui
import space.gtimpact.virtual_world.client.gui.widgets.RenderMapTexture
import space.gtimpact.virtual_world.common.items.ScannerTool.Companion.TYPE_FLUIDS
import space.gtimpact.virtual_world.common.items.ScannerTool.Companion.TYPE_ORES
import space.gtimpact.virtual_world.util.Math.repeatOffset

typealias RenderMap = HashMap<ChunkCoordIntPair, RenderComponent?>

data class RenderComponent(
    val idComponent: Int,
    val amount: Int,
)

class FindVeinsPacket(
    val dimId: Int = 0,
    val chunkX: Int = 0,
    val chunkZ: Int = 0,
    val centerX: Int = 0,
    val centerZ: Int = 0,
    val radius: Int = 0,
    val type: Int = 0,
    val layer: Int = 0,
) : IPacket {

    val map: RenderMap = RenderMap()

    val ores: HashMap<String, Int> = HashMap()
    val metaMap: HashMap<Short, String> = HashMap()

    private fun addComponent(idComponent: Int, type: Int) {
        when (type) {
            TYPE_ORES -> VirtualAPI.getRegisterOres().find { it.id == idComponent }?.also {
                if (!it.isHidden) {
                    ores[it.name] = it.color
                    metaMap[idComponent.toShort()] = it.name
                }
            }

            TYPE_FLUIDS -> VirtualAPI.getRegisterFluids().find { it.id == idComponent }?.also {
                if (!it.isHidden) {
                    ores[it.name] = it.color
                    metaMap[idComponent.toShort()] = it.name
                }
            }
        }
    }

    fun addRenderComponent(chX: Int, chZ: Int, idComponent: Int, amount: Int) {
        val chunk = ChunkCoordIntPair(
            chX - (chunkX - radius) * 16,
            chZ - (chunkZ - radius) * 16,
        )
        val component = RenderComponent(idComponent, amount)
        map[chunk] = component
    }

    override fun getPacketID(): Int {
        return 0
    }

    @Suppress("UnstableApiUsage")
    override fun encode(): ByteArray {
        val out = ByteStreams.newDataOutput(1)
        out.writeByte(dimId)
        out.writeInt(chunkX)
        out.writeInt(chunkZ)
        out.writeInt(centerX)
        out.writeInt(centerZ)
        out.writeByte(radius)
        out.writeByte(type)
        out.writeByte(layer)

        val radius: Int = (radius * 2 + 1) * 16

        repeatOffset(0, radius - 1, 16) { x ->
            repeatOffset(0, radius - 1, 16) { z ->
                val coordinates = ChunkCoordIntPair(x, z)
                if (map[coordinates] == null) {
                    out.writeShort(0) // idComponent
                    out.writeByte(0) // amount
                } else {
                    map[coordinates]?.apply {
                        out.writeShort(idComponent)
                        out.writeByte(amount)
                    }
                }
            }
        }

        return out.toByteArray()
    }

    override fun process() {
        ScannerGui.create(RenderMapTexture(this))
        VirtualOres.proxy.openGui()
    }

    override fun decode(data: ByteArrayDataInput): IPacket {
        val packet = FindVeinsPacket(
            dimId = data.readByte().toInt(),
            chunkX = data.readInt(),
            chunkZ = data.readInt(),
            centerX = data.readInt(),
            centerZ = data.readInt(),
            radius = data.readByte().toInt(),
            type = data.readByte().toInt(),
            layer = data.readByte().toInt(),
        )

        val radius = (packet.radius * 2 + 1) * 16

        repeatOffset(0, radius - 1, 16) { z ->
            repeatOffset(0, radius - 1, 16) { x ->

                val idComponent = data.readShort().toInt()
                val amount = data.readByte().toInt()
                val component = RenderComponent(idComponent, amount)

                val coord = ChunkCoordIntPair(x, z)
                packet.map[coord] = component
                packet.addComponent(idComponent, packet.type)

            }
        }
        return packet
    }

    fun getSize(): Int {
        return (radius * 2 + 1) * 16
    }
}