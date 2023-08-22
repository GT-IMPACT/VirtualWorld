package space.impact.virtual_world.mixinplugin

import com.google.common.io.Files
import java.nio.file.Path
import java.util.*

enum class TargetedMod(@JvmField val modName: String, jarNamePrefix: String, @JvmField val loadInDevelopment: Boolean) {
    VANILLA("Minecraft", "unused", true);

    private val jarNamePrefixLowercase: String = jarNamePrefix.lowercase(Locale.getDefault())

    fun isMatchingJar(path: Path): Boolean {
        val pathString = path.toString()
        val nameLowerCase = Files.getNameWithoutExtension(pathString).lowercase(Locale.getDefault())
        val fileExtension = Files.getFileExtension(pathString)
        return (nameLowerCase.startsWith(jarNamePrefixLowercase)
                && ("jar" == fileExtension || "litemod" == fileExtension))
    }

    override fun toString(): String {
        return ("TargetedMod{" + "modName='"
                + modName + '\'' + ", jarNamePrefixLowercase='"
                + jarNamePrefixLowercase + '\'' + '}')
    }
}
