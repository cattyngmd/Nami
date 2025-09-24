package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.ParticleEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import net.minecraft.particle.ParticleTypes;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class NoRenderModule extends Module {



    public final BoolSetting noTilt = addSetting(new BoolSetting("NoTilt", true));
    public final BoolSetting noBob = addSetting(new BoolSetting("NoBob", true));
    public final IntSetting tileEntity = addSetting(new IntSetting("TileEntity", 15, 0, 75));
    public final BoolSetting portalGui = addSetting(new BoolSetting("PortalGui", true));
    public final BoolSetting noFire = addSetting(new BoolSetting("NoFire", true));
    public final BoolSetting noBackground = addSetting(new BoolSetting("NoBackground", true));
    public final BoolSetting noTotemParticle = addSetting(new BoolSetting("NoPopParticle", false));
    public final BoolSetting noPotionParticle = addSetting(new BoolSetting("NoPotParticle", false));
    public final BoolSetting noFirework = addSetting(new BoolSetting("NoFirework", false));
    public final BoolSetting noWaterParticle = addSetting(new BoolSetting("NoWaterParticle", true));
    public final BoolSetting noExplosion = addSetting(new BoolSetting("NoExplosion", true));
    public final BoolSetting noBlockBreak = addSetting(new BoolSetting("NoBreakParticle", false));
    public final BoolSetting noLiguid = addSetting(new BoolSetting("NoLiquid", false));
    public final BoolSetting noWall = addSetting(new BoolSetting("NoWall", false));
    public final BoolSetting noVignette = addSetting(new BoolSetting("NoVignette", true));
    public final BoolSetting noTotem = addSetting(new BoolSetting("NoTotem", true));
    public final BoolSetting noBossBar = addSetting(new BoolSetting("NoBoss", true));
    public final BoolSetting noPortal = addSetting(new BoolSetting("NoPortalGui", true));
    public final BoolSetting noPotIcon = addSetting(new BoolSetting("NoPotIcon", true));
    public final BoolSetting noFog = addSetting(new BoolSetting("NoFog", true));
    public final BoolSetting noArmor = addSetting(new BoolSetting("NoArmor", true));
    public final BoolSetting noNausea = addSetting(new BoolSetting("NoNausea", true));
    public final BoolSetting noPumpkin = addSetting(new BoolSetting("NoPumpkin", false));
    public final BoolSetting noPowderedSnow = addSetting(new BoolSetting("NoPowdered", false));

    public NoRenderModule() {
        super("NoRender", "Prevent rendering certain overlays/effects.", ModuleCategory.of("Visuals"), "norender");
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

        if (noPotionParticle.get() && ev.getParticle().getType() == ParticleTypes.ENTITY_EFFECT)
            ev.cancel();

        if (noFirework.get() && ev.getParticle().getType() == ParticleTypes.FIREWORK)
            ev.cancel();

        if (noWaterParticle.get() && (ev.getParticle().getType() == ParticleTypes.RAIN || ev.getParticle().getType() == ParticleTypes.DRIPPING_DRIPSTONE_WATER || ev.getParticle().getType() == ParticleTypes.DRIPPING_WATER || ev.getParticle().getType() == ParticleTypes.FALLING_DRIPSTONE_WATER || ev.getParticle().getType() == ParticleTypes.FALLING_WATER))
            ev.cancel();
    }
}
