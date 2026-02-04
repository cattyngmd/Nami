package namidevelopment.kiriyaga.nami.mixin;

import namidevelopment.kiriyaga.nami.impl.feature.miscellaneous.AutoReconnectFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static namidevelopment.kiriyaga.nami.Nami.LAST_CONNECTION;
import static namidevelopment.kiriyaga.api.NamiApi.FEATURE_SERVICE;
import static namidevelopment.kiriyaga.nami.Nami.MC;

@Mixin(DisconnectedScreen.class)
public abstract class MixinDisconnectedScreen extends Screen {

    @Unique private Button reconnectButton;
    @Unique private Button toggleButton;
    @Unique private double time = 100.0;

    protected MixinDisconnectedScreen(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        AutoReconnectFeature Feature = FEATURE_SERVICE.getStorage().getByClass(AutoReconnectFeature.class);
        if (Feature == null) return;

        if (LAST_CONNECTION != null || !Feature.hardHide.get()) {
            reconnectButton = Button.builder(Component.literal(getReconnectText(Feature)), button -> tryReconnect())
                    .width(200)
                    .build();

            toggleButton = Button.builder(Component.literal(getToggleText(Feature)), button -> {
                Feature.toggle();
                toggleButton.setMessage(Component.literal(getToggleText(Feature)));
                reconnectButton.setMessage(Component.literal(getReconnectText(Feature)));
                time = Feature.delay.get() * 20;
            }).width(200).build();

            int centerX = this.width / 2;
            int y = this.height / 2 + 40;

            reconnectButton.setPosition(centerX - 100, y);
            toggleButton.setPosition(centerX - 100, y + 25);

            this.addRenderableWidget(reconnectButton);
            this.addRenderableWidget(toggleButton);
        }
    }

    @Override
    public void tick() {
        AutoReconnectFeature Feature = FEATURE_SERVICE.getStorage().getByClass(AutoReconnectFeature.class);
        if (Feature == null) return;

        if (!Feature.isEnabled() || LAST_CONNECTION == null || Feature.hardHide.get()) return;

        if (time <= 0) {
            tryReconnect();
        } else {
            time--;
            if (reconnectButton != null) {
                reconnectButton.setMessage(Component.literal(getReconnectText(Feature)));
            }
        }
    }

    @Unique
    private String getReconnectText(AutoReconnectFeature Feature) {
        String text = "Reconnect";
        if (Feature != null && Feature.isEnabled()) {
            text += " " + String.format("(" + ChatFormatting.WHITE + "%.1fs" + ChatFormatting.RESET + ")", time / 20.0);
        }
        return text;
    }

    @Unique
    private String getToggleText(AutoReconnectFeature Feature) {
        if (Feature == null) return ChatFormatting.RED + "AutoReconnect";
        return (Feature.isEnabled() ? ChatFormatting.WHITE : ChatFormatting.RED) + "AutoReconnect";
    }

    @Unique
    private void tryReconnect() {
        if (LAST_CONNECTION == null) return;
        ConnectScreen.startConnecting(new TitleScreen(), MC, LAST_CONNECTION.getA(), LAST_CONNECTION.getB(), false, null);
    }
}
