package we.devs.forever.loader;


import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {
    private final List<String> mixins = new ArrayList<>();

    @Override
    public void onLoad(String mixinPackage) {
        try {
            Launch.classLoader.addURL(MixinCache.getRefMapFile().toURI().toURL());
        } catch (MalformedURLException var13) {
//            var13.printStackTrace();
        }
        MixinCache.getRefMapFile().delete();
        MixinCache.getMixins().forEach(x -> {
            mixins.add(x);
            if (FMLLaunchHandler.isDeobfuscatedEnvironment()) {
                try {
                    Launch.classLoader.loadClass(x);
                } catch (ClassNotFoundException e) {
//                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public String getRefMapperConfig() {
        String var1;
        try {
            var1 = MixinCache.getRefMapFile().toURI().toURL().toString();
        } catch (MalformedURLException var3) {
            var1 = "mixins.forever.refmap.json";
        }
        return var1;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return mixins;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
