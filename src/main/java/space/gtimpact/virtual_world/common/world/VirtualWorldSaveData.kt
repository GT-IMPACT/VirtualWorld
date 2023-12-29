package space.gtimpact.virtual_world.common.world

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.world.World
import net.minecraft.world.WorldSavedData
import net.minecraftforge.common.DimensionManager
import net.minecraftforge.event.world.WorldEvent
import space.gtimpact.virtual_world.extras.NBT
import kotlin.properties.Delegates

class VirtualWorldSaveData(name: String) : WorldSavedData(name) {
    constructor() : this(DATA_NAME)

    @Suppress("unused")
    @SubscribeEvent
    fun onWorldLoad(e: WorldEvent.Load) {
        if (!e.world.isRemote) getInstance(e.world).markDirty()
    }

    @Suppress("unused")
    @SubscribeEvent
    fun onWorldLoad(e: WorldEvent.Save) {
        if (!e.world.isRemote) getInstance(e.world).markDirty()
    }

    companion object {
        internal const val DATA_NAME = "VirtualWorld"

        @JvmStatic
        fun getInstance(world: World): VirtualWorldSaveData {
            val storage = world.perWorldStorage
            var instance = storage.loadData(VirtualWorldSaveData::class.java, DATA_NAME) as? VirtualWorldSaveData
            if (instance == null) {
                instance = VirtualWorldSaveData()
                storage.setData(DATA_NAME, instance)
            }

            if (instance.world == null)
                instance.world = world

            return instance
        }
    }

    var world: World? = null


    override fun readFromNBT(nbt: NBTTagCompound) {
        val worldServer = world

        val resTag = nbt.getCompoundTag(NBT.VIRTUAL_WORLD)

        if (worldServer is IWorldNbt) {
            worldServer.readFromNBT(resTag)
        }
    }

    override fun writeToNBT(nbt: NBTTagCompound) {
        val worldServer = world

        if (worldServer is IWorldNbt) {
            val worldTag = NBTTagCompound()
            worldTag.setInteger(NBT.WORLD_ID, worldServer.provider.dimensionId)
            worldServer.writeToNBT(worldTag)
            nbt.setTag(NBT.VIRTUAL_WORLD, worldTag)
        }
    }
}

