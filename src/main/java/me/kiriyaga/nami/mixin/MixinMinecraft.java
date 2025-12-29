package me.kiriyaga.nami.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.kiriyaga.nami.core.macro.model.Macro;
import me.kiriyaga.nami.event.impl.DissconectEvent;
import me.kiriyaga.nami.event.impl.EntityDeathEvent;
import me.kiriyaga.nami.event.impl.InteractionEvent;
import me.kiriyaga.nami.event.impl.OpenScreenEvent;
import me.kiriyaga.nami.feature.module.impl.client.FontModule;
import me.kiriyaga.nami.feature.module.impl.combat.AuraModule;
import me.kiriyaga.nami.feature.module.impl.visuals.ESPModule;
import me.kiriyaga.nami.feature.module.impl.exploits.AirPlaceModule;
import me.kiriyaga.nami.feature.module.impl.world.AutoEatModule;
import me.kiriyaga.nami.feature.module.impl.world.FastPlaceModule;
import me.kiriyaga.nami.feature.module.impl.exploits.NoHitDelayModule;
import me.kiriyaga.nami.feature.setting.impl.KeyBindSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import me.kiriyaga.nami.feature.module.Module;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static me.kiriyaga.nami.Nami.*;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
    @Shadow
    public int missTime;
    @Shadow private int rightClickDelay;
    @Shadow @Nullable public LocalPlayer player;
    @Shadow @Final
    public Options options;
    @Shadow @Nullable
    public MultiPlayerGameMode gameMode;
    private int holdTicks = 0;
    @Shadow public ClientLevel level;
    private final Set<Integer> deadList = new HashSet<>();

    @Inject(method = "disconnectFromWorld(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), cancellable = true)
    private void onDisconnect(Component reason, CallbackInfo ci) {
        DissconectEvent ev = new DissconectEvent();
        EVENT_MANAGER.post(ev);

        if (ev.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "handleKeybinds", at = @At("TAIL"))
    private void onHandleInputEvents_TAIL(CallbackInfo ci) {
        if (MC == null || MC.mouseHandler == null || MC.screen != null) return;

        for (Module module : MODULE_MANAGER.getStorage().getAll()) {
            if (module == null) continue;
            KeyBindSetting bind = module.getKeyBind();
            if (bind == null) continue;

            if (bind.get() != KeyBindSetting.KEY_NONE) {
                boolean currentlyPressed = bind.isPressed();

                if (bind.isHoldMode()) {
                    if (currentlyPressed && !module.isEnabled()) {
                        module.setEnabled(true);
                    } else if (!currentlyPressed && module.isEnabled()) {
                        module.setEnabled(false);
                    }
                } else {
                    if (currentlyPressed && !bind.wasPressedLastTick()) {
                        module.toggle();
                    }
                }

                bind.setWasPressedLastTick(currentlyPressed);
            }
        }

        for (Macro macro : MACRO_MANAGER.getAll()) {
            int keyCode = macro.getKeyCode();
            boolean currentlyPressed = MACRO_MANAGER.isKeyPressed(keyCode);
            boolean wasPressed = MACRO_MANAGER.wasKeyPressedLastTick(keyCode);

            if (currentlyPressed && !wasPressed) {
                if (MC.player != null) {
                    MC.player.connection.sendChat(macro.getMessage());
                }
            }

            MACRO_MANAGER.setKeyPressedLastTick(keyCode, currentlyPressed);
        }
    }


    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isHandsBusy()Z", ordinal = 0, shift = At.Shift.BEFORE))
    private void doItemUse(CallbackInfo info) {

        AirPlaceModule airPlace = MODULE_MANAGER.getStorage().getByClass(AirPlaceModule.class);
        FastPlaceModule fastPlace = MODULE_MANAGER.getStorage().getByClass(FastPlaceModule.class);

        if (airPlace == null || fastPlace == null) return;

        if (airPlace.isEnabled() && airPlace.cooldown <= 0) {
            rightClickDelay = airPlace.delay.get();
            return;
        }

        if (!fastPlace.isEnabled()) return;

        if (MC == null || MC.player == null) return;

        ItemStack heldStack = MC.player.getMainHandItem();
        if (heldStack == null) return;

        Item heldItem = heldStack.getItem();
        if (heldItem == null) return;

        Identifier heldId = BuiltInRegistries.ITEM.getKey(heldItem);
        if (heldId == null) return;

        if (fastPlace.whitelist.get() && !fastPlace.whitelist.isWhitelisted(heldId)) return;

        if (fastPlace.blacklist.get() && fastPlace.blacklist.isWhitelisted(heldId)) return;

        if (holdTicks >= fastPlace.startDelay.get()) {
            rightClickDelay = fastPlace.delay.get();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo info) {
        FastPlaceModule fastPlace = MODULE_MANAGER.getStorage().getByClass(FastPlaceModule.class);

        if (fastPlace == null) return;

        if (fastPlace.isEnabled() && MC != null && MC.options != null && MC.options.keyUse.isDown()) {
            holdTicks++;
        } else {
            holdTicks = 0;
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        OpenScreenEvent event = new OpenScreenEvent(screen);

        EVENT_MANAGER.post(event);

        if (event.isCancelled())
            ci.cancel();
    }

    @Inject(method = "startAttack", at = @At("HEAD"))
    private void doAttack(CallbackInfoReturnable<Boolean> info) {
        NoHitDelayModule noHitDelay = MODULE_MANAGER.getStorage().getByClass(NoHitDelayModule.class);
        if (noHitDelay != null && noHitDelay.isEnabled()) {
            missTime = 0;
        }
    }

    @Inject(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"), cancellable = true)
    private void handleBlockBreaking(boolean bl, CallbackInfo ci) {
        InteractionEvent ev = new InteractionEvent();
        EVENT_MANAGER.post(ev);
        if (ev.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;isDestroying()Z"), cancellable = true)
    private void doItemUse2(CallbackInfo ci) {
        InteractionEvent ev = new InteractionEvent();
        EVENT_MANAGER.post(ev);
        if (ev.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void tick(CallbackInfo ci) {
        if (player == null && level == null)
            return;

        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof LivingEntity e) {
                if (e.isDeadOrDying() && !deadList.contains(e.getId())) {
                    EntityDeathEvent ev = new EntityDeathEvent(e);
                    EVENT_MANAGER.post(ev);
                    deadList.add(e.getId());
                } else if (!e.isDeadOrDying()) {
                    deadList.remove(e.getId());
                }
            }
        }
    }


    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void onHasOutline(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        ESPModule esp = MODULE_MANAGER.getStorage().getByClass(ESPModule.class);
        if (esp != null && esp.isEnabled() && esp.renderMode.get() == ESPModule.RenderMode.OUTLINE) {
            if (ESPModule.getESPColor(entity) != null) {
                cir.setReturnValue(true);
            }
        }
    }

    // Author @cattyngmd
    @ModifyExpressionValue(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
    private boolean handleInputEvents(boolean original) {
        if (MODULE_MANAGER.getStorage().getByClass(AutoEatModule.class).eating.get())
            return false;
        return original;
    }

    @Inject(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z", ordinal = 0, shift = At.Shift.BEFORE))
    private void handleInputEvents3(CallbackInfo info) {
        if (MODULE_MANAGER.getStorage().getByClass(AuraModule.class).isEnabled() && MODULE_MANAGER.getStorage().getByClass(AuraModule.class).multitask() && player != null && player.isUsingItem()) {
            if (!options.keyUse.isDown()) {
                gameMode.releaseUsingItem(player);
            }
        }
    }
}