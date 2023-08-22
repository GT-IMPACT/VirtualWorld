package space.impact.virtual_world.api.fluids

import com.google.gson.annotations.SerializedName
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import space.impact.virtual_world.api.ores.RegionOre
import space.impact.virtual_world.api.ores.VeinOre
import java.util.*

/**
 * Region with Virtual Fluid
 */
data class RegionFluid @JvmOverloads constructor(
    @SerializedName("x") val xRegion: Int,
    @SerializedName("z") val zRegion: Int,
    @SerializedName("d") val dim: Int,
    @SerializedName("veins") val veins: ArrayList<VeinFluid> = arrayListOf()
) {
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
        nbt.setInteger("FLUID_xRegion", xRegion)
        nbt.setInteger("FLUID_zRegion", zRegion)
        nbt.setInteger("FLUID_dim", dim)

        val veins = NBTTagList()

        for (veinsOre in this.veins) {
            val tag = NBTTagCompound()
            veinsOre.writeToNBT(tag)
            veins.appendTag(tag)
        }

        nbt.setTag("FLUID_veins", veins)
    }

    companion object {
        fun readFromNBT(nbt: NBTTagCompound): RegionFluid {
            val veins = arrayListOf<VeinFluid>()
            val veinsTag = nbt.getTag("FLUID_veins") as NBTTagList
            for (i in 0 until veinsTag.tagCount()) {
                val tag = veinsTag.getCompoundTagAt(i)
                veins += VeinFluid.readFromNBT(tag)
            }

            return RegionFluid(
                xRegion = nbt.getInteger("FLUID_xRegion"),
                zRegion = nbt.getInteger("FLUID_zRegion"),
                dim = nbt.getInteger("FLUID_dim"),
                veins = veins,
            )
        }
    }
}