package space.gtimpact.virtual_world.network

import net.minecraft.client.Minecraft
import net.minecraft.server.MinecraftServer
import space.gtimpact.virtual_world.VirtualOres
import space.gtimpact.virtual_world.api.VirtualAPI
import space.gtimpact.virtual_world.api.core.ResourcePos
import space.gtimpact.virtual_world.api.game.scanner.ScannerClientStateManager
import space.gtimpact.virtual_world.api.game.scanner.fluids.toData
import space.gtimpact.virtual_world.api.game.scanner.fluids.toPacketDataFluidVein
import space.gtimpact.virtual_world.api.game.scanner.ores.toData
import space.gtimpact.virtual_world.api.game.scanner.ores.toPacketDataOreVein
import space.gtimpact.virtual_world.api.saving.ClientDataSaver
import space.gtimpact.virtual_world.api.services.scanning.ScanMode
import space.gtimpact.virtual_world.api.services.storage.getOreVeinAtVein
import space.gtimpact.virtual_world.config.Config
import space.impact.packet_network.network.packets.createPacketStream

val prospectorPacketFluid = createPacketStream(2000) { isServer, read ->
    if (!isServer) {
        val scanState = read
            .toPacketDataFluidVein()
            .toData()

        ScannerClientStateManager.updateFluids(scanState)
    }
}

val prospectorPacketOre = createPacketStream(2001) { isServer, read ->
    if (!isServer) {

        val scanState = read
            .toPacketDataOreVein()
            .toData()

        ScannerClientStateManager.updateOres(scanState)
    }
}

val notifyClientSavePacket = createPacketStream(2002) { isServer, data ->
    if (!isServer) {
        val isSave = data.readBoolean()

        val serverName = runCatching {
            val serverData = Minecraft.getMinecraft().func_147104_D()
            serverData?.serverName ?: MinecraftServer.getServer()?.worldName
        }.getOrNull() ?: return@createPacketStream

        if (isSave) {
            ClientDataSaver.save(serverName)
        } else {
            ClientDataSaver.load(serverName)
        }
    }
}

val MineFromClientOrePacket = createPacketStream(2005) { isServer, data ->
    if (isServer && Config.enableDebug) {

        val layerIndex = data.readInt()
        val x = data.readInt()
        val z = data.readInt()

        val instance = VirtualOres.proxy.virtualWorldProvider.instance ?: return@createPacketStream
        val mining = instance.mining

        val world = serverWorld ?: return@createPacketStream
        val player = serverPlayer ?: return@createPacketStream

        val pos = ResourcePos(
            x = x,
            z = z,
        )

        val vein = instance.regions.getOreVeinAtVein(
            dimensionId = world.provider.dimensionId,
            pos = pos,
        ) ?: return@createPacketStream

        val layer = vein.layers.find { it.layerIndex == layerIndex } ?: return@createPacketStream

        if (layerIndex == layer.layerIndex) {
            layer.chunks.forEach {
                mining.mineOreAtChunk(
                    dimensionId = world.provider.dimensionId,
                    chunkPos = it.chunkPos,
                    layerIndex = layerIndex,
                    amount = 1,
                )
            }
        }

        VirtualAPI.scannerManager.scanOres(
            player = player,
            mode = ScanMode.WITH_AMOUNT,
            dimensionId = world.provider.dimensionId,
            layer = 1,
            radiusVeins = 1,
        )
    }
}
