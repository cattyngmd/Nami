package me.kiriyaga.nami.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {
    private static final String MIXIN_PACKAGE = "me.kiriyaga.nami.mixin";

    private static boolean loaded = false;
    public static boolean isSodiumPresent;

    @Override
    public void onLoad(String mixinPackage) {
        if (loaded) return;

        isSodiumPresent = FabricLoader.getInstance().isModLoaded("sodium");

        loaded = true;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.startsWith(MIXIN_PACKAGE)) {
            throw new RuntimeException("Mixin " + mixinClassName + " is not in the mixin package");
        }

        if (mixinClassName.endsWith("MixinSodiumWorldRenderer")) {
            return isSodiumPresent;
        }

        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
