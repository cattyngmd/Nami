package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.ParticleEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import net.minecraft.client.particle.ExplosionEmitterParticle;
import net.minecraft.client.particle.ExplosionLargeParticle;
import net.minecraft.client.particle.ExplosionSmokeParticle;
import net.minecraft.client.particle.TotemParticle;
import net.minecraft.particle.ParticleTypes;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class NoRenderModule extends Module {



    public final BoolSetting noFire = addSetting(new BoolSetting("fire", true));
    public final BoolSetting noBackground = addSetting(new BoolSetting("background", true));
    public final BoolSetting noTotemParticle = addSetting(new BoolSetting("totem particle", false));
    public final BoolSetting noFirework = addSetting(new BoolSetting("firework", false));
    public final BoolSetting noWaterParticle = addSetting(new BoolSetting("water particle", true));
    public final BoolSetting noExplosion = addSetting(new BoolSetting("explosion particle", true));
    public final BoolSetting noBlockBreak = addSetting(new BoolSetting("block break", false));
    public final BoolSetting noLiguid = addSetting(new BoolSetting("liquid", false));
    public final BoolSetting noWall = addSetting(new BoolSetting("wall", false));
    public final BoolSetting noVignette = addSetting(new BoolSetting("vignette", true));
    public final BoolSetting noTotem = addSetting(new BoolSetting("totem", true));
    public final BoolSetting noBossBar = addSetting(new BoolSetting("boss", true));
    public final BoolSetting noPortal = addSetting(new BoolSetting("portal", true));
    public final BoolSetting noPotIcon = addSetting(new BoolSetting("pot", true));
    public final BoolSetting noFog = addSetting(new BoolSetting("fog", true));
    public final BoolSetting noArmor = addSetting(new BoolSetting("armor", true));
    public final BoolSetting noNausea = addSetting(new BoolSetting("nausea", true));
    public final BoolSetting noPumpkin = addSetting(new BoolSetting("pumpkin", false));
    public final BoolSetting noPowderedSnow = addSetting(new BoolSetting("powdered snow", false));

    public NoRenderModule() {
        super("no render", "Prevent rendering certain overlays/effects.", ModuleCategory.of("visuals"), "norender");
        noFire.setOnChanged(this::reloadRenderer);
        noBackground.setOnChanged(this::reloadRenderer);
        noLiguid.setOnChanged(this::reloadRenderer);
        noVignette.setOnChanged(this::reloadRenderer);
        noPortal.setOnChanged(this::reloadRenderer);
        noFog.setOnChanged(this::reloadRenderer);
        noPumpkin.setOnChanged(this::reloadRenderer);
        noPowderedSnow.setOnChanged(this::reloadRenderer);
    }

    private void reloadRenderer() {
        if (MC.world != null) {
            MC.worldRenderer.reload();
        }
    }

    @Override
    public void onEnable() {
        reloadRenderer();
    }

    @Override
    public void onDisable() {
        reloadRenderer();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onParticle(ParticleEvent ev){
        if (MC.world == null || MC.player == null)
            return;

//        if (noExplosion.get() && (ev.getParticle() instanceof ExplosionEmitterParticle || ev.getParticle() instanceof ExplosionLargeParticle || ev.getParticle() instanceof ExplosionSmokeParticle))
//            ev.cancel();

//        if (noTotemParticle.get() && ev.getParticle() instanceof TotemParticle)
//            ev.cancel();

        if (noExplosion.get() && (ev.getParticle().getType() == ParticleTypes.EXPLOSION || ev.getParticle().getType() == ParticleTypes.EXPLOSION_EMITTER))
            ev.cancel();

        if (noTotemParticle.get() && ev.getParticle().getType() == ParticleTypes.TOTEM_OF_UNDYING)
            ev.cancel();

        if (noFirework.get() && ev.getParticle().getType() == ParticleTypes.FIREWORK)
            ev.cancel();

        if (noWaterParticle.get() && (ev.getParticle().getType() == ParticleTypes.RAIN || ev.getParticle().getType() == ParticleTypes.DRIPPING_DRIPSTONE_WATER || ev.getParticle().getType() == ParticleTypes.DRIPPING_WATER || ev.getParticle().getType() == ParticleTypes.FALLING_DRIPSTONE_WATER || ev.getParticle().getType() == ParticleTypes.FALLING_WATER))
            ev.cancel();
    }
}
