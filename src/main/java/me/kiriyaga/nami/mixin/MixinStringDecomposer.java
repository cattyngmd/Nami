package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.feature.module.impl.miscellaneous.NameProtectModule;
import net.minecraft.util.StringDecomposer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(StringDecomposer.class)
public class MixinStringDecomposer {

    @ModifyVariable(method = "iterateFormatted(Ljava/lang/String;ILnet/minecraft/network/chat/Style;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private static String replaceText(String value) {
        if (MODULE_MANAGER.getStorage() == null || MODULE_MANAGER.getStorage().getByClass(NameProtectModule.class) == null)
            return value;

        if (MODULE_MANAGER.getStorage().getByClass(NameProtectModule.class).isEnabled()) return value.replaceAll(MC.getUser().getName(), "NamiClient"); // TODO unhardcode that. the day im gonna write runtime lists
        return value;
    }
}