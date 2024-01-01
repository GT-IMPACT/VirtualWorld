package space.gtimpact.virtual_world.proxy

import cpw.mods.fml.common.network.IGuiHandler
import cpw.mods.fml.common.network.NetworkRegistry
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World
import space.gtimpact.virtual_world.VirtualOres
import space.gtimpact.virtual_world.client.gui.ScannerGui

class GuiHandler : IGuiHandler {

    init {
        NetworkRegistry.INSTANCE.registerGuiHandler(VirtualOres.instance(), this)
    }

    override fun getServerGuiElement(ID: Int, player: EntityPlayer?, world: World?, x: Int, y: Int, z: Int): Any? {
        return null
    }

    override fun getClientGuiElement(ID: Int, player: EntityPlayer?, world: World?, x: Int, y: Int, z: Int): Any? {
        if (ID == ScannerGui.GUI_ID) {
            return ScannerGui()
        }
        return null
    }
}