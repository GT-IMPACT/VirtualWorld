package space.gtimpact.virtual_world.proxy.debug

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import space.gtimpact.virtual_world.api.game.scanner.ScannerClientStateManager
import space.gtimpact.virtual_world.api.game.scanner.ores.ClientOreScanState
import java.text.DecimalFormat
import kotlin.math.floor
import kotlin.math.max

class GuiOreScanState : GuiScreen() {

    private var zoom = 1.0f
    private var offsetX = 0f
    private var offsetZ = 0f

    private var dragging = false
    private var lastMouseX = 0
    private var lastMouseY = 0

    data class VeinKey(
        val dimensionId: Int,
        val layer: Int,
        val veinX: Int,
        val veinZ: Int,
        val veinId: Int
    )

    private var selected: ClientOreScanState.ClientOreVeinState? = null
    private var selectedKey: VeinKey? = null

    private var lastState: ClientOreScanState? = null
    private var currentState: ClientOreScanState? = null

    private val state: ClientOreScanState
        get() = currentState ?: ClientOreScanState.EMPTY

    private var veinIndex: Map<VeinKey, ClientOreScanState.ClientOreVeinState> = emptyMap()

    private var hovered: ClientOreScanState.ClientOreVeinState? = null

    private val fmt = DecimalFormat("#0.0")

    companion object {
        private const val VEIN_CHUNKS = 4
        private const val CELL_BASE = 16f

        private const val PANEL_W = 170
        private const val TOP_H = 26
        private const val PAD = 8

        private const val BTN_DIG = 1
    }

    override fun doesGuiPauseGame(): Boolean = false

    private fun ClientOreScanState.ClientOreVeinState.toKey(
        state: ClientOreScanState
    ): VeinKey {
        return VeinKey(
            dimensionId = state.dimensionId,
            layer = state.layer,
            veinX = veinX(),
            veinZ = veinZ(),
            veinId = vein.id
        )
    }

    private fun updateSelectedFromClientState() {
        val newState = ScannerClientStateManager.getOreState(
            dimensionId = Minecraft.getMinecraft().thePlayer.dimension,
            layer = 1,
        )

        if (newState === lastState) return

        lastState = newState
        currentState = newState

        veinIndex = newState.veins.associateBy { it.toKey(newState) }

        selected = selectedKey?.let { veinIndex[it] }
    }

