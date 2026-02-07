package namidevelopment.kiriyaga.api.model.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import namidevelopment.kiriyaga.api.model.setting.*;

import net.minecraft.core.registries.BuiltInRegistries;

import java.util.concurrent.CompletableFuture;

public class SettingValueArgument implements ArgumentType<Object> {

    private final Setting<?> setting;

    public SettingValueArgument(Setting<?> setting) {
        this.setting = setting;
    }

    @Override
    public Object parse(com.mojang.brigadier.StringReader reader) throws CommandSyntaxException {
        String raw = reader.readUnquotedString();

        if (setting instanceof BoolSetting) {
            return Boolean.parseBoolean(raw);
        }

        if (setting instanceof IntSetting s) {
            return Integer.parseInt(raw);
        }

        if (setting instanceof DoubleSetting s) {
            return Double.parseDouble(raw);
        }

        if (setting instanceof EnumSetting<?> s) {
            for (Enum<?> e : s.getValues()) {
                if (e.name().equalsIgnoreCase(raw)) {
                    return e;
                }
            }
            return null;
        }

        if (setting instanceof KeyBindSetting) {
            if (raw.equalsIgnoreCase("none")) return KeyBindSetting.KEY_NONE;
            try {
                return Integer.parseInt(raw);
            } catch (Exception ignored) {}
            return KeyBindSetting.KEY_NONE;
        }

        if (setting instanceof ColorSetting) {
            int r = Integer.parseInt(raw);
            reader.skipWhitespace();
            int g = reader.readInt();
            reader.skipWhitespace();
            int b = reader.readInt();

            int a = 255;
            if (reader.canRead()) {
                reader.skipWhitespace();
                a = reader.readInt();
            }

            return new int[]{r, g, b, a};
        }

        if (setting instanceof WhitelistSetting) {
            return raw;
        }

        return raw;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {

        if (setting instanceof BoolSetting) {
            builder.suggest("true");
            builder.suggest("false");
            return builder.buildFuture();
        }

        if (setting instanceof EnumSetting<?> s) {
            for (Enum<?> e : s.getValues()) {
                builder.suggest(e.name().toLowerCase());
            }
            return builder.buildFuture();
        }

        if (setting instanceof KeyBindSetting) {
            builder.suggest("none");
            builder.suggest("KEY_65");
            builder.suggest("MOUSE_LEFT");
            builder.suggest("MOUSE_RIGHT");
            return builder.buildFuture();
        }

        if (setting instanceof WhitelistSetting wl) {
            if (wl.allows(WhitelistSetting.Type.ITEM)) {
                BuiltInRegistries.ITEM.keySet().forEach(id -> builder.suggest(id.toString()));
            }
            if (wl.allows(WhitelistSetting.Type.BLOCK)) {
                BuiltInRegistries.BLOCK.keySet().forEach(id -> builder.suggest(id.toString()));
            }
            if (wl.allows(WhitelistSetting.Type.ENTITY)) {
                BuiltInRegistries.ENTITY_TYPE.keySet().forEach(id -> builder.suggest(id.toString()));
            }
            if (wl.allows(WhitelistSetting.Type.SOUND)) {
                BuiltInRegistries.SOUND_EVENT.keySet().forEach(id -> builder.suggest(id.toString()));
            }
            if (wl.allows(WhitelistSetting.Type.PARTICLE)) {
                BuiltInRegistries.PARTICLE_TYPE.keySet().forEach(id -> builder.suggest(id.toString()));
            }

            return builder.buildFuture();
        }

        return builder.buildFuture();
    }

    public static SettingValueArgument of(Setting<?> setting) {
        return new SettingValueArgument(setting);
    }
}
