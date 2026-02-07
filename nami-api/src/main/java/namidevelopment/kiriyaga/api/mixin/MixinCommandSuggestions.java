package namidevelopment.kiriyaga.api.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import namidevelopment.kiriyaga.api.model.command.CommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

import static namidevelopment.kiriyaga.api.NamiApi.*;

@Mixin(CommandSuggestions.class)
public abstract class MixinCommandSuggestions {

    @Shadow @Final EditBox input;
    @Shadow private ParseResults<?> currentParse;
    @Shadow private CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow private CommandSuggestions.SuggestionsList suggestions;
    @Shadow boolean keepSuggestions;
    @Shadow protected abstract void showSuggestions(boolean narrateFirstSuggestion);

    @Inject(method = "updateCommandInfo", at = @At("HEAD"), cancellable = true)
    private void onUpdateCommandInfo(CallbackInfo ci) {

        String prefix = COMMAND_SERVICE.getExecutor().getPrefix();
        if (prefix == null || prefix.isBlank())
            return;

        String string = this.input.getValue();
        if (!string.startsWith(prefix))
            return;

        if (this.currentParse != null && !this.currentParse.getReader().getString().equals(string)) {
            this.currentParse = null;
        }

        if (!this.keepSuggestions) {
            this.input.setSuggestion(null);
            this.suggestions = null;
        }
        int cursor = this.input.getCursorPosition();

        StringReader reader = new StringReader(string);
        reader.setCursor(prefix.length());

        var dispatcher = COMMAND_SERVICE.getSuggester().getDispatcher();
        ParseResults<CommandSource> parse = dispatcher.parse(reader, new CommandSource());
        this.currentParse = parse;
        if (cursor >= prefix.length() && (this.suggestions == null || !this.keepSuggestions)) {
            this.pendingSuggestions = dispatcher.getCompletionSuggestions(parse, cursor);

            this.pendingSuggestions.thenRun(() -> {
                if (this.pendingSuggestions.isDone()) {
                    this.showSuggestions(false);
                }
            });
        }
        ci.cancel();
    }
}
