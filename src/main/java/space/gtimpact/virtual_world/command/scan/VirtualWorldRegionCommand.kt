package space.gtimpact.virtual_world.command.scan

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World
import space.gtimpact.virtual_world.VirtualOres
import space.gtimpact.virtual_world.api.VirtualAPI
import space.gtimpact.virtual_world.api.core.WorldPos
import space.gtimpact.virtual_world.api.core.toResourcePos
import space.gtimpact.virtual_world.api.services.scanning.ScanMode
import space.gtimpact.virtual_world.api.services.storage.getFluidVeinAtVein
import space.gtimpact.virtual_world.api.services.storage.getOreVeinAtVein
import space.gtimpact.virtual_world.api.services.storage.getRegionAtBlock
import kotlin.math.roundToInt

fun region(world: World, player: EntityPlayer, args: Array<String>) {

    val command = args.joinToString(" ")

    val pos = WorldPos(
        x = player.posX.roundToInt(),
        z = player.posZ.roundToInt(),
    )

    when (command) {
        "region create" -> {
            val region = VirtualOres.proxy.virtualWorldProvider.instance?.regions?.getRegionAtBlock(
                dimensionId = world.provider.dimensionId,
                blockPos = pos,
            )
            println("====================")
            println("Created region: $region")
            println("====================")
        }

        "region get ore" -> {
            val oreVein = VirtualOres.proxy.virtualWorldProvider.instance?.regions?.getOreVeinAtVein(
                dimensionId = world.provider.dimensionId,
                pos = pos.toResourcePos(),
            )
            println("====================")
            println("Find ore: ${oreVein?.let { it.layers.joinToString(" --- ") { it.ore.name + ", L" + it.ore.layer} } }}")
            println("====================")
        }

        "region get fluid" -> {
            val fluidVein = VirtualOres.proxy.virtualWorldProvider.instance?.regions?.getFluidVeinAtVein(
                dimensionId = world.provider.dimensionId,
                pos = pos.toResourcePos(),
            )
            println("====================")
            println("Find fluid: $fluidVein")
            println("====================")
        }

        "region cache clear" -> {
            VirtualOres.proxy.virtualWorldProvider.instance?.regions?.clearCache()
        }

        "region pos" -> {
            val region = VirtualOres.proxy.virtualWorldProvider.instance?.regions?.getRegionAtBlock(
                dimensionId = world.provider.dimensionId,
                blockPos = pos,
            )

            val oreVein = VirtualOres.proxy.virtualWorldProvider.instance?.regions?.getOreVeinAtVein(
                dimensionId = world.provider.dimensionId,
                pos = pos.toResourcePos(),
            )

            println("====================")
            println("region pos: ${region?.pos}")
            println("oreVein pos: ${oreVein?.pos}")
            println("====================")
        }

        "region scan ore" -> {
            VirtualAPI.scannerManager.scanOres(
                player = player,
                mode = ScanMode.WITH_AMOUNT,
                dimensionId = world.provider.dimensionId,
                layer = 1,
                radiusVeins = 1,
            )
        }

        "region mine ore" -> {
            val mining = VirtualOres.proxy.virtualWorldProvider.instance?.mining ?: return
            mining.mineOreAtBlock(
                dimensionId = world.provider.dimensionId,
                blockPos = pos,
                layerIndex = 1,
                amount = 1,
            )
        }

        "region scan fluid" -> {
            VirtualAPI.scannerManager.scanFluids(
                player = player,
                mode = ScanMode.WITH_AMOUNT,
                dimensionId = world.provider.dimensionId,
                radiusVeins = 8,
            )
        }
    }
}
