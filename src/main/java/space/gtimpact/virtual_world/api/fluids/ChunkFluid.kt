package space.gtimpact.virtual_world.api.fluids

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Chunk with Virtual Fluid
 */
data class ChunkFluid(
    @SerializedName("x") val x: Int,
    @SerializedName("z") val z: Int,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        if (other.hashCode() == hashCode()) return true
        return (other as ChunkFluid).let { it.x == x && it.z == z }
    }

    override fun hashCode(): Int {
        return Objects.hash(x, z)
    }
}