package space.gtimpact.virtual_world.api.fluids

import com.google.gson.annotations.SerializedName
import space.gtimpact.virtual_world.api.TypeFluidVein
import space.gtimpact.virtual_world.api.TypeFluidVein.LP
import java.util.*

/**
 * Vein with Virtual Fluid
 */
data class VeinFluid @JvmOverloads constructor(
    @SerializedName("x") val xVein: Int,
    @SerializedName("z") val zVein: Int,
    @SerializedName("f") val fluidId: Int,
    @SerializedName("size") var size: Int = 0,
    @SerializedName("type") var type: TypeFluidVein = TypeFluidVein.values().random(),
    @SerializedName("ch") val oreChunks: ArrayList<ChunkFluid> = arrayListOf(),
) {

    fun hasExtract(amount: Int): Boolean {
        size -= amount
        return size > 0
    }

    override fun hashCode(): Int {
        return Objects.hash(xVein, zVein)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        if (other.hashCode() == hashCode()) return true
        return (other as VeinFluid).let { xVein == it.xVein && zVein == it.zVein }
    }
}