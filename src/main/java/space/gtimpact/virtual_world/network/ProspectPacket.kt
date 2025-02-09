package space.gtimpact.virtual_world.network

import net.minecraft.client.Minecraft
import net.minecraft.server.MinecraftServer
import net.minecraft.util.ChatComponentTranslation
import net.minecraft.util.EnumChatFormatting
import space.gtimpact.virtual_world.addon.visual_prospecting.cache.CacheObjectPoint
import space.gtimpact.virtual_world.addon.visual_prospecting.cache.ClientVirtualWorldCache
import space.gtimpact.virtual_world.addon.visual_prospecting.cache.PutDataStatus
import space.gtimpact.virtual_world.addon.visual_prospecting.readPacketDataFluidVein
import space.gtimpact.virtual_world.addon.visual_prospecting.readPacketDataOreVein
import space.gtimpact.virtual_world.api.VirtualAPI
import space.gtimpact.virtual_world.common.items.ScannerTool
import space.gtimpact.virtual_world.util.ItemStackByteUtil
import space.impact.packet_network.network.packets.createPacketStream

val prospectorPacketFluid = createPacketStream(2000) { isServer, read ->
    if (!isServer) {
        val veins = read.readPacketDataFluidVein()

        var status = arrayListOf<PutDataStatus>()

        veins.forEach { data ->

            VirtualAPI.getRegisterFluids().find { it.id == data.veinId }?.also { veinData ->

                status += ClientVirtualWorldCache.putFluid(data.apply {
                    dimension = dimId
                    vein = veinData
                })
            }
        }


        if (status.isNotEmpty()) {
            ChatComponentTranslation("virtual_world.prospected.fluids", status.count { it == PutDataStatus.NEW }, status.count { it == PutDataStatus.UPDATE })
                .apply {
                    chatStyle.setItalic(true)
                    chatStyle.setColor(EnumChatFormatting.GRAY)
                }.also { veinNotification ->
                    Minecraft.getMinecraft().thePlayer.addChatMessage(veinNotification)
                }
        }
    }
}

val prospectorPacketOre = createPacketStream(2001) { isServer, read ->
    if (!isServer) {
        val scan = read.readPacketDataOreVein()

        var status = arrayListOf<PutDataStatus>()

        scan.veins.forEach { data ->

            VirtualAPI.getRegisterOres().find { it.id == data.veinId }?.also { veinData ->

                status += ClientVirtualWorldCache.putOre(scan.layer, data.apply {
                    dimension = dimId
                    vein = veinData
                })
            }
        }

        if (status.isNotEmpty()) {
            ChatComponentTranslation("virtual_world.prospected.ores", status.count { it == PutDataStatus.NEW }, status.count { it == PutDataStatus.UPDATE })
                .apply {
                    chatStyle.setItalic(true)
                    chatStyle.setColor(EnumChatFormatting.GRAY)
                }.also { veinNotification ->
                    Minecraft.getMinecraft().thePlayer.addChatMessage(veinNotification)
                }
        }
    }
}

val notifyClientSavePacket = createPacketStream(2002) { isServer, data ->
    if (!isServer) {
        val isSave = data.readBoolean()

        val serverName = runCatching {
            val serverData = Minecraft.getMinecraft().func_147104_D()
            serverData?.serverName ?: MinecraftServer.getServer()?.worldName
        }.getOrNull() ?: return@createPacketStream

        if (isSave)
            ClientVirtualWorldCache.saveCache(serverName)
        else
            ClientVirtualWorldCache.loadVeinCache(serverName)
    }
}

val MetaBlockGlassPacket = createPacketStream(2003) { isServer, data ->
    if (isServer) {
        serverPlayer?.heldItem?.also { stack ->
            (stack.item as ScannerTool).changeLayer(serverPlayer!!, stack)
        }
    }
}

val SetObjectToChunkPacket = createPacketStream(2004) { isServer, data ->
    if (!isServer) {
        val isRemove = data.readBoolean()
        val stack = ItemStackByteUtil.readItemStackFromDataInput(data) ?: return@createPacketStream
        val element = CacheObjectPoint.ObjectElement(name = data.readUTF(), stack = stack)
        val dim = data.readInt()
        val x = data.readInt()
        val z = data.readInt()

        if (isRemove) {
            ClientVirtualWorldCache.removeObjectChunk(dimId = dim, blockX = x, blockZ = z, element = element)
        } else {
            ClientVirtualWorldCache.putObjectElement(dimId = dim, blockX = x, blockZ = z, element = element)
        }
    }
}
