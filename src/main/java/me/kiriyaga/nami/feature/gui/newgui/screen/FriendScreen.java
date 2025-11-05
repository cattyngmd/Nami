package me.kiriyaga.nami.feature.gui.newgui.screen;

import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.feature.gui.newgui.component.ConsolePanelComponent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;
import java.util.stream.Collectors;

import static me.kiriyaga.nami.Nami.*;

public class FriendScreen extends Screen {
    private ConsolePanelComponent console;

    public FriendScreen() {
        super(Text.literal("NamiFriends"));
    }

    @Override
    protected void init() {
        super.init();
        if (console == null) {
            console = new ConsolePanelComponent("Friends",20, 20, 300, 200,
                    name -> { FRIEND_MANAGER.addFriend(name); updateEntries(); },
                    name -> { FRIEND_MANAGER.removeFriend(name); updateEntries(); },
                    name -> {});
        }
        updateEntries();
    }

    private void updateEntries() {
        List<Text> friends = FRIEND_MANAGER.getFriends().stream()
                .map(f -> CAT_FORMAT.format(f + " [" + (isOnline(f) ? "{green}Online" : "{red}Offline") + "{reset}]"))
                .collect(Collectors.toList());
        console.setEntries(friends);
    }

    private boolean isOnline(String name) {
        if (MC.world == null) return false;
        return MC.world.getPlayers().stream()
                .map(p -> p.getGameProfile().getName())
                .anyMatch(n -> n.equalsIgnoreCase(name));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ClickGuiModule clickGui = MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class);
        updateEntries();

        NAVIGATE_PANEL.render(context, this.textRenderer, mouseX, mouseY);

        if (clickGui != null && clickGui.background.get()) {
            int alpha = (clickGui.backgroundAlpha.get() & 0xFF) << 24;
            int color = alpha | 0x101010;
            context.fill(0, 0, width, height, color);
        }

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(CLICK_GUI.scale, CLICK_GUI.scale);

        console.render(context, textRenderer, (int)(mouseX / CLICK_GUI.scale), (int)(mouseY / CLICK_GUI.scale));
        context.getMatrices().popMatrix();
    }

    @Override public boolean mouseClicked(double x, double y, int button) {
        NAVIGATE_PANEL.mouseClicked(x, y, this.textRenderer);

        return console.mouseClicked(x / CLICK_GUI.scale, y / CLICK_GUI.scale, button) || super.mouseClicked(x, y, button);
    }

    @Override public boolean mouseScrolled(double x, double y, double h, double v) {
        return console.mouseScrolled(x / CLICK_GUI.scale, y / CLICK_GUI.scale, v) || super.mouseScrolled(x, y, h, v);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return console.mouseDragged(mouseX / CLICK_GUI.scale, mouseY / CLICK_GUI.scale, deltaX, deltaY)
                || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        console.mouseReleased(mouseX / CLICK_GUI.scale, mouseY / CLICK_GUI.scale, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override public boolean keyPressed(int k, int s, int m) {
        return console.keyPressed(k, s, m) || super.keyPressed(k, s, m);
    }

    @Override public boolean charTyped(char c, int m) {
        return console.charTyped(c, m) || super.charTyped(c, m);
    }

    @Override public boolean shouldPause() { return false; }
}
