package space.gtimpact.virtual_world.mixinplugin

import cpw.mods.fml.relauncher.FMLLaunchHandler
import java.util.function.Supplier

enum class Mixins(builder: Builder) {

    SUPPORT_VANILA_CHUNKS_NBT(
        Builder("Support Vanila Chunks Nbt")
            .setPhase(Phase.EARLY)
            .addTargetedMod(TargetedMod.VANILLA)
            .addMixinClasses("minecraft.ChunkMixin", "minecraft.WorldServerMixin")
            .setApplyIf { true }
            .setSide(Side.SERVER)
    )

    ;

    var mixinClasses: MutableList<String> = mutableListOf()
        private set
    private var targetedMods: MutableList<TargetedMod> = mutableListOf()
    private var excludedMods: MutableList<TargetedMod> = mutableListOf()
    private var applyIf: Supplier<Boolean>? = null
    var phase: Phase? = null
        private set
    private var side: Side? = null

    init {
        this.mixinClasses = builder.mixinClasses
        this.targetedMods = builder.targetedMods
        this.excludedMods = builder.excludedMods
        this.applyIf = builder.applyIf
        this.phase = builder.phase
        this.side = builder.side
        if (mixinClasses.isEmpty()) {
            throw java.lang.RuntimeException("No mixin class specified for Mixin : " + this.name)
        }
        if (targetedMods.isEmpty()) {
            throw java.lang.RuntimeException("No targeted mods specified for Mixin : " + this.name)
        }
        if (this.applyIf == null) {
            throw java.lang.RuntimeException("No ApplyIf function specified for Mixin : " + this.name)
        }
        if (this.phase == null) {
            throw java.lang.RuntimeException("No Phase specified for Mixin : " + this.name)
        }
        if (this.side == null) {
            throw java.lang.RuntimeException("No Side function specified for Mixin : " + this.name)
        }
    }

    private fun shouldLoadSide(): Boolean {
        return (side == Side.BOTH || (side == Side.SERVER && FMLLaunchHandler.side().isServer)
                || (side == Side.CLIENT && FMLLaunchHandler.side().isClient))
    }

    private fun allModsLoaded(
        targetedMods: List<TargetedMod>?,
        loadedCoreMods: Set<String>,
        loadedMods: Set<String>
    ): Boolean {
        if (targetedMods!!.isEmpty()) return false

        for (target in targetedMods) {
            if (target == TargetedMod.VANILLA) continue

            // Check coremod first
            if (loadedCoreMods.isNotEmpty() && target.coreModClass != null && !loadedCoreMods.contains(target.coreModClass)) return false
            else if (loadedMods.isNotEmpty() && target.modId != null && !loadedMods.contains(target.modId)) return false
        }

        return true
    }

    private fun noModsLoaded(
        targetedMods: List<TargetedMod>?,
        loadedCoreMods: Set<String>,
        loadedMods: Set<String>
    ): Boolean {
        if (targetedMods!!.isEmpty()) return true

        for (target in targetedMods) {
            if (target == TargetedMod.VANILLA) continue

            // Check coremod first
            if (loadedCoreMods.isNotEmpty() && target.coreModClass != null && loadedCoreMods.contains(target.coreModClass)) return false
            else if (loadedMods.isNotEmpty() && target.modId != null && loadedMods.contains(target.modId)) return false
        }

        return true
    }

    private fun shouldLoad(loadedCoreMods: Set<String>, loadedMods: Set<String>): Boolean {
        return (shouldLoadSide() && applyIf?.get() == true
                && allModsLoaded(targetedMods, loadedCoreMods, loadedMods)
                && noModsLoaded(excludedMods, loadedCoreMods, loadedMods))
    }

}


class Builder(private val name: String) {
    val mixinClasses: MutableList<String> = ArrayList()
    val targetedMods: MutableList<TargetedMod> = ArrayList()
    val excludedMods: MutableList<TargetedMod> = ArrayList()
    var applyIf: Supplier<Boolean>? = null
    var phase: Phase? = null
    var side: Side? = null

    fun addMixinClasses(vararg mixinClasses: String): Builder {
        this.mixinClasses.addAll(mixinClasses)
        return this
    }

    fun setPhase(phase: Phase?): Builder {
        if (this.phase != null) {
            throw RuntimeException("Trying to define Phase twice for " + this.name)
        }
        this.phase = phase
        return this
    }

    fun setSide(side: Side?): Builder {
        if (this.side != null) {
            throw RuntimeException("Trying to define Side twice for " + this.name)
        }
        this.side = side
        return this
    }

    fun setApplyIf(applyIf: Supplier<Boolean>): Builder {
        this.applyIf = applyIf
        return this
    }

    fun addTargetedMod(mod: TargetedMod): Builder {
        targetedMods.add(mod)
        return this
    }

    fun addExcludedMod(mod: TargetedMod): Builder {
        excludedMods.add(mod)
        return this
    }
}

enum class Side {
    BOTH,
    CLIENT,
    SERVER
}

enum class Phase {
    EARLY,
    LATE,
}