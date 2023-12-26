package space.gtimpact.virtual_world.common.world

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.world.World
import net.minecraft.world.WorldSavedData
import net.minecraftforge.common.DimensionManager
import net.minecraftforge.event.world.WorldEvent

class VirtualWorldSaveData(name: String) : WorldSavedData(name) {

    constructor() : this(DATA_NAME)

    companion object {
        private const val DATA_NAME = "VirtualWorldSaveData"
        private var INSTANCE: VirtualWorldSaveData? = null

        private fun loadWorld(w: World) {
            val storage = w.mapStorage
            val wsd = storage.loadData(VirtualWorldSaveData::class.java, DATA_NAME)
            if (wsd == null) {
                INSTANCE = VirtualWorldSaveData()
                storage.setData(DATA_NAME, INSTANCE)
            } else {
                INSTANCE = wsd as? VirtualWorldSaveData
            }
            INSTANCE?.markDirty()
        }
    }

    @Suppress("unused")
    @SubscribeEvent
    fun onWorldLoad(e: WorldEvent.Load) {
        if (!e.world.isRemote && e.world.provider.dimensionId == 0) loadWorld(e.world)
    }

    override fun readFromNBT(nbt: NBTTagCompound) {
        val resTag = nbt.getCompoundTag("VIRTUAL_WORLD")
        val worldList = resTag.getTag("WORLD_LIST") as NBTTagList


        for (i in 0 until worldList.tagCount()) {
            val worldTag = worldList.getCompoundTagAt(i)
            val dimId = worldTag.getInteger("WORLD_ID")
            val worldServer = DimensionManager.getWorld(dimId)
            if (worldServer is IWorldNbt) {
                worldServer.readFromNBT(worldTag)
            }
        }
    }

    override fun writeToNBT(nbt: NBTTagCompound) {
        val nbtWorlds = NBTTagCompound()
        val listWorlds = NBTTagList()

        for (worldServer in DimensionManager.getWorlds()) {
            if (worldServer is IWorldNbt) {
                val worldTag = NBTTagCompound()
                worldTag.setInteger("WORLD_ID", worldServer.provider.dimensionId)
                worldServer.writeToNBT(worldTag)
                listWorlds.appendTag(worldTag)
            }
        }

        nbtWorlds.setTag("WORLD_LIST", listWorlds)
        nbt.setTag("VIRTUAL_WORLD", nbtWorlds)
    }
}

