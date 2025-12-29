package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.feature.module.impl.miscellaneous.AutoReconnectModule;
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

import static me.kiriyaga.nami.Nami.LAST_CONNECTION;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

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
        AutoReconnectModule module = MODULE_MANAGER.getStorage().getByClass(AutoReconnectModule.class);
        if (module == null) return;

        if (LAST_CONNECTION != null || !module.hardHide.get()) {
            reconnectButton = Button.builder(Component.literal(getReconnectText(module)), button -> tryReconnect())
                    .width(200)
                    .build();

            toggleButton = Button.builder(Component.literal(getToggleText(module)), button -> {
                module.toggle();
                toggleButton.setMessage(Component.literal(getToggleText(module)));
                reconnectButton.setMessage(Component.literal(getReconnectText(module)));
                time = module.delay.get() * 20;
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
        AutoReconnectModule module = MODULE_MANAGER.getStorage().getByClass(AutoReconnectModule.class);
        if (module == null) return;

        if (!module.isEnabled() || LAST_CONNECTION == null || module.hardHide.get()) return;

        if (time <= 0) {
            tryReconnect();
        } else {
            time--;
            if (reconnectButton != null) {
                reconnectButton.setMessage(Component.literal(getReconnectText(module)));
            }
        }
    }

    @Unique
    private String getReconnectText(AutoReconnectModule module) {
        String text = "Reconnect";
        if (module != null && module.isEnabled()) {
            text += " " + String.format("(" + ChatFormatting.WHITE + "%.1fs" + ChatFormatting.RESET + ")", time / 20.0);
        }
        return text;
    }

    @Unique
    private String getToggleText(AutoReconnectModule module) {
        if (module == null) return ChatFormatting.RED + "AutoReconnect";
        return (module.isEnabled() ? ChatFormatting.WHITE : ChatFormatting.RED) + "AutoReconnect";
    }

    @Unique
    private void tryReconnect() {
        if (LAST_CONNECTION == null) return;
        ConnectScreen.startConnecting(
                new TitleScreen(),
                Minecraft.getInstance(),
                LAST_CONNECTION.getA(),
                LAST_CONNECTION.getB(),
                false,
                null
        );
    }
}
