package space.gtimpact.virtual_world.api

import com.google.common.io.ByteStreams
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.world.ChunkCoordIntPair
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.common.DimensionManager
import space.gtimpact.virtual_world.api.ResourceGenerator.getVeinChunks
import space.gtimpact.virtual_world.config.Config
import space.gtimpact.virtual_world.config.Config.IS_DISABLED_VIRTUAL_FLUIDS
import space.gtimpact.virtual_world.config.Config.IS_DISABLED_VIRTUAL_ORES
import space.gtimpact.virtual_world.network.SetObjectToChunkPacket
import space.gtimpact.virtual_world.network.sendPacket
import space.gtimpact.virtual_world.util.ItemStackByteUtil
import kotlin.math.round

/**
 * Virtual Ore API
 */
@Suppress("unused")
object VirtualAPI {

    /**
     * Set of types Virtual Ores
     */
    @JvmField
    val virtualOres: HashSet<VirtualOreVein> = HashSet(Config.MAX_SIZE_REGISTERED_VIRTUAL_ORES)

    /**
     * Set of types Virtual Fluids
     */
    @JvmField
    val virtualFluids: HashSet<VirtualFluidVein> = HashSet(Config.MAX_SIZE_REGISTERED_VIRTUAL_ORES)

    /**
     * Max layers of Virtual ores
     */
    const val LAYERS_VIRTUAL_ORES = 2

    /**
     * Resized Ore Veins
     * First Int = dim
     * Second Int = layer
     */
    @JvmStatic
    val resizeOreVeins: HashMap<Int, Map<Int, List<VirtualOreVein>>> by lazy {
        val map = hashMapOf<Int, Map<Int, List<VirtualOreVein>>>()
        for (dim in DimensionManager.getIDs()) {
            val resizedLayersVeins = HashMap<Int, List<VirtualOreVein>>()
            for (layer in 0 until LAYERS_VIRTUAL_ORES) {
                val list = ArrayList<VirtualOreVein>()
                for (virtualOre in virtualOres) {
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
            map[dim] = resizedLayersVeins
        }
        map
    }

    /**
     * Resized Fluid veins
     * Int = dim
     */
    @JvmStatic
    val resizeFluidVeins: HashMap<Int, List<VirtualFluidVein>> by lazy {
        val map = hashMapOf<Int, List<VirtualFluidVein>>()
        for (dim in DimensionManager.getIDs()) {
            val list = ArrayList<VirtualFluidVein>()
            for (virtualOre in virtualFluids) {
                for (dimension in virtualOre.dimensions) {
                    if (dimension.first == dim) {
                        list += virtualOre
                    }
                }
            }
            map[dim] = list
        }
        map
    }

    /**
     * Get virtual vein by id
     *
     * @param oreId ID virtual vein
     */
    @JvmStatic
    fun getVirtualOreVeinById(oreId: Int): VirtualOreVein? {
        return virtualOres.find { it.id == oreId }
    }

    /**
     * Get registered Virtual Ores
     */
    @JvmStatic
    fun getRegisterOres(): List<VirtualOreVein> {
        return virtualOres.toList()
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
        if (virtualOres.any { it.id == vein.id }) {
            throw ConcurrentModificationException(
                "Ore vein must not use the identifier of the other ore vein: ${vein.name}"
            )
        }
        virtualOres += vein
    }
    //endregion

    //region Fluids
    @JvmStatic
    fun getRandomVirtualFluid(dim: Int, randomCoefficient: Double): VirtualFluidVein? {
        if (!resizeFluidVeins.contains(dim)) return null
        var total = 0.0
        resizeFluidVeins[dim]!!.forEach { candidate ->
            total += candidate.maxWeight
            candidate.weight = total
        }
        val medium = randomCoefficient * total
        resizeFluidVeins[dim]!!.forEach { candidate ->
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
        return virtualFluids.find { it.id == oreId }
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
        return ch.extractFluidFromVein(amount)
    }

    /**
     * Get registered Virtual Fluids
     */
    @JvmStatic
    fun getRegisterFluids(): List<VirtualFluidVein> {
        return virtualFluids.toList()
    }

    /**
     * Register Virtual Fluid
     *
     * @param vein virtual fluid
     */
    fun registerVirtualFluid(vein: VirtualFluidVein) {
        if (IS_DISABLED_VIRTUAL_FLUIDS) return
        if (virtualFluids.any { it.id == vein.id }) {
            throw ConcurrentModificationException(
                "Fluid vein must not use the identifier of the other ore vein: ${vein.name}"
            )
        }
        virtualFluids += vein
    }
    //endregion

    @JvmStatic
    @Suppress("UnstableApiUsage")
    fun addCustomObject(stack: ItemStack, label: String, player: EntityPlayerMP) {
        val data = ByteStreams.newDataOutput()
        val dataStack = ItemStackByteUtil.writeItemStackToDataOutput(stack)

        data.writeBoolean(false)
        data.write(dataStack)
        data.writeUTF(label)
        data.writeInt(player.worldObj.provider.dimensionId)
        data.writeInt(round(player.posX).toInt())
        data.writeInt(round(player.posZ).toInt())

        player.sendPacket(SetObjectToChunkPacket.transaction(data))
    }

    @JvmStatic
    fun addCustomObject(world: World, obj: ObjectIndicator, blockX: Int, blockZ: Int) {
        if (!world.isRemote) actionCustomObject(world, obj, blockX, blockZ, false)
    }

    @JvmStatic
    fun removeCustomObject(world: World, obj: ObjectIndicator, blockX: Int, blockZ: Int) {
        if (!world.isRemote) actionCustomObject(world, obj, blockX, blockZ, true)
    }

    @Suppress("UnstableApiUsage")
    private fun actionCustomObject(world: World, obj: ObjectIndicator, blockX: Int, blockZ: Int, isRemove: Boolean) {
        val data = ByteStreams.newDataOutput()
        val dataStack = ItemStackByteUtil.writeItemStackToDataOutput(obj.getStack())

        data.writeBoolean(isRemove) //isRemove
        data.write(dataStack) //stack
        data.writeUTF(obj.getLabel()) //name
        data.writeInt(world.provider.dimensionId) //dimId
        data.writeInt(blockX) //x
        data.writeInt(blockZ) //z

        val players = obj.playersRecipients()
        world.playerEntities
            .filter { players.contains(it.gameProfile.name) }
            .forEach {
                it.sendPacket(SetObjectToChunkPacket.transaction(data))
            }
    }
}
