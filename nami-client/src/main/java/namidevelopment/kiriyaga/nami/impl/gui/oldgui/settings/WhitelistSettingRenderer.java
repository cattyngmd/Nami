package namidevelopment.kiriyaga.nami.impl.gui.oldgui.settings;

import namidevelopment.kiriyaga.nami.Nami;
import namidevelopment.kiriyaga.api.client.ColorFeature;
import namidevelopment.kiriyaga.api.model.setting.WhitelistSetting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;

public class WhitelistSettingRenderer implements SettingRenderer<WhitelistSetting> {
    public final BoolSettingRenderer boolRenderer = new BoolSettingRenderer();

    @Override
    public void render(GuiGraphics context, Font textRenderer, WhitelistSetting setting, int x, int y, int mouseX, int mouseY) {
        boolRenderer.render(context, textRenderer, setting, x, y, mouseX, mouseY);

        //TODO: item identifier list extension
    }

    @Override
    public boolean mouseClicked(WhitelistSetting setting, double mouseX, double mouseY, int button) {
        return boolRenderer.mouseClicked(setting, mouseX, mouseY, button);
    }
}
