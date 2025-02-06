package space.gtimpact.virtual_world.mixinplugin;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@IFMLLoadingPlugin.MCVersion("1.7.10")
public class VWEarlyMixins implements IFMLLoadingPlugin, IEarlyMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.impact_vw.early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        return Arrays.stream(Mixins.values())
                .filter(mixin -> mixin.getPhase() == Phase.EARLY)
                .map(Mixins::getMixinClasses)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
