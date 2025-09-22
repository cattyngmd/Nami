package me.kiriyaga.nami.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.setting.Setting;
import me.kiriyaga.nami.feature.setting.impl.WhitelistSetting;
import me.kiriyaga.nami.util.BlockUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static me.kiriyaga.nami.Nami.*;

public class CommandSuggester {

    private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();
    private final CommandStorage storage;

    private final List<String> itemIdCache = new ArrayList<>();
    private final List<String> blockIdCache = new ArrayList<>();
    private final List<String> generalIdCache = new ArrayList<>();
    private final List<String> soundIdCache = new ArrayList<>();
    private final List<String> particleIdCache = new ArrayList<>();
    private boolean identifierCacheBuilt = false;
    private static final int SUGGESTION_LIMIT = 200;

    public CommandSuggester(CommandStorage storage) {
        this.storage = storage;
    }

    private synchronized void ensureIdentifierCache() {
        if (identifierCacheBuilt) return;
        try {
            java.util.LinkedHashSet<String> ids = new java.util.LinkedHashSet<>();

            // Items
            Registries.ITEM.stream().forEach(item -> {
                Identifier id = Registries.ITEM.getId(item);
                if (id != null) {
                    String s = id.toString().toLowerCase(Locale.ROOT);
                    itemIdCache.add(s);
                    ids.add(s);
                }
            });
            // Blocks
            Registries.BLOCK.stream().forEach(block -> {
                Identifier id = Registries.BLOCK.getId(block);
                if (id != null) {
                    String s = id.toString().toLowerCase(Locale.ROOT);
                    blockIdCache.add(s);
                    ids.add(s);
                }
            });

            Registries.SOUND_EVENT.stream().forEach(snd -> {
                Identifier id = Registries.SOUND_EVENT.getId(snd);
                if (id != null) {
                    soundIdCache.add(id.toString().toLowerCase(Locale.ROOT));
                    ids.add(id.toString().toLowerCase(Locale.ROOT));
                }
            });

            Registries.PARTICLE_TYPE.stream().forEach(p -> {
                Identifier id = Registries.PARTICLE_TYPE.getId(p);
                if (id != null) {
                    particleIdCache.add(id.toString().toLowerCase(Locale.ROOT));
                    ids.add(id.toString().toLowerCase(Locale.ROOT));
                }
            });

            // Other registries useful for whitelists
            try {
                Registries.ENTITY_TYPE.stream().forEach(e -> {
                    Identifier id = Registries.ENTITY_TYPE.getId(e);
                    if (id != null) ids.add(id.toString().toLowerCase(Locale.ROOT));
                });
                        } catch (Exception e) { e.printStackTrace(); }

            try {
                Registries.SOUND_EVENT.stream().forEach(snd -> {
                    Identifier id = Registries.SOUND_EVENT.getId(snd);
                    if (id != null) ids.add(id.toString().toLowerCase(Locale.ROOT));
                });
            } catch (Exception e) { e.printStackTrace(); }

            try {
                Registries.PARTICLE_TYPE.stream().forEach(p -> {
                    Identifier id = Registries.PARTICLE_TYPE.getId(p);
                    if (id != null) ids.add(id.toString().toLowerCase(Locale.ROOT));
                });
            } catch (Exception e) { e.printStackTrace(); }

            generalIdCache.clear();
            generalIdCache.addAll(ids);

                } catch (RuntimeException e) {
            LOGGER.warn("Failed to build identifier caches for suggestions", e);
        }
        identifierCacheBuilt = true;
    }

