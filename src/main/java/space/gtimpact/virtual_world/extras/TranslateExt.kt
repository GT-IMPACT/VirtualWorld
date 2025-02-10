package space.gtimpact.virtual_world.extras

import net.minecraft.util.StatCollector

fun String.toTranslate(vararg args: Any): String {
    return StatCollector.translateToLocalFormatted(this, *args)
}