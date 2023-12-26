package space.gtimpact.virtual_world.mixinplugin

enum class TargetedMod(
    val modName: String,
    val coreModClass: String?,
    val modId: String? = null
) {
    VANILLA("Minecraft", null),
}
