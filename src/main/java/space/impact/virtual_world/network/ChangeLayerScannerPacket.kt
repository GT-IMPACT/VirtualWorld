package space.impact.virtual_world.network

import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteStreams
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.common.DimensionManager
import space.impact.virtual_world.common.items.ScannerTool

class ChangeLayerScannerPacket(
    private val dim: Int = 0,
    private val playerId: Int = 0,
): IPacket {

    override fun getPacketID(): Int {
        return 1
    }

    @Suppress("UnstableApiUsage")
    override fun encode(): ByteArray {
        val out = ByteStreams.newDataOutput(1)
        out.writeInt(dim)
        out.writeInt(playerId)
        return out.toByteArray()
    }

    override fun process() {
        DimensionManager.getWorld(dim)?.let { world ->
            (world.getEntityByID(playerId) as? EntityPlayer)?.let { player ->
                player.heldItem?.let { stack ->
                    (stack.item as ScannerTool).changeLayer(player, stack)
                }
            }
        }
    }

    override fun decode(data: ByteArrayDataInput): IPacket {
        return ChangeLayerScannerPacket(dim = data.readInt(), playerId = data.readInt())
    }
}