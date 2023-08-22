package space.impact.virtual_world.extras

import net.minecraft.util.StatCollector

fun String.toTranslate(): String {
    return StatCollector.translateToLocal(this)
}