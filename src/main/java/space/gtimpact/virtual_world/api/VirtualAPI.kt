package space.gtimpact.virtual_world.api

import com.google.common.io.ByteStreams
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.world.ChunkCoordIntPair
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.common.DimensionManager
import space.gtimpact.virtual_world.api.ResourceGenerator.getVeinChunks
import space.gtimpact.virtual_world.config.Config
import space.gtimpact.virtual_world.config.Config.IS_DISABLED_VIRTUAL_FLUIDS
import space.gtimpact.virtual_world.config.Config.IS_DISABLED_VIRTUAL_ORES
import space.gtimpact.virtual_world.network.SetObjectToChunk
import space.gtimpact.virtual_world.network.sendPacket
import space.gtimpact.virtual_world.util.ItemStackByteUtil
import java.util.*
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * Virtual Ore API
 */
@Suppress("unused")
object VirtualAPI {

    var random: Random? = null

    /**
     * Set of types Virtual Ores
     */
    @JvmField
    val VIRTUAL_ORES: HashSet<VirtualOreVein> = HashSet(Config.MAX_SIZE_REGISTERED_VIRTUAL_ORES)

    /**
     * Set of types Virtual Fluids
     */
    @JvmField
    val VIRTUAL_FLUIDS: HashSet<VirtualFluidVein> = HashSet(Config.MAX_SIZE_REGISTERED_VIRTUAL_ORES)

    /**
     * Max layers of Virtual ores
     */
    @JvmField
    val LAYERS_VIRTUAL_ORES = 2

    /**
     * Resized Ore Veins
     * First Int = dim
     * Second Int = layer
     */
    @JvmField
    var RESIZE_ORE_VEINS: HashMap<Int, Map<Int, List<VirtualOreVein>>> = HashMap()

    /**
     * Resized Fluid veins
     * Int = dim
     */
    @JvmField
    var RESIZE_FLUID_VEINS: HashMap<Int, List<VirtualFluidVein>> = HashMap()

    //region Ores

    /**
     * Get Virtual Ore by layer and dimension
     *
     * @param layer layer of virtual ore
     * @param dim minecraft dimension
     */
    @JvmStatic
    fun getRandomVirtualOre(layer: Int, dim: Int): VirtualOreVein? {
        if (!RESIZE_ORE_VEINS.contains(dim)) return null
        if (!RESIZE_ORE_VEINS[dim]!!.containsKey(layer)) return null
        var total = 0.0
        RESIZE_ORE_VEINS[dim]!![layer]!!.forEach { candidate ->
            total += candidate.maxWeight
            candidate.weight = total
        }
        val medium = (random?.nextDouble() ?: 0.0) * total

        RESIZE_ORE_VEINS[dim]!![layer]!!.forEach { candidate ->
            if (candidate.weight > medium) {
                candidate.reduceWeight()
                return candidate
            } else {
                candidate.increaseWeight()
            }
        }
        return null
    }

    /**
     * Resize registered ores
     */
    fun resizeOreVeins() {
        RESIZE_ORE_VEINS.clear()
        for (dim in DimensionManager.getIDs()) {
            val resizedLayersVeins = HashMap<Int, List<VirtualOreVein>>()
            for (layer in 0 until LAYERS_VIRTUAL_ORES) {
                val list = ArrayList<VirtualOreVein>()
                for (virtualOre in VIRTUAL_ORES) {
                    if (virtualOre.layer == layer) {
                        for (dimension in virtualOre.dimensions) {
                            if (dimension.first == dim) {
                                list += virtualOre
                            }
                        }
                    }
                }
                resizedLayersVeins[layer] = list
            }
            RESIZE_ORE_VEINS[dim] = resizedLayersVeins
        }
    }

    /**
     * Get virtual vein by id
     *
     * @param oreId ID virtual vein
     */
    @JvmStatic
    fun getVirtualOreVeinById(oreId: Int): VirtualOreVein? {
        return VIRTUAL_ORES.find { it.id == oreId }
    }

    /**
     * Get registered Virtual Ores
     */
    @JvmStatic
    fun getRegisterOres(): List<VirtualOreVein> {
        return VIRTUAL_ORES.toList()
    }

    /**
     * Get current chunk ore info
     */
    @JvmStatic
    fun getOreInfoChunk(ch: Chunk, layer: Int): OreVeinCount? {
        return when(layer) {
            0 -> ch.getOreLayer0()
            1 -> ch.getOreLayer1()
            else -> null
        }
    }

    /**
     * Extract from current Chunk
     */
    @JvmStatic
    fun extractOreFromChunk(ch: Chunk, layer: Int, amount: Int): OreVeinCount? {
        return ch.extractOreFromChunk(layer, amount)
    }

    /**
     * Extract from current Vein chunks
     */
    @JvmStatic
    fun extractOreFromVein(ch: Chunk, layer: Int, amount: Int): OreVeinCount? {
        return ch.extractOreFormVein(layer, amount)
    }

