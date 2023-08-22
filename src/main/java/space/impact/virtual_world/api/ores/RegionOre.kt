package space.impact.virtual_world.api.ores

import com.google.gson.annotations.SerializedName
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import java.util.*

/**
 * Region with Virtual Ore
 *
 * @param veins Int = Layer
 */
data class RegionOre @JvmOverloads constructor(
    @SerializedName("x") val xRegion: Int,
    @SerializedName("z") val zRegion: Int,
    @SerializedName("d") val dim: Int,
    @SerializedName("veins") val veins: ArrayList<VeinOre> = ArrayList() //Layer to list veins
)  {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        if (other.hashCode() == hashCode()) return true
        return (other as RegionOre).let {
            xRegion == it.xRegion && zRegion == it.zRegion && dim == it.dim
        }
    }

    override fun hashCode(): Int {
        return Objects.hash(xRegion, zRegion, dim)
    }

    fun writeToNBT(nbt: NBTTagCompound) {
        nbt.setInteger("ORE_xRegion", xRegion)
        nbt.setInteger("ORE_zRegion", zRegion)
        nbt.setInteger("ORE_dim", dim)

        val veins = NBTTagList()

        for (veinsOre in this.veins) {
            val tag = NBTTagCompound()
            veinsOre.writeToNBT(tag)
            veins.appendTag(tag)
        }

        nbt.setTag("ORE_veins", veins)
    }

    companion object {
        fun readFromNBT(nbt: NBTTagCompound): RegionOre {
            val veins = arrayListOf<VeinOre>()
            val veinsTag = nbt.getTag("ORE_veins") as NBTTagList
            for (i in 0 until veinsTag.tagCount()) {
                val tag = veinsTag.getCompoundTagAt(i)
                veins += VeinOre.readFromNBT(tag)
            }

            return RegionOre(
                xRegion = nbt.getInteger("ORE_xRegion"),
                zRegion = nbt.getInteger("ORE_zRegion"),
                dim = nbt.getInteger("ORE_dim"),
                veins = veins,
            )
        }
    }
}