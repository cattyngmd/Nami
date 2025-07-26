package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.Render2DEvent;
import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import me.kiriyaga.nami.util.ChatAnimationHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@RegisterModule
public class HudModule extends Module {

    public final BoolSetting chatAnimation = addSetting(new BoolSetting("chat animation", true));

    public HudModule() {
        super("hud", "Main HUD module", ModuleCategory.of("client"));
    }

    @SubscribeEvent
    public void onRender2D(Render2DEvent event) {
        Matrix3x2fStack matrices = event.getDrawContext().getMatrices();

        int screenHeight = MC.getWindow().getScaledHeight();
        int chatAnimationOffset = (int) ChatAnimationHelper.getAnimationOffset();

        int chatZoneTop = screenHeight - (screenHeight / 8);

        for (Module module : MODULE_MANAGER.getStorage().getAll()) {
            if (module instanceof HudElementModule hudElement && hudElement.isEnabled()) {
                Text text = hudElement.getDisplayText();
                if (text == null) continue;

                int x = hudElement.getRenderX();
                int y = hudElement.getRenderY();
                int height = MC.textRenderer.fontHeight;

                boolean isInChatZone = (y + height) >= chatZoneTop;

                if (chatAnimation.get() && isInChatZone) {
                    y -= chatAnimationOffset;
                }

                event.getDrawContext().drawText(MC.textRenderer, text, x, y, 0xFFFFFFFF, true);
            }
        }
    }
}
