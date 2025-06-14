package me.kiriyaga.essentials.mixin;

import net.minecraft.network.message.ChatVisibility;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;

import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.lwjgl.glfw.GLFW;
import java.util.List;
import java.util.ArrayList;
import java.awt.Color;
import java.util.Comparator;

import me.kiriyaga.essentials.util.ChatAnimationHelper;
import me.kiriyaga.essentials.feature.module.impl.client.ColorModule;

import static me.kiriyaga.essentials.Essentials.COMMAND_MANAGER;
import static me.kiriyaga.essentials.Essentials.MODULE_MANAGER;
import static me.kiriyaga.essentials.Essentials.LOGGER;
import static me.kiriyaga.essentials.Essentials.MINECRAFT;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen {

    @Unique
    private float animationOffset = 20f;
    @Unique
    private long lastUpdateTime = System.currentTimeMillis();
    @Unique
    private List<String> suggestions = new ArrayList<>();
    @Unique
    private String selectedSuggestion = "";
    @Unique
    private int selectedSuggestionIndex = 0;
    @Shadow
    @Final
    private TextFieldWidget chatField;
    @Unique
    private boolean drawSuggestions = false;


    @Inject(method = "init", at = @At("HEAD"))
    private void onInit(CallbackInfo ci) {
        animationOffset = 20f;
        lastUpdateTime = System.currentTimeMillis();
        ChatAnimationHelper.setAnimationOffset(animationOffset);
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void onRemoved(CallbackInfo ci) {
        animationOffset = 20f;
        ChatAnimationHelper.setAnimationOffset(animationOffset);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderStart(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        long now = System.currentTimeMillis();
        float elapsed = (now - lastUpdateTime) / 1000f;
        lastUpdateTime = now;

        if (animationOffset > 0f) {
            animationOffset -= elapsed * 60f;
            if (animationOffset < 0f) animationOffset = 0f;
        }
        ChatAnimationHelper.setAnimationOffset(animationOffset);
    }

    @Inject(method = "render", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
            shift = At.Shift.BEFORE
    ))
    private void beforeInputRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        context.getMatrices().push();
        context.getMatrices().translate(0, animationOffset, 0);
    }

    @Inject(method = "render", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
            shift = At.Shift.AFTER
    ))
    private void afterInputRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        context.getMatrices().pop();
    }

    @Redirect(method = "render", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"
    ))
        private void redirectFill(DrawContext context, int x1, int y1, int x2, int y2, int color) {
            int width = MINECRAFT.getWindow().getScaledWidth();
            int height = MINECRAFT.getWindow().getScaledHeight();

            if (x1 == 2 && x2 == width - 2 && y1 >= height - 14 && y2 <= height - 2) {
                context.fill(x1, (int)(y1 + animationOffset), x2, (int)(y2 + animationOffset), color);
            } else {
                context.fill(x1, y1, x2, y2, color);
            }
        }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderSuggestions(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ColorModule colorModule = MODULE_MANAGER.getModule(ColorModule.class);
        Color primary = colorModule.getStyledPrimaryColor();
        Color secondary = colorModule.getStyledSecondaryColor();
        Color textCol = colorModule.getStyledTextColor();
        Color textColInverted = new Color(
            255 - textCol.getRed(),
            255 - textCol.getGreen(),
            255 - textCol.getBlue()
        );

        String text = chatField.getText();
        String[] parts = text.split("\\s+");
        String cmdName = parts.length > 0 ? parts[0] : "";


        String prefix = COMMAND_MANAGER.getPrefix();
        suggestions = COMMAND_MANAGER.getSuggestions(cmdName);

        if (!suggestions.isEmpty() && selectedSuggestionIndex < suggestions.size()) {
            selectedSuggestion = suggestions.get(selectedSuggestionIndex);
        } else {
            selectedSuggestion = "";
            selectedSuggestionIndex = 0; // Reset to valid state just in case
        }

        if (selectedSuggestion.isEmpty()) {
            return;
        }

        chatField.setEditableColor(14737632); // Reset to default

        // LOGGER.info(drawSuggestions);
        // LOGGER.info(selectedSuggestion);
        // LOGGER.info("X:" + chatField.getX());
        // LOGGER.info("Y:" + chatField.getY());


        if (cmdName.startsWith(prefix) && suggestions.size() > 1 ){
            drawSuggestions = true;
        } else if (cmdName.startsWith(prefix) && cmdName == selectedSuggestion) {
            drawSuggestions = false;
            return;
        }
        if (!text.startsWith(prefix)) {
            drawSuggestions = false;
            return;
        }
        if (suggestions.size() < 1) {
            drawSuggestions = false;
            return;
        }
        if (drawSuggestions) {
            int x = chatField.getX();
            int y = chatField.getY();
            int width = chatField.getWidth();
            int height = chatField.getHeight();
            int spacing = 6; // Adjust spacing as needed

            // Draw Background 
            // context.fill(x, y-2, x + width, y + height-2, (Color.DARK_GRAY.getRGB()));

            for (int i = 0; i < suggestions.size(); i++) {
                String suggestion = suggestions.get(i);
                int color = (i == selectedSuggestionIndex) ? Color.WHITE.getRGB() : Color.LIGHT_GRAY.getRGB();

                // Draw the suggestion text
                context.drawText(MINECRAFT.textRenderer, suggestion, x, y, color, true);

                // Update x for the next suggestion
                int textWidth = MINECRAFT.textRenderer.getWidth(suggestion);
                x += textWidth + spacing;
            }
        } else {
            suggestions = new ArrayList<>();
            drawSuggestions = false;
            return;
        }
    }
    
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (suggestions.isEmpty()) {
            drawSuggestions = false;
            return;
        }

        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            selectedSuggestionIndex = (selectedSuggestionIndex + 1) % suggestions.size();
            cir.setReturnValue(true);
        } else if (keyCode == GLFW.GLFW_KEY_UP) {
            selectedSuggestionIndex = (selectedSuggestionIndex - 1 + suggestions.size()) % suggestions.size();
            cir.setReturnValue(true);
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            suggestions = new ArrayList<>();
            selectedSuggestionIndex = 0;
            drawSuggestions = false;
            cir.setReturnValue(false);
        } else if (keyCode == GLFW.GLFW_KEY_TAB) {
            if (!suggestions.isEmpty()) {
                if (suggestions.size() == 1) {
                    // Autocomplete if only one suggestion
                    String selected = suggestions.get(0);
                    chatField.setText(selected);
                    chatField.setCursor(selected.length(), false);
                    selectedSuggestionIndex = 0;
                    drawSuggestions = false;
                } else {
                    // Cycle through multiple suggestions
                    selectedSuggestionIndex = (selectedSuggestionIndex + 1) % suggestions.size();
                }

                cir.setReturnValue(true);
            }
        } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            String text = chatField.getText();
            String[] parts = text.split("\\s+");
            String cmdName = parts.length > 0 ? parts[0] : "";
            String selected = suggestions.get(selectedSuggestionIndex);

            if (cmdName.equals(selected)) {
                suggestions = new ArrayList<>();
                drawSuggestions = false;
                selectedSuggestionIndex = 0;
            } else {
                // Suggestion is different — apply it as new input and close suggestions
                chatField.setText(selected);
                chatField.setCursor(selected.length(), false);

                suggestions = new ArrayList<>();
                drawSuggestions = false;
                selectedSuggestionIndex = 0;
            }
            cir.setReturnValue(false);
        }
    }
}

