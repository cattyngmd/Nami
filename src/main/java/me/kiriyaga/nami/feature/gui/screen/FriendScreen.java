package me.kiriyaga.nami.feature.gui.screen;

import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.feature.gui.base.ConsolePanelRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.stream.Collectors;

import static me.kiriyaga.nami.Nami.*;

public class FriendScreen extends Screen {
    private ConsolePanelRenderer console;

    public FriendScreen() {
        super(Text.literal("NamiFriends"));
    }

    private ClickGuiModule getClickGuiModule() {
        return MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class);
    }

    @Override
    protected void init() {
        super.init();

        int panelWidth = 300;
        int panelHeight = 200;
        int panelX = 20;
        int panelY = 20;

        console = new ConsolePanelRenderer(
                panelX,
                panelY,
                panelWidth,
                panelHeight,
                name -> {
                    if (!name.isEmpty()) {
                        FRIEND_MANAGER.addFriend(name);
                        updateEntries();
                    }
                },
                name -> {
                    FRIEND_MANAGER.removeFriend(name);
                    updateEntries();
                },
                name -> {}
        );

        updateEntries();
    }

    private void updateEntries() {
        List<String> friends = FRIEND_MANAGER.getFriends().stream()
                .map(f -> {
                    boolean online = isOnline(f);
                    String status = online ? "Online" : "Offline";
                    int color = online ? 0x00FF00 : 0xFF0000;
                    return f + " [" + status + "]";
                })
                .collect(Collectors.toList());
        console.setEntries(friends);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ClickGuiModule clickGui = getClickGuiModule();
        if (clickGui != null && clickGui.background.get()) {
            int alpha = (clickGui.backgroundAlpha.get() & 0xFF) << 24;
            int color = alpha | 0x101010;
            context.fill(0, 0, this.width, this.height, color);
        }

        console.render(context, this.textRenderer, mouseX, mouseY);

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(CLICK_GUI.scale, CLICK_GUI.scale);
        int scaledMouseX = (int) (mouseX / CLICK_GUI.scale);
        int scaledMouseY = (int) (mouseY / CLICK_GUI.scale);

        int panelWidth = NAVIGATE_PANEL.calcWidth();
        int navX = (this.width - panelWidth) / 2;
        int navY = 1;
        NAVIGATE_PANEL.render(context, this.textRenderer, navX, navY, scaledMouseX, scaledMouseY);

        context.getMatrices().popMatrix();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (console.mouseClicked(mouseX, mouseY, button)) return true;

        int scaledMouseX = (int) (mouseX / CLICK_GUI.scale);
        int scaledMouseY = (int) (mouseY / CLICK_GUI.scale);
        int navX = (int) ((this.width / CLICK_GUI.scale - NAVIGATE_PANEL.calcWidth()) / 2);
        int navY = 1;
        NAVIGATE_PANEL.mouseClicked(scaledMouseX, scaledMouseY, navX, navY, this.textRenderer);

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (console.keyPressed(keyCode, scanCode, modifiers)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (console.charTyped(chr, modifiers)) return true;
        return super.charTyped(chr, modifiers);
    }

    private boolean isOnline(String name) {
        if (MC.world == null) return false;
        for (AbstractClientPlayerEntity player : MC.world.getPlayers()) {
            if (player.getGameProfile().getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void renderBackground(DrawContext context, int i, int j, float f) {
        if (MC.world != null && MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class).blur.get()) {
            this.applyBlur(context);
        }
    }
}