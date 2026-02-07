package namidevelopment.kiriyaga.api.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import static namidevelopment.kiriyaga.api.NamiApi.*;

import namidevelopment.kiriyaga.api.model.command.Command;
import namidevelopment.kiriyaga.api.model.command.CommandArgument;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.setting.Setting;
import namidevelopment.kiriyaga.api.model.setting.WhitelistSetting;
import namidevelopment.kiriyaga.api.util.BlockUtils;
import namidevelopment.kiriyaga.api.util.KeyUtils;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class CommandSuggester {

    private final CommandDispatcher<SharedSuggestionProvider> dispatcher = new CommandDispatcher<>();
    private final CommandStorage storage;

    private final List<String> itemIdCache = new ArrayList<>();
    private final List<String> blockIdCache = new ArrayList<>();
    private final List<String> generalIdCache = new ArrayList<>();
    private final List<String> soundIdCache = new ArrayList<>();
    private final List<String> particleIdCache = new ArrayList<>();
    private final List<String> keyNameCache = new ArrayList<>();
    private final List<String> configNameCache = new ArrayList<>();
    private final List<String> playerListCache = new java.util.concurrent.CopyOnWriteArrayList<>();
    private boolean identifierCacheBuilt = false;
    private static final int SUGGESTION_LIMIT = 200;

    public CommandSuggester(CommandStorage storage) {
        this.storage = storage;
        API_MC.execute(() -> {
            if (API_MC.getConnection() == null) {
                if (!playerListCache.isEmpty()) playerListCache.clear();
                return;
            }
            List<String> currentNames = API_MC.getConnection().getOnlinePlayers()
                    .stream()
                    .map(p -> p.getProfile().name())
                    .toList();
            if (!playerListCache.equals(currentNames)) {
                playerListCache.clear();
                playerListCache.addAll(currentNames);
            }
        });
    }

    private synchronized void ensureIdentifierCache() {
        if (identifierCacheBuilt) return;
        try {
            java.util.LinkedHashSet<String> ids = new java.util.LinkedHashSet<>();

            // Items
            BuiltInRegistries.ITEM.stream().forEach(item -> {
                Identifier id = BuiltInRegistries.ITEM.getKey(item);
                if (id != null) {
                    String s = id.toString().toLowerCase(Locale.ROOT);
                    itemIdCache.add(s);
                    ids.add(s);
                }
            });
            // Blocks
            BuiltInRegistries.BLOCK.stream().forEach(block -> {
                Identifier id = BuiltInRegistries.BLOCK.getKey(block);
                if (id != null) {
                    String s = id.toString().toLowerCase(Locale.ROOT);
                    blockIdCache.add(s);
                    ids.add(s);
                }
            });

            BuiltInRegistries.SOUND_EVENT.stream().forEach(snd -> {
                Identifier id = BuiltInRegistries.SOUND_EVENT.getKey(snd);
                if (id != null) {
                    soundIdCache.add(id.toString().toLowerCase(Locale.ROOT));
                    ids.add(id.toString().toLowerCase(Locale.ROOT));
                }
            });

            BuiltInRegistries.PARTICLE_TYPE.stream().forEach(p -> {
                Identifier id = BuiltInRegistries.PARTICLE_TYPE.getKey(p);
                if (id != null) {
                    particleIdCache.add(id.toString().toLowerCase(Locale.ROOT));
                    ids.add(id.toString().toLowerCase(Locale.ROOT));
                }
            });

            // Key names
            keyNameCache.clear();

            // Dynamically get key names from KeyUtils by iterating through possible key codes
            IntStream.range(-1, 350).forEach(keyCode -> {
                String keyName = KeyUtils.getKeyName(keyCode);
                if (keyName != null && !keyName.startsWith("KEY_")) {
                    keyNameCache.add(keyName);
                }
            });

            // Config names
            configNameCache.clear();
            try {
                configNameCache.addAll(CONFIG_SERVICE.getConfigSerializer().listConfigs());
            } catch (Exception e) {
                API_LOGGER.warn("Failed to load config names for suggestions", e);
            }

            // Other registries useful for whitelists
            try {
                BuiltInRegistries.ENTITY_TYPE.stream().forEach(e -> {
                    Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(e);
                    if (id != null) ids.add(id.toString().toLowerCase(Locale.ROOT));
                });
                        } catch (Exception e) { e.printStackTrace(); }

            try {
                BuiltInRegistries.SOUND_EVENT.stream().forEach(snd -> {
                    Identifier id = BuiltInRegistries.SOUND_EVENT.getKey(snd);
                    if (id != null) ids.add(id.toString().toLowerCase(Locale.ROOT));
                });
            } catch (Exception e) { e.printStackTrace(); }

            try {
                BuiltInRegistries.PARTICLE_TYPE.stream().forEach(p -> {
                    Identifier id = BuiltInRegistries.PARTICLE_TYPE.getKey(p);
                    if (id != null) ids.add(id.toString().toLowerCase(Locale.ROOT));
                });
            } catch (Exception e) { e.printStackTrace(); }

            generalIdCache.clear();
            generalIdCache.addAll(ids);

                } catch (RuntimeException e) {
            API_LOGGER.warn("Failed to build identifier caches for suggestions", e);
        }
        identifierCacheBuilt = true;
    }

    public void updateDispatcher() {
        dispatcher.getRoot().getChildren().clear();

        ensureIdentifierCache();

        for (Command command : storage.getCommands()) {
            String displayName = command.getName() == null ? "" : command.getName().replaceAll("\\s", "");
            LiteralArgumentBuilder<SharedSuggestionProvider> builder = LiteralArgumentBuilder.literal(displayName);
            CommandArgument[] args = command.getArguments();

            if (args.length == 0) {
                builder.executes(context -> 1);
            } else {
                com.mojang.brigadier.builder.ArgumentBuilder<SharedSuggestionProvider, ?> argumentChain = null;
                for (int i = args.length - 1; i >= 0; i--) {
                    CommandArgument arg = args[i];
                    boolean isLast = (i == args.length - 1);

                    RequiredArgumentBuilder<SharedSuggestionProvider, ?> argBuilder = RequiredArgumentBuilder.argument(arg.getName(), toBrigadierArgument(arg, isLast));

                    if (arg instanceof CommandArgument.ActionArg actionArg) {
                        argBuilder.suggests((context, suggestionBuilder) -> {
                            String rem = suggestionBuilder.getRemaining().toLowerCase(Locale.ROOT);
                            for (String value : actionArg.getAllowedValues()) {
                                if (value.toLowerCase(Locale.ROOT).startsWith(rem)) suggestionBuilder.suggest(value.replaceAll("\\s", ""));
                            }
                            return suggestionBuilder.buildFuture();
                        });
                    }

                    if (arg instanceof CommandArgument.FeatureArg) {
                        argBuilder.suggests((context, suggestionBuilder) -> {
                            String remaining = suggestionBuilder.getRemaining().toLowerCase(Locale.ROOT);
                            AtomicInteger count = new AtomicInteger(0);
                            for (Feature m : FEATURE_SERVICE.getStorage().getAll()) {
                                if (m == null) continue;
                                String nameNoSpaces = m.getName() == null ? "" : m.getName().replaceAll("\\s", "");
                                if (nameNoSpaces.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                                    suggestionBuilder.suggest(nameNoSpaces);
                                    if (count.incrementAndGet() >= SUGGESTION_LIMIT) break;
                                }
                            }
                            return suggestionBuilder.buildFuture();
                        });
                    }

                    if (arg instanceof CommandArgument.SettingArg) {
                        argBuilder.suggests((context, suggestionBuilder) -> {
                            String input = context.getInput();
                            String remaining = suggestionBuilder.getRemaining().toLowerCase(Locale.ROOT);

                            String[] parts = input.split("\\s+");
                            if (parts.length < 1) return suggestionBuilder.buildFuture();

                            String prefix = COMMAND_SERVICE.getExecutor().getPrefix();
                            String FeatureName = parts[0].startsWith(prefix) ? parts[0].substring(prefix.length()) : parts[0];

                            Feature Feature = FEATURE_SERVICE.getStorage().getByName(FeatureName);
                            if (Feature == null) {
                                for (Feature m : FEATURE_SERVICE.getStorage().getAll()) {
                                    if (m.matches(FeatureName)) { Feature = m; break; }
                                }
                            }
                            if (Feature == null) return suggestionBuilder.buildFuture();

                            int count = 0;
                            for (Setting<?> s : Feature.getSettings()) {
                                String nameNoSpaces = s.getName().replaceAll("\\s", "");
                                if (nameNoSpaces.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                                    suggestionBuilder.suggest(nameNoSpaces);
                                    if (++count >= SUGGESTION_LIMIT) break;
                                }
                            }
                            return suggestionBuilder.buildFuture();
                        });
                    }

                    if (arg instanceof CommandArgument.KeyBindArg) {
                        argBuilder.suggests((context, suggestionBuilder) -> {
                            String rem = suggestionBuilder.getRemaining().toLowerCase(Locale.ROOT);
                            for (String value : keyNameCache) {
                                if (value.toLowerCase(Locale.ROOT).startsWith(rem)) {
                                    suggestionBuilder.suggest(value);
                                }
                            }
                            return suggestionBuilder.buildFuture();
                        });
                    }

                    if (arg instanceof CommandArgument.ConfigNameArg) {
                        argBuilder.suggests((context, suggestionBuilder) -> {
                            String rem = suggestionBuilder.getRemaining().toLowerCase(Locale.ROOT);
                            for (String value : configNameCache) {
                                if (value.toLowerCase(Locale.ROOT).startsWith(rem)) {
                                    suggestionBuilder.suggest(value);
                                }
                            }
                            return suggestionBuilder.buildFuture();
                        });
                    }

                    if (arg instanceof CommandArgument.IdentifierArg idArg) {
                        argBuilder.suggests((context, suggestionBuilder) -> {
                            String remaining = suggestionBuilder.getRemaining().toLowerCase(Locale.ROOT);
                            AtomicInteger count = new AtomicInteger(0);

                            java.util.function.Predicate<String> matches = s -> {
                                if (s == null) return false;
                                if (remaining.contains(":")) return s.startsWith(remaining);
                                if (s.startsWith(remaining)) return true;
                                int idx = s.indexOf(':');
                                String path = idx >= 0 ? s.substring(idx + 1) : s;
                                return path.startsWith(remaining);
                            };

                            java.util.HashSet<String> suggested = new java.util.HashSet<>();

                            List<String> primary;
                            List<String> secondary;

                            try {
                                String input = context.getInput();
                                String[] inParts = input.split("\\s+");
                                String cmdToken = inParts.length > 0 ? inParts[0] : null;
                                String cmdName = cmdToken;
                                String prefix = COMMAND_SERVICE.getExecutor().getPrefix();
                                if (cmdName != null && cmdName.startsWith(prefix)) cmdName = cmdName.substring(prefix.length());

                                Feature possibleFeature = null;
                                if (cmdName != null) possibleFeature = FEATURE_SERVICE.getStorage().getByName(cmdName);
                                if (possibleFeature == null && cmdName != null) {
                                    for (Feature mm : FEATURE_SERVICE.getStorage().getAll()) {
                                        if (mm.matches(cmdName)) { possibleFeature = mm; break; }
                                    }
                                }

                                if (possibleFeature != null && inParts.length >= 2) {
                                    String settingToken = inParts[1];
                                    Setting<?> st = possibleFeature.getSettingByName(settingToken);
                                    if (st instanceof WhitelistSetting wl) {
                                        var types = wl.getAllowedTypes();
                                        if (types.contains(WhitelistSetting.Type.ANY) || types.size() > 1) {
                                            primary = generalIdCache; secondary = null;
                                        } else if (types.contains(WhitelistSetting.Type.BLOCK)) {
                                            primary = blockIdCache; secondary = null;
                                        } else if (types.contains(WhitelistSetting.Type.ITEM)) {
                                            primary = itemIdCache; secondary = null;
                                        } else if (types.contains(WhitelistSetting.Type.SOUND)) {
                                            primary = soundIdCache; secondary = null;
                                        } else if (types.contains(WhitelistSetting.Type.PARTICLE)) {
                                            primary = particleIdCache; secondary = null;
                                        } else {
                                            primary = generalIdCache; secondary = null;
                                        }
                                    }else {
                                        if (idArg.getTarget() == CommandArgument.IdentifierArg.Target.BLOCK) { primary = blockIdCache; secondary = null; }
                                        else if (idArg.getTarget() == CommandArgument.IdentifierArg.Target.ITEM) { primary = itemIdCache; secondary = null; }
                                        else { primary = generalIdCache; secondary = null; }
                                    }
                                } else {
                                    if (idArg.getTarget() == CommandArgument.IdentifierArg.Target.BLOCK) { primary = blockIdCache; secondary = null; }
                                    else if (idArg.getTarget() == CommandArgument.IdentifierArg.Target.ITEM) { primary = itemIdCache; secondary = null; }
                                    else { primary = generalIdCache; secondary = null; }
                                }
                            } catch (Exception ignored) {
                                if (idArg.getTarget() == CommandArgument.IdentifierArg.Target.BLOCK) { primary = blockIdCache; secondary = null; }
                                else if (idArg.getTarget() == CommandArgument.IdentifierArg.Target.ITEM) { primary = itemIdCache; secondary = null; }
                                else { primary = generalIdCache; secondary = null; }
                            }

                            java.util.function.Consumer<List<String>> offerList = list -> {
                                if (list == null) return;
                                for (String id : list) {
                                    if (id == null) continue;
                                    if (!matches.test(id)) continue;
                                    if (suggested.add(id)) {
                                        suggestionBuilder.suggest(id.replaceAll("\\s", ""));
                                        if (count.incrementAndGet() >= SUGGESTION_LIMIT) return;
                                    }
                                }
                            };

                            offerList.accept(primary);
                            if (count.get() < SUGGESTION_LIMIT) offerList.accept(secondary);

                            if (idArg.getTarget() == CommandArgument.IdentifierArg.Target.BLOCK && count.get() < SUGGESTION_LIMIT) {
                                for (net.minecraft.world.level.block.Block b : BlockUtils.getNonVanillaGeneratedBlocks()) {
                                    Identifier id = BuiltInRegistries.BLOCK.getKey(b);
                                    if (id == null) continue;
                                    String s = id.toString().toLowerCase(Locale.ROOT);
                                    if (!matches.test(s)) continue;
                                    if (suggested.add(s)) {
                                        suggestionBuilder.suggest(s.replaceAll("\\s", ""));
                                        if (count.incrementAndGet() >= SUGGESTION_LIMIT) break;
                                    }
                                }
                            }

                            return suggestionBuilder.buildFuture();
                        });
                    }

                    if (arg instanceof CommandArgument.OnlinePlayerArg) {
                        argBuilder.suggests((context, suggestionBuilder) -> {
                            String rem = suggestionBuilder.getRemaining().toLowerCase(Locale.ROOT);
                            for (String name : playerListCache) {
                                if (name.toLowerCase(Locale.ROOT).startsWith(rem)) {
                                    suggestionBuilder.suggest(name);
                                }
                            }
                            return suggestionBuilder.buildFuture();
                        });
                    }

                    if (arg instanceof CommandArgument.FriendArg) {
                        argBuilder.suggests((context, suggestionBuilder) -> {
                            suggestFriends(suggestionBuilder);
                            return suggestionBuilder.buildFuture();
                        });
                    }

                    if (arg instanceof CommandArgument.FriendNameArg) {
                        argBuilder.suggests((context, suggestionBuilder) -> {
                            String input = context.getInput();
                            String[] parts = input.split("\\s+");
                            String rem = suggestionBuilder.getRemaining().toLowerCase(Locale.ROOT);

                            if (parts.length > 1) {
                                String action = parts[1].toLowerCase(Locale.ROOT);
                                if (action.equals("add")) {
                                    for (String name : playerListCache) {
                                        if (name.toLowerCase(Locale.ROOT).startsWith(rem)) {
                                            suggestionBuilder.suggest(name);
                                        }
                                    }
                                } else if (action.equals("del")) {
                                    suggestFriends(suggestionBuilder);
                                }
                            }
                            return suggestionBuilder.buildFuture();
                        });
                    }

                    if (argumentChain == null) {
                        argBuilder.executes(context -> 1);
                    } else {
                        argBuilder.then(argumentChain);
                    }
                    argumentChain = argBuilder;
                }
                builder.then(argumentChain);
            }

            dispatcher.register(builder);
        }
    }

    private void suggestFriends(SuggestionsBuilder suggestionBuilder) {
        String rem = suggestionBuilder.getRemaining().toLowerCase(Locale.ROOT);
        FRIEND_SERVICE.getFriends().forEach(f -> {
            if (f.toLowerCase(Locale.ROOT).startsWith(rem)) {
                suggestionBuilder.suggest(f);
            }
        });
    }

    private ArgumentType<?> toBrigadierArgument(CommandArgument arg, boolean isLast) {
        if (arg instanceof CommandArgument.StringArg) {
            return isLast ? StringArgumentType.greedyString() : StringArgumentType.string();
        } else if (arg instanceof CommandArgument.ActionArg) {
            return StringArgumentType.string();
        } else if (arg instanceof CommandArgument.IntArg) {
            return IntegerArgumentType.integer();
        } else if (arg instanceof CommandArgument.DoubleArg) {
            return DoubleArgumentType.doubleArg();
        } else if (arg instanceof CommandArgument.FeatureArg) {
            return StringArgumentType.string();
        } else if (arg instanceof CommandArgument.SettingArg) {
            return StringArgumentType.string();
        } else if (arg instanceof CommandArgument.IdentifierArg) {
            return StringArgumentType.string();
        } else if (arg instanceof CommandArgument.KeyBindArg) {
            return StringArgumentType.string();
        } else if (arg instanceof CommandArgument.ConfigNameArg) {
            return StringArgumentType.string();
        }
        return StringArgumentType.string();
    }

    public CommandDispatcher<SharedSuggestionProvider> getDispatcher() {
        return dispatcher;
    }
}