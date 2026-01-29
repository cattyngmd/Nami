package namidevelopment.kiriyaga.nami.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import namidevelopment.kiriyaga.nami.Nami;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.SharedSuggestionProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(value = CommandSuggestions.class)
public abstract class MixinCommandSuggestions {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private EditBox input;
    @Shadow private ParseResults<SharedSuggestionProvider> currentParse;
    @Shadow private CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow private CommandSuggestions.SuggestionsList suggestions;
    @Shadow private boolean keepSuggestions;

    @Shadow
    protected abstract void updateUsageInfo();

    @Inject(
        method = "updateCommandInfo",
        at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/StringReader;canRead()Z", remap = false),
        cancellable = true
    )
    private void onRefresh(CallbackInfo ci, @Local StringReader reader) {
        String text = this.input.getValue();
        String prefix = Nami.COMMAND_SERVICE.getExecutor().getPrefix();

        if (text.startsWith(prefix) && reader.getCursor() == 0) {
            reader.setCursor(prefix.length());

            SharedSuggestionProvider source = this.minecraft.getConnection().getSuggestionsProvider();
            this.currentParse = Nami.COMMAND_SERVICE.getSuggester().getDispatcher().parse(reader, source);

            int cursor = this.input.getCursorPosition();
            if (cursor >= prefix.length() && (this.suggestions == null || !this.keepSuggestions)) {
                this.pendingSuggestions = Nami.COMMAND_SERVICE.getSuggester().getDispatcher().getCompletionSuggestions(this.currentParse, cursor);
                this.pendingSuggestions.thenRun(() -> {
                    if (this.pendingSuggestions.isDone()) {
                        this.updateUsageInfo();
                    }
                });
            }
            ci.cancel();
        }
    }
}
