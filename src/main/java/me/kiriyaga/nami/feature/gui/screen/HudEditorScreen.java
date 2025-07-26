package me.kiriyaga.nami.feature.gui.screen;

import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;
import me.kiriyaga.nami.util.ChatAnimationHelper;

public class HudEditorScreen extends Screen {
    private HudElementModule draggingElement = null;
    private int dragOffsetX, dragOffsetY;

    public HudEditorScreen() {
        super(Text.literal("nami hud editor"));
    }

    private ClickGuiModule getClickGuiModule() {
        return MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ClickGuiModule clickGuiModule = getClickGuiModule();
        if (clickGuiModule != null && clickGuiModule.background.get())
            context.fill(0, 0, this.width, this.height, 0xC0101010);

        int chatAnimationOffset = (int) ChatAnimationHelper.getAnimationOffset();
        int screenHeight = MC.getWindow().getScaledHeight();
        int chatZoneTop = screenHeight - (screenHeight / 8);

        for (Module module : MODULE_MANAGER.getStorage().getAll()) {
            if (module instanceof HudElementModule hud && hud.isEnabled()) {
                Text text = hud.getDisplayText();
                if (text == null) continue;

                int x = hud.getRenderX();
                int y = hud.getRenderY();
                int height = MC.textRenderer.fontHeight;

                boolean isInChatZone = (y + height) >= chatZoneTop;
                int renderY = y;

                if (isInChatZone) {
                    renderY = y - chatAnimationOffset;
                }

                context.fill(
                        x - 1, renderY - 1,
                        x + hud.width + 1, renderY + height + 1,
                        0x50FFFFFF
                );

                context.drawText(MC.textRenderer, text, x, renderY, 0xFFFFFFFF, false);
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        int chatAnimationOffset = (int) ChatAnimationHelper.getAnimationOffset();
        int screenHeight = MC.getWindow().getScaledHeight();
        int chatZoneTop = screenHeight - (screenHeight / 8);

        for (Module module : MODULE_MANAGER.getStorage().getAll()) {
            if (module instanceof HudElementModule hud && hud.isEnabled()) {
                int x = hud.getRenderX();
                int y = hud.getRenderY();
                int height = MC.textRenderer.fontHeight;

                boolean isInChatZone = (y + height) >= chatZoneTop;
                int renderY = y;

                if (isInChatZone) {
                    renderY = y - chatAnimationOffset;
                }

                if (mouseX >= x && mouseX <= x + hud.width &&
                        mouseY >= renderY && mouseY <= renderY + height) {
                    draggingElement = hud;
                    dragOffsetX = (int) mouseX - x;
                    dragOffsetY = (int) mouseY - renderY;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button != 0 || draggingElement == null) return false;

        int chatAnimationOffset = (int) ChatAnimationHelper.getAnimationOffset();

        int newX = (int) mouseX - dragOffsetX;
        int newY = (int) mouseY - dragOffsetY + chatAnimationOffset;

        int screenWidth = MC.getWindow().getScaledWidth();
        int screenHeight = MC.getWindow().getScaledHeight();

        newX = Math.max(1, Math.min(newX, screenWidth - draggingElement.width - 1));
        newY = Math.max(1, Math.min(newY, screenHeight - draggingElement.height - 1));

        boolean intersects = false;
        for (Module module : MODULE_MANAGER.getStorage().getAll()) {
            if (module instanceof HudElementModule other && other.isEnabled() && other != draggingElement) {
                int ox = other.getRenderX();
                int oy = other.getRenderY();
                int oWidth = other.width;
                int oHeight = MC.textRenderer.fontHeight;

                boolean overlapX = newX < ox + oWidth && newX + draggingElement.width > ox;
                boolean overlapY = newY < oy + oHeight && newY + MC.textRenderer.fontHeight > oy;

                if (overlapX && overlapY) {
                    intersects = true;
                    break;
                }
            }
        }

        if (!intersects) {
            draggingElement.x.set(newX);
            draggingElement.y.set(newY);
        }

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) draggingElement = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            MC.setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}