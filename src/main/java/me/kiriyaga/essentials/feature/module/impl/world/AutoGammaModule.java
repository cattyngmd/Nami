package me.kiriyaga.essentials.feature.module.impl.world;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.PacketReceiveEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.mixininterface.ISimpleOption;
import me.kiriyaga.essentials.setting.impl.DoubleSetting;
import me.kiriyaga.essentials.setting.impl.IntSetting;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;

import static me.kiriyaga.essentials.Essentials.CHAT_MANAGER;
import static me.kiriyaga.essentials.Essentials.MINECRAFT;

public class AutoGammaModule extends Module {

    public final DoubleSetting gamma = addSetting(new DoubleSetting("Gamma", 2, 1, 8));

    public AutoGammaModule() {
        super("Auto Gamma", "Sets up gamma on join.", Category.WORLD, "autogamma", "gamma", "autogmam", "фгещпфььф");
    }

    @Override
    public void onEnable(){
        ((ISimpleOption) (Object) MINECRAFT.options.getGamma()).setValue(gamma.get());
    }
}
