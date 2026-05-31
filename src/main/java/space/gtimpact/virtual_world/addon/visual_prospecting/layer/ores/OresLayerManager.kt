package space.gtimpact.virtual_world.addon.visual_prospecting.layer.ores

import com.sinthoras.visualprospecting.Utils
import com.sinthoras.visualprospecting.integration.model.buttons.ButtonManager
import com.sinthoras.visualprospecting.integration.model.layers.LayerManager
import com.sinthoras.visualprospecting.integration.model.locations.ILocationProvider
import net.minecraft.client.Minecraft
import org.lwjgl.input.Keyboard.KEY_LSHIFT
import org.lwjgl.input.Keyboard.isKeyDown
import space.gtimpact.virtual_world.addon.visual_prospecting.MapSettings
import space.gtimpact.virtual_world.api.core.toChunkPos
import space.gtimpact.virtual_world.api.core.toWorldOrigin
import space.gtimpact.virtual_world.api.game.scanner.ScannerClientStateManager

class OresLayerManager(
    private val layer: Int,
    buttonManager: ButtonManager,
) : LayerManager(buttonManager) {

    override fun generateVisibleElements(
        minBlockX: Int,
        minBlockZ: Int,
        maxBlockX: Int,
        maxBlockZ: Int,
    ): List<ILocationProvider> {

        val state = ScannerClientStateManager.getOreState(
            dimensionId = Minecraft.getMinecraft().thePlayer.dimension,
            layer = layer,
        )
        if (state.layer != layer) return emptyList()

        val minX = mapToCorner(Utils.coordBlockToChunk(minBlockX))
        val minZ = mapToCorner(Utils.coordBlockToChunk(minBlockZ))
        val maxX = mapToCorner(Utils.coordBlockToChunk(maxBlockX))
        val maxZ = mapToCorner(Utils.coordBlockToChunk(maxBlockZ))

        val result = ArrayList<ILocationProvider>()

        for (vein in state.veins) {

            if (vein.chunks.isEmpty()) {

                val firstChunkPos = vein.position
                    .toWorldOrigin()
                    .toChunkPos()

                result += OresLocation(
                    dimension = state.dimensionId,
                    chunkX = firstChunkPos.x,
                    chunkZ = firstChunkPos.z,
                    id = vein.vein.id,
                    name = vein.vein.name,
                    layer = state.layer,
                    color = vein.vein.color,
                    remainingAmount = -1,
                    generatedAmount = -1,
                    maxGeneratedSize = vein.vein.rangeSize.last,
                    isVeinOrigin = true,
                )

            } else {
                val originChunkX = vein.chunks.minOf { it.chunkX }
                val originChunkZ = vein.chunks.minOf { it.chunkZ }

                for (chunk in vein.chunks) {

                    if (chunk.chunkX !in minX..maxX) continue
                    if (chunk.chunkZ !in minZ..maxZ) continue

                    result += OresLocation(
                        dimension = state.dimensionId,
                        chunkX = chunk.chunkX,
                        chunkZ = chunk.chunkZ,
                        id = vein.vein.id,
                        name = vein.vein.name,
                        layer = state.layer,
                        color = vein.vein.color,
                        remainingAmount = chunk.remainingAmount,
                        generatedAmount = chunk.generatedAmount,
                        maxGeneratedSize = vein.vein.rangeSize.last,
                        isVeinOrigin = chunk.chunkX == originChunkX && chunk.chunkZ == originChunkZ,
                    )
                }
            }
        }

        return result
    }

    override fun needsRegenerateVisibleElements(
        minBlockX: Int,
        minBlockZ: Int,
        maxBlockX: Int,
        maxBlockZ: Int,
    ): Boolean {

        val minX = mapToCorner(Utils.coordBlockToChunk(minBlockX))
        val minZ = mapToCorner(Utils.coordBlockToChunk(minBlockZ))
        val maxX = mapToCorner(Utils.coordBlockToChunk(maxBlockX))
        val maxZ = mapToCorner(Utils.coordBlockToChunk(maxBlockZ))

        val isShiftKey = isKeyDown(KEY_LSHIFT)

        if (minX != oldMinX || maxX != oldMaxX || minZ != oldMinZ || maxZ != oldMaxZ || isShiftKey != oldShiftKey) {
            oldMinX = minX
            oldMaxX = maxX
            oldMinZ = minZ
            oldMaxZ = maxZ
            oldShiftKey = isShiftKey
            return true
        }
        return false
    }

    override fun onOpenMap() {
        MapSettings.isFullscreenMap = true
    }

    private fun mapToCorner(chunkCoord: Int): Int {
        return chunkCoord and -0x4
    }

    private var oldMinX = 0
    private var oldMaxX = 0
    private var oldMinZ = 0
    private var oldMaxZ = 0
    private var oldShiftKey = false
}
