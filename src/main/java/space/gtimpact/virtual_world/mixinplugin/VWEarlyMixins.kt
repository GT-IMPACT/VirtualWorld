package space.gtimpact.virtual_world.mixinplugin

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader
import cpw.mods.fml.relauncher.IFMLLoadingPlugin

@IFMLLoadingPlugin.MCVersion("1.7.10")
class VWEarlyMixins : IFMLLoadingPlugin, IEarlyMixinLoader {

    private val transformerClasses: Array<String> = arrayOf()

    override fun getMixinConfig(): String {
        return "mixins.impact_vw.early.json"
    }

    override fun getMixins(loadedCoreMods: MutableSet<String>?): MutableList<String> {
        return Mixins.values()
            .filter { it.phase == Phase.EARLY }
            .map { it.mixinClasses }
            .flatten()
            .toMutableList()
    }

    override fun getASMTransformerClass(): Array<String> {
        return transformerClasses
    }

    override fun getModContainerClass(): String? {
        return null
    }

    override fun getSetupClass(): String? {
        return null
    }

    override fun injectData(data: MutableMap<String, Any>?) {}

    override fun getAccessTransformerClass(): String? {
        return null
    }
}