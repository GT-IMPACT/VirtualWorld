package space.impact.virtual_world.api.fluids

import com.google.gson.annotations.SerializedName
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import space.impact.virtual_world.api.TypeFluidVein
import java.util.*

/**
 * Vein with Virtual Fluid
 */
data class VeinFluid @JvmOverloads constructor(
    @SerializedName("x") val xVein: Int,
    @SerializedName("z") val zVein: Int,
    @SerializedName("f") val fluidId: Int,
    @SerializedName("type") var type: TypeFluidVein = TypeFluidVein.values().random(),
    @SerializedName("ch") val oreChunks: ArrayList<ChunkFluid> = arrayListOf(),
) {

    override fun hashCode(): Int {
        return Objects.hash(xVein, zVein)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        if (other.hashCode() == hashCode()) return true
        return (other as VeinFluid).let { xVein == it.xVein && zVein == it.zVein }
    }

    fun writeToNBT(nbt: NBTTagCompound) {
        nbt.setInteger("FLUID_xVein", xVein)
        nbt.setInteger("FLUID_zVein", zVein)
        nbt.setInteger("FLUID_fluidId", fluidId)
        nbt.setInteger("FLUID_type", type.ordinal)

        val chunks = NBTTagList()
        for (oreChunk in oreChunks) {
            val tag = NBTTagCompound()
            oreChunk.writeToNBT(tag)
            chunks.appendTag(tag)
        }
        nbt.setTag("FLUID_chunks", chunks)
    }

    companion object {
        fun readFromNBT(nbt: NBTTagCompound): VeinFluid {
            val chunks = arrayListOf<ChunkFluid>()
            val chunksTag = nbt.getTag("FLUID_chunks") as NBTTagList
            for (i in 0 until chunksTag.tagCount()) {
                val tag = chunksTag.getCompoundTagAt(i)
                chunks += ChunkFluid.readFromNBT(tag)
            }
            val typeOrdinal = nbt.getInteger("FLUID_type")
            return VeinFluid(
                xVein = nbt.getInteger("FLUID_xVein"),
                zVein = nbt.getInteger("FLUID_zVein"),
                fluidId = nbt.getInteger("FLUID_fluidId"),
                type = TypeFluidVein.values().find { it.ordinal == typeOrdinal } ?: TypeFluidVein.LP,
                oreChunks = chunks,
            )
        }
    }
}