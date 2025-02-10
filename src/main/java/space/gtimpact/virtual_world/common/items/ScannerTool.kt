package space.gtimpact.virtual_world.common.items

import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import ic2.api.item.IElectricItem
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
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
import space.gtimpact.virtual_world.addon.ic2.IC2ElectricManager
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
import kotlin.math.max

class ScannerTool : Item(), IElectricItem {

    companion object {
        const val TYPE_ORES = 0
        const val TYPE_FLUIDS = 1

        const val TYPES_COUNT = 2

        const val NBT_TYPE = "type_mode"
        const val NBT_LAYER = "layer_id"

        const val COUNT_ITEM_REGISTERED = 4

        internal val INSTANCE = ScannerTool()
    }

    fun registerItem() {
        if (!IS_DISABLED_SCANNER_TOOL) {

            setHasSubtypes(true)
            setUnlocalizedName("virtual_ore_scanner")
            if (!Config.enableDebug) setMaxStackSize(1)

            GameRegistry.registerItem(INSTANCE, "virtual_ore_scanner")

            FMLCommonHandler.instance().bus().register(this)
            MinecraftForge.EVENT_BUS.register(this)
        }
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

    override fun getUnlocalizedName(stack: ItemStack): String {
        return super.getUnlocalizedName() + "." + stack.getItemDamage()
    }

    private val icon = arrayOfNulls<IIcon>(COUNT_ITEM_REGISTERED)

    @SideOnly(Side.CLIENT)
    override fun registerIcons(reg: IIconRegister) {
        repeat(COUNT_ITEM_REGISTERED) {
            icon[it] = reg.registerIcon("$ASSETS:ore_scanner_$it")
        }
    }

    @SideOnly(Side.CLIENT)
    override fun getIconFromDamage(meta: Int): IIcon? {
        return icon[meta]
    }

    @SideOnly(Side.CLIENT)
    override fun getSubItems(item: Item, tab: CreativeTabs?, list: MutableList<ItemStack>) {
        repeat(COUNT_ITEM_REGISTERED) {
            list.add(ItemStack(item, 1, it))
        }
    }

    fun radiusByStack(stack: ItemStack): Int {
        return when (stack.itemDamage) {
            0 -> 8
            1 -> 12
            2 -> 16
            3 -> 40
            else -> 8
        }
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

        tooltip += "scanner.tooltip.0".toTranslate() // Change scanner mode: SHIFT + Right Click
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
        tooltip += ""
        tooltip += "scanner.tooltip.7".toTranslate(radiusByStack(stack))
        tooltip += ""

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

                    val radius = radiusByStack(stack)

                    if (IC2ElectricManager.getCharge(stack) >= 0) {
//                        IC2ElectricManager.discharge(stack, 1000.0, 2, true, false, false)
                        when (type) {
                            TYPE_ORES -> scanOres(world, layer, player as EntityPlayerMP, radius)
                            TYPE_FLUIDS -> scanFluids(world, player as EntityPlayerMP, radius)
                        }
                    }
                }
            }
        }
        return super.onItemRightClick(stack, world, player)
    }

    override fun canProvideEnergy(stack: ItemStack): Boolean {
        return true
    }

    override fun getChargedItem(stack: ItemStack): Item {
        return this
    }

    override fun getEmptyItem(stack: ItemStack): Item {
        return this
    }

    override fun getMaxCharge(stack: ItemStack): Double {
        return 500_000.0 + 250_000.0 * (getTier(stack) - 4)
    }

    override fun getTier(stack: ItemStack): Int {
        return max(stack.itemDamage + 4, 4)
    }

    override fun getTransferLimit(stack: ItemStack): Double {
        return 1000.0
    }
}
