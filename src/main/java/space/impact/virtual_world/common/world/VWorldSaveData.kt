package space.impact.virtual_world.common.world

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.world.World
import net.minecraft.world.WorldSavedData
import net.minecraftforge.common.DimensionManager
import net.minecraftforge.event.world.WorldEvent
import space.impact.virtual_world.api.IResourceWorld

class VWorldSaveData : WorldSavedData(DATA_NAME) {

    companion object {
        private var INSTANCE: VWorldSaveData? = null
        private const val DATA_NAME = "VirtualWorldSaveData"
        private fun loadWorld(w: World) {
            val storage = w.mapStorage
            INSTANCE = storage.loadData(VWorldSaveData::class.java, DATA_NAME) as? VWorldSaveData
            if (INSTANCE == null) {
                INSTANCE = VWorldSaveData()
                storage.setData(DATA_NAME, INSTANCE)
            }
            INSTANCE?.markDirty()
        }
    }

    @SubscribeEvent
    fun onWorldLoad(e: WorldEvent.Load) {
        if (!e.world.isRemote && e.world.provider.dimensionId == 0) loadWorld(e.world)
    }

    override fun readFromNBT(nbt: NBTTagCompound) {

        val resTag = nbt.getCompoundTag("RESOURCE_TAG")
        val worldList = resTag.getTag("WORLD_LIST") as NBTTagList

        for (i in 0 until worldList.tagCount()) {
            val worldTag = worldList.getCompoundTagAt(i)
            val dimId = worldTag.getInteger("WORLD_ID")
            val worldServer =  DimensionManager.getWorld(dimId)
            if (worldServer is IResourceWorld) {
                worldServer.readFromNBT(worldTag)
            }
        }
    }

    override fun writeToNBT(nbt: NBTTagCompound) {
        val nbtWorlds = NBTTagCompound()
        val listWorlds = NBTTagList()

        for (worldServer in DimensionManager.getWorlds()) {
            if (worldServer is IResourceWorld) {
                val worldTag = NBTTagCompound()
                worldTag.setInteger("WORLD_ID", worldServer.provider.dimensionId)
                worldServer.writeToNBT(worldTag)
                listWorlds.appendTag(worldTag)
            }
        }

        nbtWorlds.setTag("WORLD_LIST", listWorlds)
        nbt.setTag("RESOURCE_TAG", nbtWorlds)
    }
}
