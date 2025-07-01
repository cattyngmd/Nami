package me.kiriyaga.essentials.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.feature.module.impl.world.NameProtectModule;
import me.kiriyaga.essentials.mixininterface.IMinecraft;
import net.minecraft.text.Style;
import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static me.kiriyaga.essentials.Essentials.MODULE_MANAGER;

@Mixin(TextVisitFactory.class)
public class MixinTextVisitFactory implements IMinecraft {

    @ModifyVariable(method = "visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private static String replaceText(String value) {
        if (MODULE_MANAGER.getModule(NameProtectModule.class).isEnabled()) return value.replaceAll(mc.getSession().getUsername(), "Protected"); // TODO unhardcode that. the day im gonna write runtime lists
        return value;
    }
}