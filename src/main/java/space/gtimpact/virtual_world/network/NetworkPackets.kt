package space.gtimpact.virtual_world.network

import net.minecraft.entity.player.EntityPlayer
import space.impact.packet_network.network.NetworkHandler.sendToPlayer
import space.impact.packet_network.network.packets.ImpactPacket
import space.impact.packet_network.network.registerPacket

fun registerPackets() {
    registerPacket(prospectorPacketFluid)
    registerPacket(prospectorPacketOre)
    registerPacket(notifyClientSavePacket)
}

fun EntityPlayer.sendPacket(packet: ImpactPacket) {
    sendToPlayer(packet)
}
