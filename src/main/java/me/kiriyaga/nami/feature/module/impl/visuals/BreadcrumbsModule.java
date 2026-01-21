package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import me.kiriyaga.nami.util.entity.EntityUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;
import java.util.*;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;
import static me.kiriyaga.nami.util.render.RenderUtil.drawLine;

@RegisterModule
public class BreadcrumbsModule extends Module {

    public final BoolSetting self = addSetting(new BoolSetting("Self", true));
    public final BoolSetting arrows = addSetting(new BoolSetting("Arrows", false));
    public final BoolSetting pearls = addSetting(new BoolSetting("Pearls", true));
    public final DoubleSetting width = addSetting(new DoubleSetting("Width", 1.50, 0.5, 2.5));
    public final IntSetting sampleDelay = addSetting(new IntSetting("Delay", 2, 1, 10));
    public final IntSetting fadeTime = addSetting(new IntSetting("Fade", 60, 10, 200));

    private final List<Breadcrumb> selfCrumbs = new LinkedList<>();
    private final List<Breadcrumb> arrowCrumbs = new LinkedList<>();
    private final List<Breadcrumb> pearlCrumbs = new LinkedList<>();

    private final Map<Integer, Vec3> lastArrowPositions = new HashMap<>();
    private Vec3 lastPlayerPos;
    private int tickCounter;
    private final Map<Integer, Vec3> lastPearlPositions = new HashMap<>();

    public BreadcrumbsModule() {
        super("Breadcrumbs", "Shows movement trails.", ModuleCategory.of("Render"));
    }

    @Override
    public void onEnable() {
        selfCrumbs.clear();
        arrowCrumbs.clear();
        pearlCrumbs.clear();
        lastArrowPositions.clear();
        lastPearlPositions.clear();
        lastPlayerPos = null;
        tickCounter = 0;
    }

    @SubscribeEvent
    public void onRender3D(Render3DEvent event) {
        if (MC.level == null)
            return;

        Color base = MODULE_MANAGER.getStorage().getByClass(ColorModule.class).getStyledGlobalColor();
        renderList(selfCrumbs, base);
        renderList(arrowCrumbs, base);
        renderList(pearlCrumbs, base);

        tickCounter++;

        fadeList(selfCrumbs);
        fadeList(arrowCrumbs);
        fadeList(pearlCrumbs);

        if (tickCounter % sampleDelay.get() != 0)
            return;

        if (self.get() && MC.player != null) {
            Vec3 pos = MC.player.position();
            if (lastPlayerPos != null)
                selfCrumbs.add(new Breadcrumb(lastPlayerPos, pos, fadeTime.get()));
            lastPlayerPos = pos;
        }

        for (Entity entity : EntityUtils.getEntities(EntityUtils.EntityTypeCategory.ALL)) {

            if (arrows.get() && entity instanceof Arrow arrow) {
                int id = arrow.getId();
                Vec3 pos = arrow.position();
                Vec3 last = lastArrowPositions.get(id);
                if (last != null)
                    arrowCrumbs.add(new Breadcrumb(last, pos, fadeTime.get()));
                lastArrowPositions.put(id, pos);
            }

            if (pearls.get() && entity instanceof ThrownEnderpearl pearl) {
                int id = pearl.getId();
                Vec3 pos = pearl.position();
                Vec3 last = lastPearlPositions.get(id);
                if (last != null)
                    pearlCrumbs.add(new Breadcrumb(last, pos, fadeTime.get()));
                lastPearlPositions.put(id, pos);
            }
        }
    }

    private void fadeList(List<Breadcrumb> list) {
        Iterator<Breadcrumb> it = list.iterator();
        while (it.hasNext()) {
            Breadcrumb crumb = it.next();
            crumb.age++;
            if (crumb.age >= crumb.lifeTime)
                it.remove();
        }
    }

    private void renderList(List<Breadcrumb> list, Color base) {
        for (Breadcrumb crumb : list) {
            Color color = new Color(base.getRed(), base.getGreen(), base.getBlue(), 120);
            drawLine(crumb.from, crumb.to, color, width.get().floatValue());
        }
    }

    private static class Breadcrumb {
        final Vec3 from;
        final Vec3 to;
        final int lifeTime;
        int age;
        Breadcrumb(Vec3 from, Vec3 to, int lifeTime) {
            this.from = from;
            this.to = to;
            this.lifeTime = lifeTime;
            this.age = 0;
        }
    }
}