    override fun initGui() {
        super.initGui()

        buttonList.clear()

        val panelX = width - PANEL_W - PAD
        val panelY = TOP_H + PAD

        buttonList.add(
            GuiButton(
                BTN_DIG,
                panelX + 10,
                panelY + 150,
                PANEL_W - 20,
                20,
                "Dig"
            )
        )
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            BTN_DIG -> {
                val vein = selected ?: return
                digSelectedVein(vein)
            }
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        updateSelectedFromClientState()
        drawDefaultBackground()

        val mapX = PAD
        val mapY = TOP_H + PAD
        val mapW = width - PANEL_W - PAD * 3
        val mapH = height - TOP_H - PAD * 2

        val panelX = mapX + mapW + PAD
        val panelY = mapY

        hovered = null

        drawHeader()
        drawMapBackground(mapX, mapY, mapW, mapH)

        enableScissor(mapX, mapY, mapW, mapH)
        drawVeins(mapX, mapY, mapW, mapH, mouseX, mouseY)
        drawPlayerMarker(mapX, mapY, mapW, mapH)
        disableScissor()

        drawPanel(panelX, panelY, PANEL_W, mapH)

        handleWheel(mouseX, mouseY, mapX, mapY, mapW, mapH)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun drawHeader() {
        drawRect(0, 0, width, TOP_H, argb(220, 12, 14, 18))

        fontRendererObj.drawStringWithShadow(
            "Ore Scan | Dim ${state.dimensionId} | Layer ${state.layer}",
            8,
            9,
            0xE6E6E6
        )

        fontRendererObj.drawStringWithShadow(
            "veins: ${state.veins.size}   zoom: ${fmt.format(zoom)}x",
            width - 145,
            9,
            0xAAAAAA
        )
    }

    private fun drawMapBackground(x: Int, y: Int, w: Int, h: Int) {
        drawRect(x, y, x + w, y + h, argb(190, 8, 10, 14))
//        drawRect(x - 1, y - 1, x + w + 1, y, argb(255, 70, 75, 85))
//        drawRect(x - 1, y + h, x + w + 1, y + h + 1, argb(255, 30, 32, 38))
//        drawRect(x - 1, y - 1, x, y + h + 1, argb(255, 70, 75, 85))
//        drawRect(x + w, y - 1, x + w + 1, y + h + 1, argb(255, 30, 32, 38))
        border(x, y, x + w, y + h, argb(255, 55, 60, 70))
    }

    private fun drawVeins(
        mapX: Int,
        mapY: Int,
        mapW: Int,
        mapH: Int,
        mouseX: Int,
        mouseY: Int
    ) {
        hovered = null
        if (state.veins.isEmpty()) return

        val mouseInsideMap = mouseX >= mapX && mouseY >= mapY &&
                mouseX < mapX + mapW && mouseY < mapY + mapH

        val centerVeinX = state.veins.map { it.veinX() }.average().toFloat()
        val centerVeinZ = state.veins.map { it.veinZ() }.average().toFloat()

        val cell = CELL_BASE * zoom
        val veinSize = cell * VEIN_CHUNKS

        for (veinState in state.veins) {
            val vx = veinState.veinX()
            val vz = veinState.veinZ()

            val sx = (mapX + mapW / 2f + (vx - centerVeinX) * veinSize + offsetX).toInt()
            val sy = (mapY + mapH / 2f + (vz - centerVeinZ) * veinSize + offsetZ).toInt()
            val ex = (sx + veinSize).toInt()
            val ey = (sy + veinSize).toInt()

            if (ex < mapX || ey < mapY || sx > mapX + mapW || sy > mapY + mapH) continue

            val inside =
                mouseInsideMap &&
                        mouseX >= sx &&
                        mouseY >= sy &&
                        mouseX < ex &&
                        mouseY < ey

            if (inside) hovered = veinState

            val rgb = veinState.vein.color and 0xFFFFFF

            drawVeinBox(veinState, sx, sy, ex, ey, inside, selected === veinState)

            if (zoom >= 0.85f && veinState.chunks.isNotEmpty()) {
                drawChunks(veinState, sx, sy, cell)
            }

            val ratio = if (veinState.generatedAmount <= 0) 0f
            else veinState.remainingAmount.toFloat() / veinState.generatedAmount.toFloat()

            val border = when {
                selected === veinState -> 0xFFFFFFFF.toInt()
                inside -> 0xFFEEDC82.toInt()
                else -> alpha(190, rgb)
            }

            border(sx, sy, ex, ey, border)

            val barW = max(1, ((ex - sx - 4) * ratio).toInt())
            drawRect(sx + 2, ey - 5, sx + 2 + barW, ey - 2, 0xFF55DD77.toInt())
            drawRect(sx + 2 + barW, ey - 5, ex - 2, ey - 2, 0xFF442222.toInt())

//            if (zoom >= 0.75f) {
//                fontRendererObj.drawStringWithShadow(
//                    veinState.vein.name,
//                    sx + 4,
//                    sy + 4,
//                    0xFFFFFF
//                )
//            }

            if (zoom > 0.5) {
                val textScale = .5f * zoom.coerceIn(0.5f, 3.5f)
                drawScaledString(
                    veinState.vein.name,
                    sx + (4 * textScale).toInt(),
                    sy + (4 * textScale).toInt(),
                    0xFFFFFF,
                    textScale,
                )
            }
        }

//        hovered?.let {
//            drawTooltip(
//                listOf(
//                    it.vein.name,
//                    "Generated: ${it.generatedAmount}",
//                    "Mined: ${it.minedAmount}",
//                    "Remaining: ${it.remainingAmount}",
//                    "Left: ${percent(it.remainingAmount, it.generatedAmount)}%"
//                ),
//                mouseX,
//                mouseY
//            )
//        }
    }

    private fun drawVeinBox(
        v: ClientOreScanState.ClientOreVeinState,
        x1: Int,
        y1: Int,
        x2: Int,
        y2: Int,
        hovered: Boolean,
        selected: Boolean
    ) {
        val rgb = v.vein.color and 0xFFFFFF
        val alpha = when {
            selected -> 185
            hovered -> 155
            else -> 105
        }

        drawRect(x1, y1, x2, y2, alpha(alpha, rgb))
    }

    private fun drawChunks(
        veinState: ClientOreScanState.ClientOreVeinState,
        veinScreenX: Int,
        veinScreenY: Int,
        cell: Float
    ) {
        val minChunkX = veinState.chunks.minOfOrNull { it.chunkX } ?: return
        val minChunkZ = veinState.chunks.minOfOrNull { it.chunkZ } ?: return

        val rgb = veinState.vein.color and 0xFFFFFF

        val gridX = IntArray(5) { i -> Math.round(veinScreenX + i * cell) }
        val gridY = IntArray(5) { i -> Math.round(veinScreenY + i * cell) }
        val maxGenerated = veinState.vein.rangeSize.last

        for (chunk in veinState.chunks) {
            val localX = chunk.chunkX - minChunkX
            val localZ = chunk.chunkZ - minChunkZ

            if (localX !in 0..3 || localZ !in 0..3) continue

            val x1 = gridX[localX]
            val y1 = gridY[localZ]
            val x2 = gridX[localX + 1]
            val y2 = gridY[localZ + 1]

            if (x2 <= x1 || y2 <= y1) continue

            val ratio = if (chunk.generatedAmount <= 0) {
                0f
            } else {
                chunk.remainingAmount.toFloat() / maxGenerated
            }.coerceIn(0f, 1f)

            val alpha = (ratio * 250).toInt()

            drawRect(x1, y1, x2, y2, alpha(alpha, rgb))
        }

        drawChunkGrid(gridX, gridY)
    }

    private fun drawChunkGrid(gridX: IntArray, gridY: IntArray) {
        val color = argb(95, 0, 0, 0)

        val left = gridX[0]
        val right = gridX[4]
        val top = gridY[0]
        val bottom = gridY[4]

        for (i in 1..3) {
            val x = gridX[i]
            Gui.drawRect(x, top, x + 1, bottom, color)
        }

        for (i in 1..3) {
            val y = gridY[i]
            Gui.drawRect(left, y, right, y + 1, color)
        }
    }

    private fun drawPlayerMarker(mapX: Int, mapY: Int, mapW: Int, mapH: Int) {
        val player = mc.thePlayer ?: return

        val centerVeinX = state.veins.map { it.veinX() }.average().toFloat()
        val centerVeinZ = state.veins.map { it.veinZ() }.average().toFloat()

        val cell = CELL_BASE * zoom
        val veinSize = cell * VEIN_CHUNKS

        val playerVeinX = floor(player.posX / 64.0).toFloat()
        val playerVeinZ = floor(player.posZ / 64.0).toFloat()

        val localBlockX = ((player.posX % 64.0 + 64.0) % 64.0).toFloat()
        val localBlockZ = ((player.posZ % 64.0 + 64.0) % 64.0).toFloat()

        val sx = mapX + mapW / 2f +
                (playerVeinX - centerVeinX) * veinSize +
                (localBlockX / 64f) * veinSize +
                offsetX

        val sy = mapY + mapH / 2f +
                (playerVeinZ - centerVeinZ) * veinSize +
                (localBlockZ / 64f) * veinSize +
                offsetZ

        val x = sx.toInt()
        val y = sy.toInt()

        Gui.drawRect(x - 1, y - 6, x + 2, y + 7, 0xFFFFFFFF.toInt())
        Gui.drawRect(x - 6, y - 1, x + 7, y + 2, 0xFFFFFFFF.toInt())

        Gui.drawRect(x - 1, y - 4, x + 2, y + 5, 0xFFFF3333.toInt())
        Gui.drawRect(x - 4, y - 1, x + 5, y + 2, 0xFFFF3333.toInt())
    }

    private fun drawPanel(x: Int, y: Int, w: Int, h: Int) {
        drawRect(x, y, x + w, y + h, argb(210, 13, 15, 20))
        border(x, y, x + w, y + h, argb(255, 55, 60, 70))

        var yy = y + 10

        fontRendererObj.drawStringWithShadow("Scan Details", x + 10, yy, 0xFFFFFF)
        yy += 16

        val v = selected ?: hovered

        if (v == null) {
            drawSmall(x, yy, "Hover or click a vein")
            yy += 14
            drawSmall(x, yy, "LMB: select")
            yy += 11
            drawSmall(x, yy, "Drag: move map")
            yy += 11
            drawSmall(x, yy, "Wheel: zoom")
            return
        }

        drawSmall(x, yy, "Name: ${v.vein.name}")
        yy += 12
        drawSmall(x, yy, "Id: ${v.vein.id}")
        yy += 12
        drawSmall(x, yy, "Layer: ${v.vein.layer}")
        yy += 12
        drawSmall(x, yy, "Vein: ${v.veinX()}, ${v.veinZ()}")
        yy += 18

        drawAmountLine(x, yy, "Generated", v.generatedAmount, 0xCCCCCC)
        yy += 12
        drawAmountLine(x, yy, "Mined", v.minedAmount, 0xDD7777)
        yy += 12
        drawAmountLine(x, yy, "Remaining", v.remainingAmount, 0x77DD88)
        yy += 18

        val p = percent(v.remainingAmount, v.generatedAmount)
        drawSmall(x, yy, "Remaining: $p%")
        yy += 12

        val barX = x + 10
        val barY = yy
        val barW = w - 20
        val ratio = if (v.generatedAmount <= 0) 0f else v.remainingAmount.toFloat() / v.generatedAmount

        drawRect(barX, barY, barX + barW, barY + 7, 0xFF331E1E.toInt())
        drawRect(barX, barY, barX + (barW * ratio).toInt(), barY + 7, 0xFF55CC77.toInt())

        yy += 20
        drawSmall(x, yy, "Chunks: ${v.chunks.size}")
    }

    private fun drawAmountLine(x: Int, y: Int, label: String, value: Int, color: Int) {
        fontRendererObj.drawString(label, x + 10, y, 0xAAAAAA)
        fontRendererObj.drawString(value.toString(), x + 95, y, color)
    }

    private fun drawSmall(
        x: Int,
        y: Int,
        text: String
    ) {
        fontRendererObj.drawString(text, x + 10, y, 0xB8B8B8)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        val mapX = PAD
        val mapY = TOP_H + PAD
        val mapW = width - PANEL_W - PAD * 3
        val mapH = height - TOP_H - PAD * 2

        val insideMap =
            mouseX >= mapX &&
                    mouseY >= mapY &&
                    mouseX < mapX + mapW &&
                    mouseY < mapY + mapH

        if (button == 0 && insideMap) {
            if (hovered != null) {
                selected = hovered
                selectedKey = selected?.toKey(state)
            }
            dragging = true
            lastMouseX = mouseX
            lastMouseY = mouseY
        }

        super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, button: Int, timeSinceLastClick: Long) {
        if (button == 0 && dragging) {
            offsetX += mouseX - lastMouseX
            offsetZ += mouseY - lastMouseY

            lastMouseX = mouseX
            lastMouseY = mouseY
        }

        super.mouseClickMove(mouseX, mouseY, button, timeSinceLastClick)
    }

    override fun mouseMovedOrUp(mouseX: Int, mouseY: Int, state: Int) {
        dragging = false
        super.mouseMovedOrUp(mouseX, mouseY, state)
    }

    private fun handleWheel(mouseX: Int, mouseY: Int, mapX: Int, mapY: Int, mapW: Int, mapH: Int) {
        if (mouseX !in mapX..(mapX + mapW) || mouseY !in mapY..(mapY + mapH)) return

        val wheel = Mouse.getDWheel()
        if (wheel == 0) return

        val oldZoom = zoom
        val newZoom = when {
            wheel > 0 -> (zoom + 0.5f).coerceAtMost(3.5f)
            else -> (zoom - 0.5f).coerceAtLeast(1f)
        }

        if (newZoom == oldZoom) return

        val mapCenterX = mapX + mapW / 2f
        val mapCenterY = mapY + mapH / 2f

        val mouseWorldX = (mouseX - mapCenterX - offsetX) / oldZoom
        val mouseWorldZ = (mouseY - mapCenterY - offsetZ) / oldZoom

        zoom = newZoom

        offsetX = mouseX - mapCenterX - mouseWorldX * newZoom
        offsetZ = mouseY - mapCenterY - mouseWorldZ * newZoom
    }

    private fun border(x1: Int, y1: Int, x2: Int, y2: Int, color: Int) {
        drawRect(x1, y1, x2, y1 + 1, color)
        drawRect(x1, y2 - 1, x2, y2, color)
        drawRect(x1, y1, x1 + 1, y2, color)
        drawRect(x2 - 1, y1, x2, y2, color)
    }

    private fun percent(value: Int, total: Int): String {
        if (total <= 0) return "0.0"
        return fmt.format(value.toDouble() * 100.0 / total.toDouble())
    }

    private fun alpha(alpha: Int, rgb: Int): Int {
        return ((alpha and 255) shl 24) or (rgb and 0xFFFFFF)
    }

    private fun argb(a: Int, r: Int, g: Int, b: Int): Int {
        return ((a and 255) shl 24) or
                ((r and 255) shl 16) or
                ((g and 255) shl 8) or
                (b and 255)
    }

    // ADAPT THIS TO YOUR ResourcePos
    private fun ClientOreScanState.ClientOreVeinState.veinX(): Int {
        return position.x
    }

    private fun ClientOreScanState.ClientOreVeinState.veinZ(): Int {
        return position.z
    }

    private fun drawScaledString(
        text: String,
        x: Int,
        y: Int,
        color: Int,
        scale: Float
    ) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)
        GL11.glPushMatrix()

        GL11.glTranslatef(x.toFloat(), y.toFloat(), 0f)
        GL11.glScalef(scale, scale, 1f)

        fontRendererObj.drawStringWithShadow(
            text,
            0,
            0,
            color,
        )

        GL11.glPopMatrix()
        GL11.glPopAttrib()

        GL11.glColor4f(1f, 1f, 1f, 1f)
    }

    private fun enableScissor(x: Int, y: Int, w: Int, h: Int) {
        val scale = mc.displayWidth.toDouble() / width.toDouble()

        val sx = (x * scale).toInt()
        val sy = (mc.displayHeight - ((y + h) * scale)).toInt()
        val sw = (w * scale).toInt()
        val sh = (h * scale).toInt()

        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        GL11.glScissor(sx, sy, sw, sh)
    }

    private fun disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST)
    }
}