    /**
     * Get current Vein chunks
     */
    @JvmStatic
    fun getVeinChunks(ch: Chunk): List<ChunkCoordIntPair> {
        return ch.getVeinChunks()
    }

    /**
     * Register Virtual Ore
     *
     * @param vein virtual ore
     */
    @JvmStatic
    fun registerVirtualOre(vein: VirtualOreVein) {
        if (IS_DISABLED_VIRTUAL_ORES) return
        if (VIRTUAL_ORES.any { it.id == vein.id }) {
            throw ConcurrentModificationException(
                "Ore vein must not use the identifier of the other ore vein: ${vein.name}"
            )
        }
        VIRTUAL_ORES += vein
    }
    //endregion

    //region Fluids
    @JvmStatic
    fun getRandomVirtualFluid(dim: Int): VirtualFluidVein? {
        if (!RESIZE_FLUID_VEINS.contains(dim)) return null
        var total = 0.0
        RESIZE_FLUID_VEINS[dim]!!.forEach { candidate ->
            total += candidate.maxWeight
            candidate.weight = total
        }
        val medium = (random?.nextDouble() ?: 0.0) * total
        RESIZE_FLUID_VEINS[dim]!!.forEach { candidate ->
            if (candidate.weight > medium) {
                candidate.reduceWeight()
                return candidate
            } else {
                candidate.increaseWeight()
            }
        }
        return null
    }

    /**
     * Get virtual vein by id
     *
     * @param oreId ID virtual vein
     */
    @JvmStatic
    fun getVirtualFluidVeinById(oreId: Int): VirtualFluidVein? {
        return VIRTUAL_FLUIDS.find { it.id == oreId }
    }

    /**
     * Resize registered fluids
     */
    fun resizeFluidVeins() {
        RESIZE_FLUID_VEINS.clear()
        for (dim in DimensionManager.getIDs()) {
            val list = ArrayList<VirtualFluidVein>()
            for (virtualOre in VIRTUAL_FLUIDS) {
                for (dimension in virtualOre.dimensions) {
                    if (dimension.first == dim) {
                        list += virtualOre
                    }
                }
            }
            RESIZE_FLUID_VEINS[dim] = list
        }
    }

    @JvmStatic
    fun getFluidInfoChunk(ch: Chunk): FluidVeinCount? {
        return ch.getFluidLayer()
    }

    /**
     * Extract from current Vein chunks
     */
    @JvmStatic
    fun extractFluidFromVein(ch: Chunk, amount: Int): FluidVeinCount? {
        return ch.extractFluidFormVein(amount)
    }

    /**
     * Get registered Virtual Fluids
     */
    @JvmStatic
    fun getRegisterFluids(): List<VirtualFluidVein> {
        return VIRTUAL_FLUIDS.toList()
    }

    /**
     * Register Virtual Fluid
     *
     * @param vein virtual fluid
     */
    fun registerVirtualFluid(vein: VirtualFluidVein) {
        if (IS_DISABLED_VIRTUAL_FLUIDS) return
        if (VIRTUAL_FLUIDS.any { it.id == vein.id }) {
            throw ConcurrentModificationException(
                "Fluid vein must not use the identifier of the other ore vein: ${vein.name}"
            )
        }
        VIRTUAL_FLUIDS += vein
    }
    //endregion

    @JvmStatic
    fun addCustomObject(stack: ItemStack, label: String, player: EntityPlayerMP) {
        val data = ByteStreams.newDataOutput()
        val dataStack = ItemStackByteUtil.writeItemStackToDataOutput(stack)

        data.write(dataStack)
        data.writeUTF(label)
        data.writeInt(player.worldObj.provider.dimensionId)
        data.writeInt(round(player.posX).toInt())
        data.writeInt(round(player.posZ).toInt())

        player.sendPacket(SetObjectToChunk.transaction(data))
    }

    @JvmStatic
    fun addCustomObject(world: World, obj: ObjectIndicator, x: Int, z: Int) {
        if (!world.isRemote) actionCustomObject(world, obj, x, z, false)
    }

    @JvmStatic
    fun removeCustomObject(world: World, obj: ObjectIndicator, x: Int, z: Int) {
        if (!world.isRemote) actionCustomObject(world, obj, x, z, true)
    }

    @Suppress("UnstableApiUsage")
    private fun actionCustomObject(world: World, obj: ObjectIndicator, x: Int, z: Int, isRemove: Boolean) {
        val data = ByteStreams.newDataOutput()
        val dataStack = ItemStackByteUtil.writeItemStackToDataOutput(obj.getStack())

        data.writeBoolean(isRemove) //isRemove
        data.write(dataStack) //stack
        data.writeUTF(obj.getLabel()) //name
        data.writeInt(world.provider.dimensionId) //dimId
        data.writeInt(x) //x
        data.writeInt(z) //z

        val players = obj.playersRecipients()
        world.playerEntities
            .filter { players.contains(it.gameProfile.name) }
            .forEach {
                it.sendPacket(SetObjectToChunk.transaction(data))
            }
    }
}
