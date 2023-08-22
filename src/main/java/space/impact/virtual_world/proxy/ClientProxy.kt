package space.impact.virtual_world.proxy

import net.minecraft.client.Minecraft
import space.impact.virtual_world.VirtualOres
import space.impact.virtual_world.client.gui.ScannerGui


class ClientProxy : CommonProxy() {

    override fun openGui() {
        super.openGui()
        Minecraft.getMinecraft().thePlayer?.let { p ->
            p.worldObj?.let { world ->
                p.openGui(
                    VirtualOres.instance(),
                    ScannerGui.GUI_ID,
                    world,
                    p.posX.toInt(),
                    p.posY.toInt(),
                    p.posZ.toInt(),
                )
            }
        }
    }
}