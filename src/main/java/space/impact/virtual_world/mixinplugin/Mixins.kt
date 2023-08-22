package space.impact.virtual_world.mixinplugin

import cpw.mods.fml.relauncher.FMLLaunchHandler

enum class Mixins {

    ChunkMixin("minecraft.ChunkMixin", TargetedMod.VANILLA),
    WorldServerMixin("minecraft.WorldServerMixin", Side.SERVER, TargetedMod.VANILLA),

    ;

    @JvmField
    val mixinClass: String

    @JvmField
    val targetedMods: List<TargetedMod>
    private val side: Side

    constructor(mixinClass: String, side: Side, vararg targetedMods: TargetedMod) {
        this.mixinClass = mixinClass
        this.targetedMods = listOf(*targetedMods)
        this.side = side
    }

    constructor(mixinClass: String, vararg targetedMods: TargetedMod) {
        this.mixinClass = mixinClass
        this.targetedMods = listOf(*targetedMods)
        side = Side.BOTH
    }

    fun shouldLoad(loadedMods: List<TargetedMod>): Boolean {
        return ((side == Side.BOTH || side == Side.SERVER && FMLLaunchHandler.side().isServer || side == Side.CLIENT && FMLLaunchHandler.side().isClient)
                && loadedMods.containsAll(targetedMods))
    }

    internal enum class Side {
        BOTH,
        CLIENT,
        SERVER
    }
}
