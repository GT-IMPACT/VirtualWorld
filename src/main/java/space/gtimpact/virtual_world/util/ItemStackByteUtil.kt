@file:Suppress("UnstableApiUsage")

package space.gtimpact.virtual_world.util

import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteArrayDataOutput
import com.google.common.io.ByteStreams
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.nbt.NBTTagCompound
import java.io.*

object ItemStackByteUtil {

    fun writeItemStackToDataOutput(itemStack: ItemStack): ByteArray {
        val dataOutput: ByteArrayDataOutput = ByteStreams.newDataOutput()
        val byteArray = itemStackToByteArray(itemStack)
        dataOutput.writeInt(byteArray.size)
        dataOutput.write(byteArray)
        return dataOutput.toByteArray()
    }

    fun readItemStackFromDataInput(dataInput: ByteArrayDataInput): ItemStack? {
        val length = dataInput.readInt()
        val byteArray = ByteArray(length)
        dataInput.readFully(byteArray)
        return byteArrayToItemStack(byteArray)
    }

    private fun itemStackToByteArray(itemStack: ItemStack): ByteArray {
        val nbtTagCompound = NBTTagCompound()
        itemStack.writeToNBT(nbtTagCompound)

        val byteArrayOutputStream = ByteArrayOutputStream()
        val dataOutputStream = DataOutputStream(byteArrayOutputStream)
        try {
            CompressedStreamTools.write(nbtTagCompound, dataOutputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            dataOutputStream.close()
        }

        return byteArrayOutputStream.toByteArray()
    }

    private fun byteArrayToItemStack(byteArray: ByteArray): ItemStack? {
        val byteArrayInputStream = ByteArrayInputStream(byteArray)
        val dataInputStream = DataInputStream(byteArrayInputStream)
        val nbtTagCompound: NBTTagCompound
        try {
            nbtTagCompound = CompressedStreamTools.read(dataInputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            dataInputStream.close()
        }

        return ItemStack.loadItemStackFromNBT(nbtTagCompound)
    }
}
