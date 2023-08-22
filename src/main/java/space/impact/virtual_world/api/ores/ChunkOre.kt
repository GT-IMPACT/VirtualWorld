package space.impact.virtual_world.api.ores

import com.google.gson.annotations.SerializedName
import net.minecraft.nbt.NBTTagCompound
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

    fun writeToNBT(nbt: NBTTagCompound) {
        nbt.setInteger("ORE_xChunk", x)
        nbt.setInteger("ORE_zChunk", z)
        nbt.setInteger("ORE_sizeChunk", size)
    }

    companion object {
        fun readFromNBT(nbt: NBTTagCompound): ChunkOre {
            return ChunkOre(
                x = nbt.getInteger("ORE_xChunk"),
                z = nbt.getInteger("ORE_zChunk"),
                size = nbt.getInteger("ORE_sizeChunk"),
            )
        }
    }
}
