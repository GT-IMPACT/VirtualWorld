package space.gtimpact.virtual_world.addon.visual_prospecting.layer.fluid

import journeymap.client.render.draw.DrawStep
import journeymap.client.render.draw.DrawUtil
import journeymap.client.render.map.GridRenderer
import org.lwjgl.input.Keyboard
import space.gtimpact.virtual_world.addon.visual_prospecting.MapSettings
import kotlin.math.pow

class FluidsDrawStep(
    private val location: FluidsLocation,
) : DrawStep {

    companion object {
        private const val CHUNK_SIZE_BLOCKS = 16.0
        private const val GAP_BLOCKS = 1
    }

    override fun draw(
        draggedPixelX: Double,
        draggedPixelY: Double,
        gridRenderer: GridRenderer,
        drawScale: Float,
        fontScale: Double,
        rotation: Double,
    ) {
        val blockPixelSize = 2.0.pow(gridRenderer.zoom.toDouble())
        val chunkPixelSize = CHUNK_SIZE_BLOCKS * blockPixelSize
        val gap = GAP_BLOCKS * blockPixelSize

        val centerPixel = gridRenderer.getBlockPixelInGrid(
            location.blockX,
            location.blockZ,
        )

        val x = centerPixel.x + draggedPixelX - chunkPixelSize / 2.0 + gap
        val y = centerPixel.y + draggedPixelY - chunkPixelSize / 2.0 + gap
        val size = chunkPixelSize * 4

        val hasFullScreenRender =
            MapSettings.isFullscreenMap and !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) and (gridRenderer.zoom >= 2)
        val hasMinimapRender = !MapSettings.isFullscreenMap and Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)

        if (location.remainingAmount >= 0 && location.generatedAmount >= 0) {

            val percentAmount = 100 * location.remainingAmount / location.maxGeneratedSize
            val alpha = 225 * percentAmount / 100

            DrawUtil.drawRectangle(
                x,
                y,
                size,
                size,
                location.color,
                alpha,
            )

            if ((hasFullScreenRender || hasMinimapRender) && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                DrawUtil.drawLabel(
                    "${percentAmount}%",
                    x + 7.5 * blockPixelSize * 4,
                    y + 7.5 * blockPixelSize * 4,
                    DrawUtil.HAlign.Center,
                    DrawUtil.VAlign.Middle,
                    0x000000,
                    180,
                    0x00FFFFFF,
                    255,
                    fontScale * 1.25.pow(gridRenderer.zoom.toDouble()),
                    false,
                    rotation,
                )
            }
        } else {
            DrawUtil.drawRectangle(
                x,
                y,
                size * 4,
                size * 4,
                location.color,
                50,
            )
        }


        val veinPixelSize = 4.0 * chunkPixelSize
        val border = 0.5 * blockPixelSize

        DrawUtil.drawRectangle(
            x - gap / 2,
            y - gap / 2,
            veinPixelSize,
            border,
            location.color,
            250,
        )
        DrawUtil.drawRectangle(
            x - gap / 2,
            y + veinPixelSize - border - gap / 2,
            veinPixelSize,
            border,
            location.color,
            250,
        )
        DrawUtil.drawRectangle(
            x - gap / 2,
            y - gap / 2,
            border,
            veinPixelSize,
            location.color,
            250,
        )
        DrawUtil.drawRectangle(
            x + veinPixelSize - border - gap / 2,
            y - gap / 2,
            border,
            veinPixelSize,
            location.color,
            250,
        )

        if (hasFullScreenRender || hasMinimapRender) {
            DrawUtil.drawLabel(
                location.name,
                x + 2.0 * blockPixelSize,
                y + 2.0 * blockPixelSize,
                DrawUtil.HAlign.Right,
                DrawUtil.VAlign.Below,
                0x000000,
                180,
                0x00FFFFFF,
                255,
                fontScale * 1.25.pow(gridRenderer.zoom.toDouble()),
                false,
                rotation,
            )
        }
    }
}
