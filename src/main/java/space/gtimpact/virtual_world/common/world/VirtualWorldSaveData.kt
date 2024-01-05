package space.gtimpact.virtual_world.common.world

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.PlayerEvent
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.world.World
import net.minecraft.world.WorldSavedData
import net.minecraftforge.common.DimensionManager
import net.minecraftforge.event.world.WorldEvent
import space.gtimpact.virtual_world.extras.NBT
import space.gtimpact.virtual_world.network.notifyClientSavePacket
import space.gtimpact.virtual_world.network.sendPacket

class VirtualWorldSaveData(name: String) : WorldSavedData(name) {
    constructor() : this(DATA_NAME)

    @Suppress("unused")
    @SubscribeEvent
    fun onPlayerLogged(e: PlayerEvent.PlayerLoggedInEvent) {
        e.player.sendPacket(notifyClientSavePacket.transaction(false))
    }

    @Suppress("unused")
    @SubscribeEvent
    fun onWorldLoad(e: WorldEvent.Load) {
        if (!e.world.isRemote)
            getInstance(e.world).markDirty()
    }

    @Suppress("unused")
    @SubscribeEvent
    fun onWorldLoad(e: WorldEvent.Save) {
        if (!e.world.isRemote) {
            getInstance(e.world).markDirty()
            if (e.world.provider.dimensionId == 0) {
                MinecraftServer.getServer().configurationManager.playerEntityList.forEach {
                    it.sendPacket(notifyClientSavePacket.transaction(true))
                }
            }
        }
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
    private val regionsHashed: HashSet<Int> = hashSetOf()

    fun saveHashRegion(hash: Int) {
        regionsHashed += hash
    }

    fun hasSave(hash: Int): Boolean {
        return !regionsHashed.contains(hash)
    }

    override fun readFromNBT(nbt: NBTTagCompound) {
        val worldTag = nbt.getCompoundTag(NBT.VIRTUAL_WORLD)

        val worldServer = DimensionManager.getWorld(worldTag.getInteger(NBT.WORLD_ID))

        regionsHashed.clear()
        regionsHashed += worldTag.getIntArray(NBT.WORLD_REGION_HASH).toSet()

        if (worldServer is IWorldNbt) {
            worldServer.readFromNBT(worldTag)
        }
    }

    override fun writeToNBT(nbt: NBTTagCompound) {
        val worldTag = NBTTagCompound()

        val worldServer = world

        if (worldServer is IWorldNbt) {
            worldServer.writeToNBT(worldTag)
            worldTag.setInteger(NBT.WORLD_ID, worldServer.provider.dimensionId)
        }

        worldTag.setIntArray(NBT.WORLD_REGION_HASH, regionsHashed.toIntArray())
        nbt.setTag(NBT.VIRTUAL_WORLD, worldTag)
    }
}
