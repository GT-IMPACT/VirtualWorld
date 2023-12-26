package space.gtimpact.virtual_world.extras

import net.minecraft.util.StatCollector

fun String.toTranslate(): String {
    return StatCollector.translateToLocal(this)
}