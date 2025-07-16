package me.kiriyaga.nami.feature.module.impl.render;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.Render2DEvent;
import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.entity.Entity;
import org.joml.Matrix3x2fStack;

import java.awt.*;

import static me.kiriyaga.nami.Nami.*;

public class TracersModule extends Module {

    public final BoolSetting showPlayers = addSetting(new BoolSetting("players", true));
    public final BoolSetting showPeacefuls = addSetting(new BoolSetting("peacefuls", false));
    public final BoolSetting showNeutrals = addSetting(new BoolSetting("neutrals", false));
    public final BoolSetting showHostiles = addSetting(new BoolSetting("hostiles", false));
    public final BoolSetting showItems = addSetting(new BoolSetting("items", false));
    public final DoubleSetting thickness = addSetting(new DoubleSetting("thickness", 1.0, 0.5, 3.0));

    // Цвета из ESP-модуля
    private static final Color COLOR_PASSIVE = new Color(211, 211, 211, 255);
    private static final Color COLOR_NEUTRAL = new Color(255, 255, 0, 255);
    private static final Color COLOR_HOSTILE = new Color(255, 0, 0, 255);
    private static final Color COLOR_ITEM = new Color(211, 211, 211, 255);

    public TracersModule() {
        super("tracers", "Draws lines from the center of the screen to entities.", Category.visuals, "екфсукы");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender2D(Render2DEvent event) {
        if (MINECRAFT == null || MINECRAFT.player == null || MINECRAFT.world == null || MINECRAFT.options.hudHidden) return;

        ColorModule colorModule = MODULE_MANAGER.getModule(ColorModule.class);

        float thickness = this.thickness.get().floatValue();

        if (showPlayers.get()) {
            for (Entity player : ENTITY_MANAGER.getOtherPlayers()) {
                if (player.isRemoved()) continue;

                Color color = FRIEND_MANAGER.isFriend(player.getName().getString())
                        ? colorModule.getStyledGlobalColor()
                        : COLOR_HOSTILE;

                // RenderUtil.drawTracer(event.getDrawContext(), player.getPos().add(0, player.getHeight() / 2.0, 0), color, thickness);
            }
        }

        if (showPeacefuls.get()) {
            for (Entity entity : ENTITY_MANAGER.getPassive()) {
                if (!entity.isAlive()) continue;
                //RenderUtil.drawTracer(matrices, entity.getPos().add(0, entity.getHeight() / 2.0, 0), COLOR_PASSIVE, thickness);
            }
        }

        if (showNeutrals.get()) {
            for (Entity entity : ENTITY_MANAGER.getNeutral()) {
                if (!entity.isAlive()) continue;
              //  RenderUtil.drawTracer(matrices, entity.getPos().add(0, entity.getHeight() / 2.0, 0), COLOR_NEUTRAL, thickness);
            }
        }

        if (showHostiles.get()) {
            for (Entity entity : ENTITY_MANAGER.getHostile()) {
                if (!entity.isAlive()) continue;
               // RenderUtil.drawTracer(matrices, entity.getPos().add(0, entity.getHeight() / 2.0, 0), COLOR_HOSTILE, thickness);
            }
        }

        if (showItems.get()) {
            for (Entity entity : ENTITY_MANAGER.getDroppedItems()) {
                if (entity.isRemoved()) continue;
               // RenderUtil.drawTracer(matrices, entity.getPos().add(0, entity.getHeight() / 2.0, 0), COLOR_ITEM, thickness);
            }
        }
    }
}
