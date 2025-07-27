package me.kiriyaga.nami.feature.gui.screen;

import me.kiriyaga.nami.feature.module.HudAlignment;
import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;
import me.kiriyaga.nami.util.ChatAnimationHelper;

import java.util.ArrayList;

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
                int y = hud.getRenderY();

                boolean isInChatZone = (y + hud.height) >= chatZoneTop;
                int renderY = isInChatZone ? y - chatAnimationOffset : y;

                int baseX = hud.getRenderX();

                boolean hovered = false;
                if (hud.alignment.get() == HudAlignment.left) {
                    hovered = mouseX >= baseX && mouseX <= baseX + hud.width;
                } else if (hud.alignment.get() == HudAlignment.center) {
                    hovered = mouseX >= baseX - hud.width / 2 && mouseX <= baseX + hud.width / 2;
                } else if (hud.alignment.get() == HudAlignment.right) {
                    hovered = mouseX >= baseX - hud.width && mouseX <= baseX;
                }

                hovered = hovered && mouseY >= renderY && mouseY <= renderY + hud.height;

                if (hovered) {
                    int rectLeft;
                    switch (hud.alignment.get()) {
                        case left -> rectLeft = baseX - 1;
                        case center -> rectLeft = baseX - hud.width / 2 - 1;
                        case right -> rectLeft = baseX - hud.width - 1;
                        default -> rectLeft = baseX - 1;
                    }

                    int rectRight = rectLeft + hud.width + 2;

                    context.fill(
                            rectLeft,
                            renderY - 1,
                            rectRight,
                            renderY + hud.height + 1,
                            0x50FFFFFF
                    );
                }

                for (HudElementModule.TextElement element : new ArrayList<>(hud.getTextElements())) {
                    int drawX = hud.getRenderXForElement(element);
                    int drawY = renderY + element.offsetY();

                    context.drawText(MC.textRenderer, element.text(), drawX, drawY, 0xFFFFFFFF, false);
                }
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
                int renderY = (y + hud.height) >= chatZoneTop ? y - chatAnimationOffset : y;

                if (mouseX >= x && mouseX <= x + hud.width &&
                        mouseY >= renderY && mouseY <= renderY + hud.height) {
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

        int newRenderX = (int) mouseX - dragOffsetX;
        int newRenderY = (int) mouseY - dragOffsetY + chatAnimationOffset;

        int screenWidth = MC.getWindow().getScaledWidth();
        int screenHeight = MC.getWindow().getScaledHeight();

        newRenderY = Math.max(1, Math.min(newRenderY, screenHeight - draggingElement.height - 1));

        int newX;

        switch (draggingElement.alignment.get()) {
            case left:
                newRenderX = Math.max(1, Math.min(newRenderX, screenWidth - draggingElement.width - 1));
                newX = newRenderX;
                break;
            case center:
                newRenderX = Math.max(draggingElement.width / 2, Math.min(newRenderX, screenWidth - draggingElement.width / 2));
                newX = newRenderX + draggingElement.width / 2;
                break;
            case right:
                newRenderX = Math.max(0, Math.min(newRenderX, screenWidth - draggingElement.width));
                newX = newRenderX + draggingElement.width;
                break;
            default:
                newRenderX = Math.max(1, Math.min(newRenderX, screenWidth - draggingElement.width - 1));
                newX = newRenderX;
                break;
        }

        boolean intersects = false;
        for (Module module : MODULE_MANAGER.getStorage().getAll()) {
            if (module instanceof HudElementModule other && other.isEnabled() && other != draggingElement) {
                int ox = other.getRenderX();
                int oy = other.getRenderY();
                int oWidth = other.width;
                int oHeight = other.height;

                boolean overlapX = newRenderX < ox + oWidth && newRenderX + draggingElement.width > ox;
                boolean overlapY = newRenderY < oy + oHeight && newRenderY + draggingElement.height > oy;

                if (overlapX && overlapY) {
                    intersects = true;
                    break;
                }
            }
        }

        if (!intersects) {
            draggingElement.x.set(newX);
            draggingElement.y.set(newRenderY);
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