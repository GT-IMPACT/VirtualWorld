package space.gtimpact.virtual_world.common.items

import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.IIcon
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.common.MinecraftForge
import org.lwjgl.input.Keyboard
import space.gtimpact.virtual_world.api.FluidGenerator.getVein
import space.gtimpact.virtual_world.api.OreGenerator.getVeinAndChunk
import space.gtimpact.virtual_world.api.VirtualAPI
import space.gtimpact.virtual_world.api.VirtualAPI.LAYERS_VIRTUAL_ORES
import space.gtimpact.virtual_world.common.world.IModifiableChunk
import space.gtimpact.virtual_world.config.Config.IS_DISABLED_VIRTUAL_FLUIDS
import space.gtimpact.virtual_world.config.Config.IS_DISABLED_VIRTUAL_ORES
import space.gtimpact.virtual_world.extras.send
import space.gtimpact.virtual_world.extras.toTranslate
import space.gtimpact.virtual_world.network.ChangeLayerScannerPacket
import space.gtimpact.virtual_world.network.FindVeinsPacket
import space.gtimpact.virtual_world.network.VirtualOresNetwork
import space.impact.impact_vw.ASSETS
import kotlin.random.Random

class ScannerTool : Item() {

    init {
        FMLCommonHandler.instance().bus().register(this)
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    fun onMouseEvent(event: MouseEvent) {
        val entityPlayer: EntityPlayer = Minecraft.getMinecraft().thePlayer
        if (Keyboard.isKeyDown(Keyboard.KEY_RMENU) || Keyboard.isKeyDown(Keyboard.KEY_LMENU) ||
            Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
        ) {
            entityPlayer.heldItem?.let {
                (it.item as? ScannerTool)?.let {
                    if (event.dwheel != 0) {
                        VirtualOresNetwork.sendToServer(
                            ChangeLayerScannerPacket(
                                entityPlayer.worldObj.provider.dimensionId,
                                entityPlayer.entityId
                            )
                        )
                    }
                    event.isCanceled = true
                }
            }
        }
    }

    private fun ItemStack.setNBT(data: Int, key: String) {
        val nbt = tagCompound ?: NBTTagCompound().apply { tagCompound = this }
        val props = nbt.getTag("props") ?: NBTTagCompound().apply { nbt.setTag("props", this) }
        (props as NBTTagCompound).setInteger(key, data)
    }

    private fun ItemStack.getNBTInt(key: String): Int {
        val nbt = tagCompound?.getCompoundTag("props") ?: return 0
        return nbt.getInteger(key)
    }

    fun changeLayer(player: EntityPlayer, stack: ItemStack) {
        val type = stack.getNBTInt(NBT_TYPE)
        if (type == TYPE_ORES) {
            var realLayer = stack.getNBTInt(NBT_LAYER) + 1
            if (realLayer >= LAYERS_VIRTUAL_ORES) {
                realLayer = 0
            }
            // Set ore layer #
            player.send("scanner.change_layer".toTranslate() + realLayer)
            stack.setNBT(realLayer, NBT_LAYER)
        }
    }

    companion object {
        const val TYPE_ORES = 0
        const val TYPE_FLUIDS = 1

        const val TYPES_COUNT = 2

        const val NBT_TYPE = "type_mode"
        const val NBT_LAYER = "layer_id"
    }

    @SideOnly(Side.CLIENT)
    lateinit var icon: IIcon

    @SideOnly(Side.CLIENT)
    override fun registerIcons(reg: IIconRegister) {
        icon = reg.registerIcon("$ASSETS:ore_scanner")
    }

    @SideOnly(Side.CLIENT)
    override fun getIconFromDamage(meta: Int): IIcon {
        return icon
    }

    @SideOnly(Side.CLIENT)
    override fun addInformation(
        stack: ItemStack,
        player: EntityPlayer,
        tooltip: MutableList<Any?>,
        f3: Boolean
    ) {
        val mode = stack.getNBTInt(NBT_TYPE)
        val layer = stack.getNBTInt(NBT_LAYER)
        // Change scanner mode: SHIFT + Right Click
        tooltip += "scanner.tooltip.0".toTranslate()
        val modName = when (mode) {
            TYPE_ORES -> "scanner.tooltip.2".toTranslate() // Virtual Ores
            TYPE_FLUIDS -> "scanner.tooltip.4".toTranslate() // Virtual Fluids else
            else -> ""
        }
        // Current scanner mode:
        tooltip += "scanner.tooltip.1".toTranslate() + " " + modName
        if (mode == TYPE_ORES) {
            tooltip += "scanner.tooltip.3".toTranslate() // Change ore layer scanner: CTRL + SCROLL
            tooltip += "scanner.tooltip.6".toTranslate() + layer // Current ore layer: #
        }
        // To scan the area use Right Click
        tooltip += "scanner.tooltip.5".toTranslate()
    }

    init {
        setMaxStackSize(1)
        unlocalizedName = "virtual_ore_scanner"
        if (!IS_DISABLED_VIRTUAL_ORES && !IS_DISABLED_VIRTUAL_FLUIDS) {
            GameRegistry.registerItem(this, "virtual_ore_scanner")
        }
    }

    override fun onItemRightClick(stack: ItemStack, world: World, player: EntityPlayer): ItemStack? {
        if (!world.isRemote) {

            var type = stack.getNBTInt(NBT_TYPE)
            val layer = if (type == TYPE_ORES) stack.getNBTInt(NBT_LAYER) else 0

//            if (player.isSneaking) {
//                type++
//
//                if (type >= TYPES_COUNT) {
//                    type = 0
//                }
//
//                when (type) {
//                    TYPE_ORES -> player.send("scanner.change_mode.0".toTranslate()) //Set mod: Underground Ores
//                    TYPE_FLUIDS -> player.send("scanner.change_mode.1".toTranslate()) //Set mod: Underground Ores
//                }
//                stack.setNBT(type, NBT_TYPE)
//                return super.onItemRightClick(stack, world, player)
//            }

            val radius = 11

            val chX = player.posX.toInt() shr 4
            val chZ = player.posZ.toInt() shr 4

            val chunkss = world.getChunkFromChunkCoords(chX, chZ)

            if (chunkss is IModifiableChunk) {

                if (player.isSneaking) {
                    val nbt = NBTTagCompound()
                    val random = Random.nextInt(1, 8999)
                    nbt.setInteger("test", random)
                    chunkss.setNbt(nbt)
                    player.send("set test: $random")
                } else {
                    val test = chunkss.getNbt()?.getInteger("test")
                    player.send("test: $test")
                }
            }
//
//            val chunks: ArrayList<Chunk> = ArrayList()
//
//            for (x in -radius..radius) {
//                for (z in -radius..radius) {
//                    if (x != -radius && x != radius && z != -radius && z != radius) {
//                        chunks += world.getChunkFromChunkCoords(chX + x, chZ + z)
//                    }
//                }
//            }
//            val packet = FindVeinsPacket(chX, chZ, player.posX.toInt(), player.posZ.toInt(), radius - 1, type)
//            for (chunk in chunks) {
//                when (type) {
//                    TYPE_ORES -> scanOres(chunk, packet, layer)
//                    TYPE_FLUIDS -> scanFluids(chunk, packet)
//                }
//            }
//            packet.level = radius - 1
//            VirtualOresNetwork.sendToPlayer(packet, player as EntityPlayerMP)
        }
        return super.onItemRightClick(stack, world, player)
    }

    /**
     * Scanning Virtual Fluids
     */
    private fun scanFluids(chunk: Chunk, packet: FindVeinsPacket) {
        VirtualAPI.generateFluidRegion(chunk).also { region ->
            region.getVein(chunk)?.let { veinFluid ->
                VirtualAPI.getVirtualFluidVeinById(veinFluid.fluidId).also { vein ->
                    val size = veinFluid.size.toDouble() / vein.rangeSize.last.toDouble() * 100.0
                    fillPacketForChunk(chunk, packet, vein.id, size.toInt() / 1000)
                }
            }
        }
    }

    /**
     * Scanning Virtual Ores
     */
    private fun scanOres(chunk: Chunk, packet: FindVeinsPacket, layer: Int) {



        VirtualAPI.generateOreRegion(chunk).also { region ->
            region.getVeinAndChunk(chunk, layer)?.let { (veinOre, chunkOre) ->
                VirtualAPI.getVirtualOreVeinInChunk(veinOre, layer, region.dim)?.also { ore ->
                    val size = chunkOre.size.toDouble() / ore.rangeSize.last.toDouble() * 100.0
                    fillPacketForChunk(chunk, packet, ore.id, size.toInt() / 1000)
                }
            }
        }
    }

    /**
     * Fill packet by chunk`s coordinates
     */
    private fun fillPacketForChunk(chunk: Chunk, packet: FindVeinsPacket, idComponent: Int, size: Int) {
        for (xx in 0..15) {
            for (zz in 0..15) {
                packet.addRenderComponent(chunk.xPosition * 16 + xx, chunk.zPosition * 16 + zz, idComponent, size)
            }
        }
    }
}