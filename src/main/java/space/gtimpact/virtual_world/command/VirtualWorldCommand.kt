package space.gtimpact.virtual_world.command

import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import space.gtimpact.virtual_world.command.scan.region

class VirtualWorldCommand : CommandBase() {

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }

    override fun getCommandName(): String {
        return "vw"
    }

    override fun getCommandUsage(p_71518_1_: ICommandSender?): String {
        return "vw"
    }

    override fun canCommandSenderUseCommand(p_71519_1_: ICommandSender?): Boolean {
        return true
    }

    override fun processCommand(ics: ICommandSender, args: Array<String>) {
        when (args[0]) {
            "region" -> region(world = ics.entityWorld, player = getCommandSenderAsPlayer(ics), args = args)
            else -> Unit
        }
    }
}
