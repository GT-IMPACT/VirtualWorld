package space.impact.virtual_world.api.fluids

import com.google.gson.annotations.SerializedName
import net.minecraft.nbt.NBTTagCompound
import space.impact.virtual_world.api.ores.ChunkOre
import java.util.*

/**
 * Chunk with Virtual Fluid
 */
data class ChunkFluid(
    @SerializedName("x") val x: Int,
    @SerializedName("z") val z: Int,
    @SerializedName("size") var size: Int,
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

    fun hasExtract(amount: Int): Boolean {
        size -= amount
        return size > 0
    }

    fun writeToNBT(nbt: NBTTagCompound) {
        nbt.setInteger("FLUID_xChunk", x)
        nbt.setInteger("FLUID_zChunk", z)
        nbt.setInteger("FLUID_size", size)
    }

    companion object {
        fun readFromNBT(nbt: NBTTagCompound): ChunkFluid {
            return ChunkFluid(
                x = nbt.getInteger("FLUID_xChunk"),
                z = nbt.getInteger("FLUID_zChunk"),
                size = nbt.getInteger("FLUID_size"),
            )
        }
    }
}