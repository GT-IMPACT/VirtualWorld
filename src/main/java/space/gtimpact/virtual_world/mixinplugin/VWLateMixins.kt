package space.gtimpact.virtual_world.mixinplugin

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader
import com.gtnewhorizon.gtnhmixins.LateMixin

@LateMixin
class VWLateMixins : ILateMixinLoader {
    override fun getMixinConfig(): String {
        return "mixins.impact_vw.late.json"
    }

    override fun getMixins(loadedMods: MutableSet<String>): MutableList<String> {
        return Mixins.values()
            .filter { it.phase == Phase.LATE }
            .map { it.mixinClasses }
            .flatten()
            .toMutableList()
    }
}
