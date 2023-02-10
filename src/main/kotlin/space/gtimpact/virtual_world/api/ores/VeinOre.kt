package space.gtimpact.virtual_world.api.ores

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Vein with Virtual Ore
 */
data class VeinOre @JvmOverloads constructor(
    @SerializedName("x") val xVein: Int,
    @SerializedName("z") val zVein: Int,
    @SerializedName("ore") val oreId: Int,
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
}