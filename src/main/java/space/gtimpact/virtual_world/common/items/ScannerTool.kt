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
import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.common.MinecraftForge
import org.lwjgl.input.Keyboard
import space.gtimpact.virtual_world.ASSETS
import space.gtimpact.virtual_world.api.VirtualAPI
import space.gtimpact.virtual_world.api.VirtualAPI.LAYERS_VIRTUAL_ORES
import space.gtimpact.virtual_world.api.extractFluidFromVein
import space.gtimpact.virtual_world.api.extractOreFromChunk
import space.gtimpact.virtual_world.api.prospect.scanFluids
import space.gtimpact.virtual_world.api.prospect.scanOres
import space.gtimpact.virtual_world.config.Config
import space.gtimpact.virtual_world.config.Config.IS_DISABLED_SCANNER_TOOL
import space.gtimpact.virtual_world.extras.send
import space.gtimpact.virtual_world.extras.toTranslate
import space.gtimpact.virtual_world.network.MetaBlockGlassPacket
import space.impact.packet_network.network.NetworkHandler.sendToServer


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
            entityPlayer.heldItem?.also {
                (it.item as? ScannerTool)?.also {
                    if (event.dwheel != 0) {
                        entityPlayer.sendToServer(MetaBlockGlassPacket.transaction(event.dwheel > 0))
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

    fun registerItem() {
        if (!Config.enableDebug) setMaxStackSize(1)
        unlocalizedName = "virtual_ore_scanner"
        if (!IS_DISABLED_SCANNER_TOOL) {
            GameRegistry.registerItem(this, "virtual_ore_scanner")
        }
    }

    companion object {
        const val TYPE_ORES = 0
        const val TYPE_FLUIDS = 1

        const val TYPES_COUNT = 2

        const val NBT_TYPE = "type_mode"
        const val NBT_LAYER = "layer_id"

        internal val INSTANCE = ScannerTool()
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
        tooltip: MutableList<String?>,
        f3: Boolean,
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

        if (Config.enableDebug)
            tooltip += listOf(
                "2 stackSize create point item",
                "3..64 stackSize extract current chunk stackSize * 1000",
            )
    }

    override fun onItemRightClick(stack: ItemStack, world: World, player: EntityPlayer): ItemStack? {
        if (!world.isRemote) {

            var type = stack.getNBTInt(NBT_TYPE)
            val layer = if (type == TYPE_ORES) stack.getNBTInt(NBT_LAYER) else 0

            when (stack.stackSize) {
                //for debug
                2 -> if (player is EntityPlayerMP) {
                    VirtualAPI.addCustomObject(stack, "Test Point ${world.rand.nextInt(99)}", player)
                }

                in 3..64 -> if (Config.enableDebug && player.capabilities.isCreativeMode) {
                    val chunk = world.getChunkFromBlockCoords(player.posX.toInt(), player.posZ.toInt())

                    when (type) {
                        TYPE_ORES -> chunk.extractOreFromChunk(layer, 1000 * stack.stackSize)?.also { data ->
                            player.send("${data.vein.name}: ${data.size}")
                        }

                        TYPE_FLUIDS -> chunk.extractFluidFromVein(1000 * stack.stackSize / 16)?.also { data ->
                            player.send("${data.vein.name}: ${data.size}")
                        }
                    }
                }

                else -> {
                    if (player.isSneaking) {
                        type++

                        if (type >= TYPES_COUNT) {
                            type = 0
                        }

                        when (type) {
                            TYPE_ORES -> player.send("scanner.change_mode.0".toTranslate()) //Set mod: Underground Ores
                            TYPE_FLUIDS -> player.send("scanner.change_mode.1".toTranslate()) //Set mod: Underground Fluids
                        }
                        stack.setNBT(type, NBT_TYPE)
                        return super.onItemRightClick(stack, world, player)
                    }

                    val radius = 20 // TODO

                    when (type) {
                        TYPE_ORES -> scanOres(world, layer, player as EntityPlayerMP, radius)
                        TYPE_FLUIDS -> scanFluids(world, player as EntityPlayerMP, radius)
                    }
                }
            }
        }
        return super.onItemRightClick(stack, world, player)
    }
}
