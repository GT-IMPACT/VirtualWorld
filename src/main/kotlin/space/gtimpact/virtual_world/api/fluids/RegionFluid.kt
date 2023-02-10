package space.gtimpact.virtual_world.api.fluids

import com.google.gson.annotations.SerializedName
import space.gtimpact.virtual_world.api.ores.RegionOre
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
}