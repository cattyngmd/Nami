package me.kiriyaga.nami.feature.gui.screen;

import me.kiriyaga.nami.core.FriendManager;
import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import static me.kiriyaga.nami.Nami.*;

public class FriendScreen extends Screen {
    private final List<ButtonWidget> removeButtons = new ArrayList<>();
    private TextFieldWidget addFriendField;
    private ButtonWidget addFriendButton;

    public FriendScreen() {
        super(Text.literal("NamiFriends"));
    }

    private ClickGuiModule getClickGuiModule() {
        return MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class);
    }

    @Override
    protected void init() {
        super.init();

        this.clearChildren();
        this.removeButtons.clear();

        int y = 40;
        int index = 0;
        for (String friend : FRIEND_MANAGER.getFriends()) {
            final String friendName = friend;

            ButtonWidget removeBtn = ButtonWidget.builder(Text.literal("Remove"), btn -> {
                FRIEND_MANAGER.removeFriend(friendName);
                this.init();
            }).dimensions(this.width - 90, y - 4, 70, 16).build();

            this.addDrawableChild(removeBtn);
            removeButtons.add(removeBtn);

            y += 20;
            index++;
        }

        addFriendField = new TextFieldWidget(this.textRenderer,
                this.width / 2 - 100, this.height - 30, 150, 20,
                Text.literal("Enter friend name"));
        this.addDrawableChild(addFriendField);

        addFriendButton = ButtonWidget.builder(Text.literal("Add"), btn -> {
            String input = addFriendField.getText().trim();
            if (!input.isEmpty()) {
                FRIEND_MANAGER.addFriend(input);
                addFriendField.setText("");
                this.init();// fucking shitcode but yeah
            }
        }).dimensions(this.width / 2 + 60, this.height - 30, 40, 20).build();
        this.addDrawableChild(addFriendButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        ClickGuiModule hudEditorModule = getClickGuiModule();
        if (hudEditorModule != null && hudEditorModule.background.get()) {
            int alpha = (hudEditorModule.backgroundAlpha.get() & 0xFF) << 24;
            int color = alpha | 0x101010;
            context.fill(0, 0, this.width, this.height, color);
        }

        int y = 40;
        for (String friend : FRIEND_MANAGER.getFriends()) {
            boolean online = isOnline(friend);

            int color = online ? 0x00FF00 : 0xFF0000;
            String status = online ? "Online" : "Offline";

            context.drawTextWithShadow(this.textRenderer,
                    friend, 20, y, 0xFFFFFF);

            context.drawTextWithShadow(this.textRenderer,
                    status, 140, y, color);

            y += 20;
        }

        super.render(context, mouseX, mouseY, delta);
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
        if (MC.world != null && MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class).blur.get())
            this.applyBlur(context);
    }
}
