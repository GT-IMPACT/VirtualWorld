package space.gtimpact.virtual_world.proxy.debug

import com.google.common.io.ByteStreams
import net.minecraft.client.Minecraft
import space.gtimpact.virtual_world.api.game.scanner.ores.ClientOreScanState
import space.gtimpact.virtual_world.network.MineFromClientOrePacket
import space.impact.packet_network.network.NetworkHandler.sendToServer

fun digSelectedVein(veinState: ClientOreScanState.ClientOreVeinState) {
    val player = Minecraft.getMinecraft().thePlayer ?: return

    val buf = ByteStreams.newDataOutput()

    buf.writeInt(veinState.vein.layer)
    buf.writeInt(veinState.position.x)
    buf.writeInt(veinState.position.z)

    player.sendToServer(MineFromClientOrePacket.transaction(buf))
}
