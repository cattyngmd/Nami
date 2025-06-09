package me.kiriyaga.essentials.feature.module.impl.render;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.PacketSendEvent;
import me.kiriyaga.essentials.event.impl.UpdateEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.mixin.PlayerInteractEntityC2SPacketAccessor;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.setting.impl.EnumSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

import java.util.concurrent.Executors;

import static me.kiriyaga.essentials.Essentials.CHAT_MANAGER;
import static me.kiriyaga.essentials.Essentials.MINECRAFT;

public class NoRenderModule extends Module {



    private final BoolSetting noFire = addSetting(new BoolSetting("No Fire", true));
    private final BoolSetting noLiguid = addSetting(new BoolSetting("No Liquid", false));
    private final BoolSetting noWall = addSetting(new BoolSetting("No Wall", false));
    private final BoolSetting noVignette = addSetting(new BoolSetting("No Vignette", true));
    private final BoolSetting noTotem = addSetting(new BoolSetting("No Totem", true));
    private final BoolSetting noEating = addSetting(new BoolSetting("No Eating", true));
    private final BoolSetting noBossBar = addSetting(new BoolSetting("No Boss Bar", true));
    private final BoolSetting noPortal = addSetting(new BoolSetting("No Portal", true));
    private final BoolSetting noPotIcon = addSetting(new BoolSetting("No Pot Icon", true));
    private final BoolSetting noFog = addSetting(new BoolSetting("No Fog", true));
    private final BoolSetting noArmor = addSetting(new BoolSetting("No Armor", true));
    private final BoolSetting noNausea = addSetting(new BoolSetting("No Nausea", true));
    private final BoolSetting noPumpkin = addSetting(new BoolSetting("No Pumpkin", false));
    private final BoolSetting noPowderedSnow = addSetting(new BoolSetting("No Powdered Snow", false));

    public NoRenderModule() {
        super("No Render", "Prevent rendering certain overlays/effects", Category.RENDER);
    }

    @Override
    public void onEnable() {
        MINECRAFT.worldRenderer.reload();
    }
    @Override
    public void onDisable() {
        MINECRAFT.worldRenderer.reload();
    }

    public boolean isNoFire() {
        return noFire.get();
    }

    public boolean isNoLiguid() {
        return noLiguid.get();
    }

    public boolean isNoWall() {
        return noWall.get();
    }

    public boolean isNoVignette() {
        return noVignette.get();
    }

    public boolean isNoTotem() {
        return noTotem.get();
    }

    public boolean isNoEating() {
        return noEating.get();
    }

    public boolean isNoBossBar() {
        return noBossBar.get();
    }

    public boolean isNoPortal() {
        return noPortal.get();
    }

    public boolean isNoPotIcon() {
        return noPotIcon.get();
    }

    public boolean isNoFog() {
        return noFog.get();
    }

    public boolean isNoArmor() {
        return noArmor.get();
    }

    public boolean isNoNausea() {
        return noNausea.get();
    }

    public boolean isNoPumpkin() {
        return noPumpkin.get();
    }

    public boolean isNoPowderedSnow() {
        return noPowderedSnow.get();
    }

}
