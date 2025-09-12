package me.kiriyaga.nami.util.container;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static me.kiriyaga.nami.Nami.MC;
import static net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED;

public class ContainerScreen extends ShulkerBoxScreen {
    private static final Identifier TEXTURE = Identifier.of("textures/gui/container/shulker_box.png");
    private final ItemStack[] contents;
    private final ItemStack containerStack;

    public ContainerScreen(ItemStack containerStack, ItemStack[] contents) {
        super(new ShulkerBoxScreenHandler(0, MC.player.getInventory(), new SimpleInventory(contents)), MC.player.getInventory(), Text.translatable(containerStack.getItemName().getString()));
        this.containerStack = containerStack;
        this.contents = contents;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(GUI_TEXTURED, TEXTURE, x, y, 0f, 0f, backgroundWidth, backgroundHeight, 256, 256);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    public static void open(ItemStack stack, ItemStack[] contents) {
        MC.setScreen(new ContainerScreen(stack, contents));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }
}
