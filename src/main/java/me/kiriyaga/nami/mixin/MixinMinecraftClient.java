package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.event.impl.EntityDeathEvent;
import me.kiriyaga.nami.event.impl.InteractionEvent;
import me.kiriyaga.nami.event.impl.OpenScreenEvent;
import me.kiriyaga.nami.feature.module.impl.misc.AutoRespawnModule;
import me.kiriyaga.nami.feature.module.impl.world.AirPlaceModule;
import me.kiriyaga.nami.feature.module.impl.world.FastPlaceModule;
import me.kiriyaga.nami.feature.module.impl.world.NoHitDelayModule;
import me.kiriyaga.nami.setting.impl.KeyBindSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import me.kiriyaga.nami.feature.module.Module;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static me.kiriyaga.nami.Nami.*;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {
    @Shadow
    public int attackCooldown;
    @Shadow private int itemUseCooldown;

    private int holdTicks = 0;
    @Shadow public ClientPlayerEntity player;
    @Shadow public ClientWorld world;
    private final Set<Integer> deadList = new HashSet<>();

    @Inject(method = "handleInputEvents", at = @At("TAIL"))
    private void onHandleInputEvents_TAIL(CallbackInfo ci) {
        if (MC == null || MC.mouse == null || MC.currentScreen != null) return;

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
    }

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isRiding()Z", ordinal = 0, shift = At.Shift.BEFORE))
    private void doItemUse(CallbackInfo info) {

        AirPlaceModule airPlace = MODULE_MANAGER.getStorage().getByClass(AirPlaceModule.class);
        FastPlaceModule fastPlace = MODULE_MANAGER.getStorage().getByClass(FastPlaceModule.class);

        if (airPlace == null || fastPlace == null) return;

        if (airPlace.isEnabled() && airPlace.cooldown <= 0) {
            itemUseCooldown = airPlace.delay.get();
            return;
        }

        if (!fastPlace.isEnabled()) return;

        if (MC == null || MC.player == null) return;

        ItemStack heldStack = MC.player.getMainHandStack();
        if (heldStack == null) return;

        Item heldItem = heldStack.getItem();
        if (heldItem == null) return;

        Identifier heldId = Registries.ITEM.getId(heldItem);
        if (heldId == null) return;

        if (fastPlace.whitelist.get() && !fastPlace.whitelist.isWhitelisted(heldId)) return;

        if (fastPlace.blacklist.get() && fastPlace.blacklist.isWhitelisted(heldId)) return;

        if (holdTicks >= fastPlace.startDelay.get()) {
            itemUseCooldown = fastPlace.delay.get();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo info) {
        FastPlaceModule fastPlace = MODULE_MANAGER.getStorage().getByClass(FastPlaceModule.class);

        if (fastPlace == null) return;

        if (fastPlace.isEnabled() && MC != null && MC.options != null && MC.options.useKey.isPressed()) {
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

    @Inject(method = "doAttack", at = @At("HEAD"))
    private void doAttack(CallbackInfoReturnable<Boolean> info) {
        NoHitDelayModule noHitDelay = MODULE_MANAGER.getStorage().getByClass(NoHitDelayModule.class);
        if (noHitDelay != null && noHitDelay.isEnabled()) {
            attackCooldown = 0;
        }
    }

    @Redirect(method = "handleBlockBreaking", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean handleBlockBreaking(ClientPlayerEntity instance) {
        InteractionEvent ev = new InteractionEvent();
        EVENT_MANAGER.post(ev);
        return !ev.isCancelled() && instance.isUsingItem();
    }

    @Redirect(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;isBreakingBlock()Z"))
    private boolean doItemUse(ClientPlayerInteractionManager instance) {
        InteractionEvent ev = new InteractionEvent();
        EVENT_MANAGER.post(ev);
        return !ev.isCancelled() && instance.isBreakingBlock();
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void tick(CallbackInfo ci) {
        if (player == null && world == null)
            return;

        for (Entity entity : world.getEntities()) {
            if (entity instanceof LivingEntity e) {
                if (e.isDead() && !deadList.contains(e.getId())) {
                    EntityDeathEvent ev = new EntityDeathEvent(e);
                    EVENT_MANAGER.post(ev);
                    deadList.add(e.getId());
                } else if (!e.isDead()) {
                    deadList.remove(e.getId());
                }
            }
        }
    }
}