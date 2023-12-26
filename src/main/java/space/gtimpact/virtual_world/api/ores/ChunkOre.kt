package space.gtimpact.virtual_world.api.ores

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Chunk with Virtual Ore
 */
data class ChunkOre @JvmOverloads constructor(
    @SerializedName("x") val x: Int,
    @SerializedName("z") val z: Int,
    @SerializedName("size") var size: Int = 0
) {

    fun hasExtract(amount: Int): Boolean {
        size -= amount
        return size > 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        if (other.hashCode() == hashCode()) return true
        return (other as ChunkOre).let { it.x == x && it.z == z }
    }

    override fun hashCode(): Int {
        return Objects.hash(x, z)
    }
}