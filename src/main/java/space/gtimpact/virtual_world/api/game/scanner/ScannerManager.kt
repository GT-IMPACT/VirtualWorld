@file:Suppress("UnstableApiUsage")

package space.gtimpact.virtual_world.api.game.scanner

import com.google.common.io.ByteStreams
import net.minecraft.entity.player.EntityPlayer
import space.gtimpact.virtual_world.api.VirtualAPI
import space.gtimpact.virtual_world.api.core.WorldPos
import space.gtimpact.virtual_world.api.game.scanner.fluids.toDO
import space.gtimpact.virtual_world.api.game.scanner.fluids.toPacket
import space.gtimpact.virtual_world.api.game.scanner.ores.toDO
import space.gtimpact.virtual_world.api.game.scanner.ores.toPacket
import space.gtimpact.virtual_world.api.services.scanning.ScanMode
import space.gtimpact.virtual_world.network.prospectorPacketFluid
import space.gtimpact.virtual_world.network.prospectorPacketOre
import space.gtimpact.virtual_world.network.sendPacket
import kotlin.math.roundToInt

class ScannerManager {

    @JvmOverloads
    fun scanOres(
        player: EntityPlayer,
        mode: ScanMode,
        layer: Int,
        dimensionId: Int = player.dimension,
        radiusVeins: Int = 1,
    ) {
        VirtualAPI.scanning.scanOreAroundBlock(
            dimensionId = dimensionId,
            centerBlockPos = WorldPos(
                x = player.posX.roundToInt(),
                z = player.posZ.roundToInt(),
            ),
            mode = mode,
            layerIndex = layer,
            radiusVeins = radiusVeins,
        ).also { report ->
            val packet = report.toPacket()

            val buf = ByteStreams.newDataOutput()
                .apply { packet.toDO(this) }

            player.sendPacket(prospectorPacketOre.transaction(buf))
        }
    }

    @JvmOverloads
    fun scanFluids(
        player: EntityPlayer,
        mode: ScanMode,
        dimensionId: Int = player.dimension,
        radiusVeins: Int = 1,
    ) {
        VirtualAPI.scanning.scanFluidsAroundBlock(
            dimensionId = dimensionId,
            centerBlockPos = WorldPos(
                x = player.posX.roundToInt(),
                z = player.posZ.roundToInt(),
            ),
            mode = mode,
            radiusVeins = radiusVeins,
        ).also { report ->
            val packet = report.toPacket()

            val buf = ByteStreams.newDataOutput()
                .apply { packet.toDO(this) }

            player.sendPacket(prospectorPacketFluid.transaction(buf))
        }
    }
}
