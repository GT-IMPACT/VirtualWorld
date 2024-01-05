package space.gtimpact.virtual_world.addon.visual_prospecting.cache

import net.minecraft.client.Minecraft
import java.io.File

object ClientVirtualWorldCache : VirtualWorldCache() {
    override fun getStorageDirectory(): File {
        val folderMC = Minecraft.getMinecraft().mcDataDir
        val fileClient = File(folderMC, "journeymap/data/sp/")
        return fileClient
    }
}