    public void updateDispatcher() {
        dispatcher.getRoot().getChildren().clear();

        ensureIdentifierCache();

        for (Command command : storage.getCommands()) {
            String displayName = command.getName() == null ? "" : command.getName().replaceAll("\\s", "");
            LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder.literal(displayName);
            CommandArgument[] args = command.getArguments();

            if (args.length == 0) {
                builder.executes(context -> 1);
            } else {
                com.mojang.brigadier.builder.ArgumentBuilder<CommandSource, ?> argumentChain = null;
                for (int i = args.length - 1; i >= 0; i--) {
                    CommandArgument arg = args[i];
                    boolean isLast = (i == args.length - 1);

                    RequiredArgumentBuilder<CommandSource, ?> argBuilder = RequiredArgumentBuilder.argument(arg.getName(), toBrigadierArgument(arg, isLast));

                    if (arg instanceof CommandArgument.ActionArg actionArg) {
                        argBuilder.suggests((context, suggestionBuilder) -> {
                            String rem = suggestionBuilder.getRemaining().toLowerCase(Locale.ROOT);
                            for (String value : actionArg.getAllowedValues()) {
                                if (value.toLowerCase(Locale.ROOT).startsWith(rem)) suggestionBuilder.suggest(value.replaceAll("\\s", ""));
                            }
                            return suggestionBuilder.buildFuture();
                        });
                    }

                    if (arg instanceof CommandArgument.ModuleArg) {
                        argBuilder.suggests((context, suggestionBuilder) -> {
                            String remaining = suggestionBuilder.getRemaining().toLowerCase(Locale.ROOT);
                            AtomicInteger count = new AtomicInteger(0);
                            for (Module m : MODULE_MANAGER.getStorage().getAll()) {
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
                            String[] parts = input.split("\\s+");
                            String remaining = suggestionBuilder.getRemaining().toLowerCase(Locale.ROOT);

                            java.util.function.UnaryOperator<String> stripQuotes = s -> {
                                if (s == null) return null;
                                s = s.trim();
                                if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
                                    return s.substring(1, s.length() - 1);
                                }
                                return s;
                            };

                            String moduleName = null;
                            if (parts.length >= 2) {
                                moduleName = stripQuotes.apply(parts[1]);
                            } else if (parts.length >= 1) {
                                String first = parts[0];
                                String prefix = COMMAND_MANAGER.getExecutor().getPrefix();
                                if (first.startsWith(prefix)) first = first.substring(prefix.length());
                                moduleName = stripQuotes.apply(first);
                            }

                            Module module = null;
                            if (moduleName != null) {
                                module = MODULE_MANAGER.getStorage().getByName(moduleName);
                                if (module == null) {
                                    for (Module m : MODULE_MANAGER.getStorage().getAll()) {
                                        if (m.matches(moduleName)) { module = m; break; }
                                    }
                                }
                            }

                            int count = 0;
                            if (module != null) {
                                for (Setting<?> s : module.getSettings()) {
                                    String name = s.getName();
                                    String nameNoSpaces = name == null ? "" : name.replaceAll("\\s", "");
                                    if (nameNoSpaces.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                                        suggestionBuilder.suggest(nameNoSpaces);
                                        if (++count >= SUGGESTION_LIMIT) break;
                                    }
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
                                String prefix = COMMAND_MANAGER.getExecutor().getPrefix();
                                if (cmdName != null && cmdName.startsWith(prefix)) cmdName = cmdName.substring(prefix.length());

                                Module possibleModule = null;
                                if (cmdName != null) possibleModule = MODULE_MANAGER.getStorage().getByName(cmdName);
                                if (possibleModule == null && cmdName != null) {
                                    for (Module mm : MODULE_MANAGER.getStorage().getAll()) {
                                        if (mm.matches(cmdName)) { possibleModule = mm; break; }
                                    }
                                }

                                if (possibleModule != null && inParts.length >= 2) {
                                    String settingToken = inParts[1];
                                    Setting<?> st = possibleModule.getSettingByName(settingToken);
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
                                for (net.minecraft.block.Block b : BlockUtils.getNonVanillaGeneratedBlocks()) {
                                    Identifier id = Registries.BLOCK.getId(b);
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

    private ArgumentType<?> toBrigadierArgument(CommandArgument arg, boolean isLast) {
        if (arg instanceof CommandArgument.StringArg) {
            return isLast ? StringArgumentType.greedyString() : StringArgumentType.string();
        } else if (arg instanceof CommandArgument.ActionArg) {
            return StringArgumentType.string();
        } else if (arg instanceof CommandArgument.IntArg) {
            return IntegerArgumentType.integer();
        } else if (arg instanceof CommandArgument.DoubleArg) {
            return DoubleArgumentType.doubleArg();
        } else if (arg instanceof CommandArgument.ModuleArg) {
            return StringArgumentType.string();
        } else if (arg instanceof CommandArgument.SettingArg) {
            return StringArgumentType.string();
        } else if (arg instanceof CommandArgument.IdentifierArg) {
            return StringArgumentType.string();
        }
        return StringArgumentType.string();
    }

    public CommandDispatcher<CommandSource> getDispatcher() {
        return dispatcher;
    }
}
