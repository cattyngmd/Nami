package me.kiriyaga.essentials.feature.module.impl.movement;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.PreTickEvent;
import me.kiriyaga.essentials.event.impl.Render3DEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.mixin.KeyBindingAccessor;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.setting.impl.EnumSetting;
import me.kiriyaga.essentials.util.render.RenderUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static me.kiriyaga.essentials.Essentials.MINECRAFT;

public class AutoWalkModule extends Module {

    public AutoWalkModule() {
        super("auto walk", "Automatically makes you walk.", Category.movement);
    }

    @Override
    public void onDisable() {
        setWalkHeld(false);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onUpdateEvent(PreTickEvent event) {
        setWalkHeld(true);
    }

    private void setWalkHeld(boolean held) {
        KeyBinding walkKey = MINECRAFT.options.forwardKey;
        InputUtil.Key boundKey = ((KeyBindingAccessor) walkKey).getBoundKey();
        int keyCode = boundKey.getCode();
        boolean physicallyPressed = InputUtil.isKeyPressed(MINECRAFT.getWindow().getHandle(), keyCode);
        walkKey.setPressed(physicallyPressed || held);
    }
}
