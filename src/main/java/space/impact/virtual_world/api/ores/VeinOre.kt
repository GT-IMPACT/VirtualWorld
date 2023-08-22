package space.impact.virtual_world.api.ores

import com.google.gson.annotations.SerializedName
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import space.impact.virtual_world.api.VirtualAPI
import space.impact.virtual_world.api.VirtualOreVein
import java.util.*

/**
 * Vein with Virtual Ore
 */
data class VeinOre @JvmOverloads constructor(
    @SerializedName("x") val xVein: Int,
    @SerializedName("z") val zVein: Int,
    @SerializedName("ore") val oreVein: VirtualOreVein,
    @SerializedName("ch") val oreChunks: ArrayList<ChunkOre> = ArrayList(),
) {
    override fun hashCode(): Int {
        return Objects.hash(xVein, zVein)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        if (other.hashCode() == hashCode()) return true
        return (other as VeinOre).let { xVein == it.xVein && zVein == it.zVein }
    }

    fun writeToNBT(nbt: NBTTagCompound) {
        nbt.setInteger("ORE_xVein", xVein)
        nbt.setInteger("ORE_zVein", zVein)
        nbt.setInteger("ORE_oreVeinId", oreVein.id)

        val chunks = NBTTagList()
        for (oreChunk in oreChunks) {
            val tag = NBTTagCompound()
            oreChunk.writeToNBT(tag)
            chunks.appendTag(tag)
        }
        nbt.setTag("ORE_chunks", chunks)
    }

    companion object {
        fun readFromNBT(nbt: NBTTagCompound): VeinOre {
            val oreChunks = arrayListOf<ChunkOre>()
            val chunks = nbt.getTag("ORE_chunks") as NBTTagList
            for (i in 0 until chunks.tagCount()) {
                val tag = chunks.getCompoundTagAt(i)
                oreChunks += ChunkOre.readFromNBT(tag)
            }
            return VeinOre(
                xVein = nbt.getInteger("ORE_xVein"),
                zVein = nbt.getInteger("ORE_zVein"),
                oreVein = VirtualAPI.getVirtualOreVeinById(nbt.getInteger("ORE_oreVeinId")),
                oreChunks = oreChunks,
            )
        }
    }
}