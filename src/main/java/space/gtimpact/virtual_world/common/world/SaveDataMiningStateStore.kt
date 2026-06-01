package space.gtimpact.virtual_world.common.world

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.world.World
import space.gtimpact.virtual_world.api.services.mining.MiningStateStore
import space.gtimpact.virtual_world.api.services.mining.fluids.FluidVeinKey
import space.gtimpact.virtual_world.api.services.mining.ores.OreChunkKey
import java.util.concurrent.ConcurrentHashMap

object SaveDataMiningStateStore : MiningStateStore {

    private val oreByChunk = ConcurrentHashMap<OreChunkKey, Int>()
    private val fluidByVein = ConcurrentHashMap<FluidVeinKey, Int>()

    fun writeToNBT(world: World, nbt: NBTTagCompound) {
        if (world.provider.dimensionId == 0) {
            val root = NBTTagCompound()

            val oreByChunkNBT = NBTTagList()
            oreByChunk.forEach { (key, value) ->
                val tag = NBTTagCompound()
                tag.setInteger("amount", value)
                tag.setTag("key", key.toNBT())
                oreByChunkNBT.appendTag(tag)
            }
            root.setInteger("oreByChunkSize", oreByChunk.size)
            root.setTag("oreByChunk", oreByChunkNBT)

            val fluidByVeinNBT = NBTTagList()
            fluidByVein.forEach { (key, value) ->
                val tag = NBTTagCompound()
                tag.setInteger("amount", value)
                tag.setTag("key", key.toNBT())
                fluidByVeinNBT.appendTag(tag)
            }
            root.setInteger("fluidByVeinSize", fluidByVein.size)
            root.setTag("fluidByVein", fluidByVeinNBT)

            nbt.setTag("MiningStateStore", root)
        }
    }

    fun readFromNBT( world: World, nbt: NBTTagCompound) {
        if (world.provider.dimensionId == 0) {
            flush()

            val root = nbt.getCompoundTag("MiningStateStore")

            val oreByChunkSize = root.getInteger("oreByChunkSize")
            val oreByChunkNBT = root.getTag("oreByChunk") as NBTTagList
            for (i in 0 until oreByChunkSize) {
                val tag = oreByChunkNBT.getCompoundTagAt(i)
                val amount = tag.getInteger("amount")
                val key = OreChunkKey.fromNBT(tag.getCompoundTag("key"))
                oreByChunk[key] = amount
            }

            val fluidByVeinSize = root.getInteger("fluidByVeinSize")
            val fluidByVeinNBT = root.getTag("fluidByVein") as NBTTagList
            for (i in 0 until fluidByVeinSize) {
                val tag = fluidByVeinNBT.getCompoundTagAt(i)
                val amount = tag.getInteger("amount")
                val key = FluidVeinKey.fromNBT(tag.getCompoundTag("key"))
                fluidByVein[key] = amount
            }
        }
    }

    override fun getMinedOreAmount(
        key: OreChunkKey,
    ): Int {
        return oreByChunk[key] ?: 0
    }

    override fun addMinedOreAmount(
        key: OreChunkKey,
        amount: Int,
    ) {
        if (amount <= 0) return

        val current = oreByChunk[key] ?: 0
        oreByChunk[key] = current + amount
    }

    override fun getExtractedFluidVolume(
        key: FluidVeinKey,
    ): Int {
        return fluidByVein[key] ?: 0
    }

    override fun addExtractedFluidVolume(
        key: FluidVeinKey,
        amount: Int,
    ) {
        if (amount <= 0) return

        val current = fluidByVein[key] ?: 0
        fluidByVein[key] = current + amount
    }

    override fun flush() {
        oreByChunk.clear()
        fluidByVein.clear()
    }
}
