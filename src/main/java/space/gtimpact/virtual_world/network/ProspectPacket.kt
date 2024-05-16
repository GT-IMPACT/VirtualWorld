package space.gtimpact.virtual_world.network

import net.minecraft.server.MinecraftServer
import space.gtimpact.virtual_world.addon.visual_prospecting.VirtualFluidVeinPosition
import space.gtimpact.virtual_world.addon.visual_prospecting.cache.ClientVirtualWorldCache
import space.gtimpact.virtual_world.addon.visual_prospecting.readPacketDataOreVein
import space.gtimpact.virtual_world.api.VirtualAPI
import space.impact.packet_network.network.packets.createPacketStream

val prospectorPacketFluid = createPacketStream(2000) { isServer, read ->
    if (!isServer) {
        read.readInt()
        val count = read.readInt()

        repeat(count) {

            val veinId = read.readInt()
            val x = read.readInt()
            val z = read.readInt()
            val size = read.readInt()

            VirtualAPI.getRegisterFluids().find { it.id == veinId }?.also {
                val veinPos = VirtualFluidVeinPosition(
                    dimId = dimId,
                    vein = it,
                    x = x,
                    z = z,
                    size = size,
                )
                ClientVirtualWorldCache.putFluid(veinPos)
            }
        }
    }
}

val prospectorPacketOre = createPacketStream(2001) { isServer, read ->
    if (!isServer) {
        val scan = read.readPacketDataOreVein()
        scan.veins.forEach { data ->
            VirtualAPI.getRegisterOres().find { it.id == data.veinId }?.also {
                ClientVirtualWorldCache.putOre(scan.layer, data.apply {
                    dimension = dimId
                    vein = it
                })
            }
        }
    }
}

val notifyClientSavePacket = createPacketStream(2002) { isServer, data ->
    if (!isServer) {
        val isSave = data.readBoolean()
        val worldName = MinecraftServer.getServer()?.worldName ?: return@createPacketStream
        if (isSave)
            ClientVirtualWorldCache.saveCache(worldName)
        else
            ClientVirtualWorldCache.loadVeinCache(worldName)
    }
}
