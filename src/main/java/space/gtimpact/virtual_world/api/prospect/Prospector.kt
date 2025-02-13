@file:Suppress("UnstableApiUsage")

package space.gtimpact.virtual_world.api.prospect

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import space.gtimpact.virtual_world.addon.visual_prospecting.ProspectorVeinManager
import space.gtimpact.virtual_world.addon.visual_prospecting.VirtualFluidVeinPosition
import space.gtimpact.virtual_world.addon.visual_prospecting.VirtualOreVeinPosition
import space.gtimpact.virtual_world.api.getFluidLayer
import space.gtimpact.virtual_world.api.getOreLayer0
import space.gtimpact.virtual_world.api.getOreLayer1

@JvmOverloads
internal fun tryScanOres(
    w: World, layer: Int,
    player: EntityPlayer,
    radius: Int,
    needScanSize: Boolean = true,
) {

    val chX = player.posX.toInt() shr 4
    val chZ = player.posZ.toInt() shr 4

    val chunksRes = arrayListOf<VirtualOreVeinPosition>()

    for (x in -radius..radius) {
        for (z in -radius..radius) {
            if (x != -radius && x != radius && z != -radius && z != radius) {
                val chunk = w.getChunkFromChunkCoords(chX + x, chZ + z)
                val veinPos = scanOreChunk(chunk, layer)
                if (veinPos != null) {
                    chunksRes += veinPos
                }
            }
        }
    }

    ProspectorVeinManager.createArea(chunksRes, w.getChunkFromChunkCoords(chX, chZ), player, layer, needScanSize)
}

private fun scanOreChunk(chunk: Chunk, layer: Int): VirtualOreVeinPosition? {
    val count = when (layer) {
        0 -> chunk.getOreLayer0()
        1 -> chunk.getOreLayer1()
        else -> null
    }

    return if (count != null) {
        VirtualOreVeinPosition(
            dimId = chunk.worldObj.provider.dimensionId,
            x = chunk.xPosition,
            z = chunk.zPosition,
            vein = count.vein,
            size = count.size * 100 / (count.vein.rangeSize.last * 1000)
        )
    } else null
}

@JvmOverloads
internal fun tryScanFluids(
    w: World,
    player: EntityPlayer,
    radius: Int,
    needScanSize: Boolean = true,
) {

    val chX = player.posX.toInt() shr 4
    val chZ = player.posZ.toInt() shr 4

    val chunks: ArrayList<Chunk> = ArrayList()

    for (x in -radius..radius) {
        for (z in -radius..radius) {
            if (x != -radius && x != radius && z != -radius && z != radius) {
                chunks += w.getChunkFromChunkCoords(chX + x, chZ + z)
            }
        }
    }

    val list = arrayListOf<VirtualFluidVeinPosition>()

    for (chunk in chunks)
        scanFluidChunk(chunk)?.also { list += it }

    ProspectorVeinManager.createArea(list, w.getChunkFromChunkCoords(chX, chZ), player, needScanSize)
}

private fun scanFluidChunk(chunk: Chunk): VirtualFluidVeinPosition? {
    val count = chunk.getFluidLayer()
    return if (count != null && count.vein.rangeSize.last > 0) {
        VirtualFluidVeinPosition(
            dimId = chunk.worldObj.provider.dimensionId,
            x = chunk.xPosition,
            z = chunk.zPosition,
            vein = count.vein,
            size = count.size * 100 / (count.vein.rangeSize.last * 1000)
        )
    } else null
}
