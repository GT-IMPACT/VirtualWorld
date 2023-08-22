package space.impact.virtual_world.network

import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteStreams
import cpw.mods.fml.common.network.FMLEmbeddedChannel
import cpw.mods.fml.common.network.FMLOutboundHandler
import cpw.mods.fml.common.network.FMLOutboundHandler.OutboundTarget
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.network.internal.FMLProxyPacket
import cpw.mods.fml.relauncher.Side
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.MessageToMessageCodec
import net.minecraft.entity.player.EntityPlayerMP
import java.util.*

@Sharable
object VirtualOresNetwork : MessageToMessageCodec<FMLProxyPacket, IPacket>() {

    private val mChannel: EnumMap<Side, FMLEmbeddedChannel> =
        NetworkRegistry.INSTANCE.newChannel("VirtualOresNetworkChannel", this, HandlerShared)

    private val mSubChannel: Array<IPacket> = arrayOf(
        FindVeinsPacket(), ChangeLayerScannerPacket()
    )

    override fun encode(ctx: ChannelHandlerContext, msg: IPacket, out: MutableList<Any>) {
        out.add(
            FMLProxyPacket(
                Unpooled.buffer().writeByte(msg.getPacketID()).writeBytes(msg.encode()).copy(),
                ctx.channel().attr(NetworkRegistry.FML_CHANNEL).get()
            )
        )
    }

    @Suppress("UnstableApiUsage")
    override fun decode(ctx: ChannelHandlerContext, msg: FMLProxyPacket, out: MutableList<Any>) {
        val data = ByteStreams.newDataInput(msg.payload().array())
        out.add(mSubChannel[data.readByte().toInt()].decode(data))
    }

    fun sendToPlayer(msg: IPacket, player: EntityPlayerMP) {
        mChannel[Side.SERVER]?.apply {
            attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.PLAYER)
            attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player)
            writeAndFlush(msg)
        }
    }

    fun sendToServer(aPacket: IPacket?) {
        mChannel[Side.CLIENT]?.apply {
            attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.TOSERVER)
            writeAndFlush(aPacket)
        }
    }
}

@Sharable
object HandlerShared : SimpleChannelInboundHandler<IPacket>() {
    override fun channelRead0(ctx: ChannelHandlerContext, msg: IPacket) {
        msg.process()
    }
}

interface IPacket {
    fun getPacketID(): Int
    fun encode(): ByteArray
    fun process()
    fun decode(data: ByteArrayDataInput): IPacket
}

