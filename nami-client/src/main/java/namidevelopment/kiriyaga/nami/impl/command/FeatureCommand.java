package namidevelopment.kiriyaga.nami.impl.command;

import namidevelopment.kiriyaga.api.core.command.CommandStorage;
import namidevelopment.kiriyaga.api.model.command.Command;
import namidevelopment.kiriyaga.api.model.command.CommandArgument;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.setting.*;
import net.minecraft.resources.Identifier;

import java.util.Arrays;

import static namidevelopment.kiriyaga.api.NamiApi.*;

// no god below anymore
public class FeatureCommand extends Command {
    private final Feature Feature;

    public FeatureCommand(Feature Feature) {
        super(
                Feature.getName().replace(" ", ""),
                buildArguments(Feature)
        );
        this.Feature = Feature;
    }

    private static CommandArgument[] buildArguments(Feature Feature) {
        CommandArgument[] defaults = new CommandArgument[]{
                new CommandArgument.SettingArg("setting"),
                new CommandArgument.StringArg("value", 1, 256) {
                    @Override
                    public boolean isRequired() { return false; } // it works very corny, you are unable to set value in Feature with whitelist setting, also auto correct tries to do whitelist args correct
                }
        };

        for (Setting<?> s : Feature.getSettings()) { // god i hate command building
            if (s instanceof WhitelistSetting wl) {
                return new CommandArgument[]{
                        new CommandArgument.SettingArg("setting") {
                            @Override
                            public boolean isRequired() {
                                return false;
                            }
                        },
                        new CommandArgument.ActionArg("action", "add", "del", "list") {
                            @Override
                            public boolean isRequired() {
                                return false;
                            }
                        },
                        new CommandArgument.IdentifierArg("id", toTarget(wl)) {
                            @Override
                            public boolean isRequired() {
                                return false;
                            }
                        }
                };
            }
        }

        return defaults;
    }

    private static CommandArgument.IdentifierArg.Target toTarget(WhitelistSetting wl) {
        var types = wl.getAllowedTypes();
        if (types.contains(WhitelistSetting.Type.ANY) || types.size() > 1) return CommandArgument.IdentifierArg.Target.ANY;
        if (types.contains(WhitelistSetting.Type.BLOCK)) return CommandArgument.IdentifierArg.Target.BLOCK;
        if (types.contains(WhitelistSetting.Type.ITEM)) return CommandArgument.IdentifierArg.Target.ITEM;
        if (types.contains(WhitelistSetting.Type.SOUND)) return CommandArgument.IdentifierArg.Target.SOUND;
        if (types.contains(WhitelistSetting.Type.PARTICLE)) return CommandArgument.IdentifierArg.Target.PARTICLE;
        return CommandArgument.IdentifierArg.Target.ANY;
    }

    @Override
    public void execute(Object[] parsedArgs) {
        String prefix = COMMAND_SERVICE.getExecutor().getPrefix();

        String settingNameRaw = ((String) parsedArgs[0]);
        String valueRaw = parsedArgs.length > 1 ? (String) parsedArgs[1] : null;

        String settingName = settingNameRaw.replace(" ", "");

        Setting<?> setting = null;
        for (Setting<?> s : Feature.getSettings()) {
            if (s.getName().replace(" ", "").equalsIgnoreCase(settingName)) {
                setting = s;
                break;
            }
        }

        if (setting == null) {
            CHAT_SERVICE.sendPersistent(Feature.getName(),
                    CAT_FORMAT.format("Setting {g}" + settingNameRaw + "{reset} not found in Feature {g}" + Feature.getName() + "{reset}."));
            return;
        }

        if (setting instanceof WhitelistSetting wlSetting) {
            String action = parsedArgs.length > 1 ? ((String) parsedArgs[1]).toLowerCase() : null;
            String item = parsedArgs.length > 2 ? ((String) parsedArgs[2]) : null;

            if (action == null) {
                CHAT_SERVICE.sendPersistent(Feature.getName(),
                        CAT_FORMAT.format("Usage: {s}" + prefix + "{g}" + Feature.getName() +
                                " {g}" + setting.getName() + " add{reset}/{g}del{reset}/{g}list {g}[item]{reset}."));
                return;
            }

            switch (action) {
                case "add" -> {
                    if (item == null || item.isEmpty()) {
                        CHAT_SERVICE.sendPersistent(Feature.getName(),
                                CAT_FORMAT.format("Usage: {s}" + prefix + "{g}" + Feature.getName() +
                                        " {g}" + setting.getName() + " add {s}<{g}item{s}>{reset}."));
                        return;
                    }
                    if (wlSetting.addToWhitelist(item)) {
                        CHAT_SERVICE.sendPersistent(Feature.getName(),
                                CAT_FORMAT.format("Added: {g}" + item + "{reset} to {g}" +
                                        setting.getName() + "{reset}."));
                    } else {
                        CHAT_SERVICE.sendPersistent(Feature.getName(),
                                CAT_FORMAT.format("Invalid item id or already added: {g}" +
                                        item + "{reset}."));
                    }
                }
                case "del" -> {
                    if (item == null || item.isEmpty()) {
                        CHAT_SERVICE.sendPersistent(Feature.getName(),
                                CAT_FORMAT.format("Usage: {s}" + prefix + "{g}" + Feature.getName() +
                                        " {g}" + setting.getName() + " del {s}<{g}item{s}>{reset}."));
                        return;
                    }
                    if (wlSetting.removeFromWhitelist(item)) {
                        CHAT_SERVICE.sendPersistent(Feature.getName(),
                                CAT_FORMAT.format("Removed: {g}" + item + "{reset} from {g}" +
                                        setting.getName() + "{reset}."));
                    } else {
                        CHAT_SERVICE.sendPersistent(Feature.getName(),
                                CAT_FORMAT.format("Invalid or not in list: {g}" +
                                        item + "{reset}."));
                    }
                }
                case "list" -> {
                    if (wlSetting.getWhitelist().isEmpty()) {
                        CHAT_SERVICE.sendPersistent(Feature.getName(),
                                CAT_FORMAT.format("List {g}" + setting.getName() + "{reset} is empty."));
                        return;
                    }
                    StringBuilder builder = new StringBuilder();
                    builder.append("List {g}").append(setting.getName()).append("{reset} items: ");
                    int i = 0;
                    int size = wlSetting.getWhitelist().size();
                    for (Identifier id : wlSetting.getWhitelist()) {
                        builder.append("{g}").append(id.toString()).append("{reset}.");
                        if (i < size - 1) builder.append("{s}, {reset}");
                        i++;
                    }
                    CHAT_SERVICE.sendPersistent(Feature.getName(), CAT_FORMAT.format(builder.toString()));
                }
                default -> {
                    CHAT_SERVICE.sendPersistent(Feature.getName(),
                            CAT_FORMAT.format("Unknown action: {g}" + action +
                                    "{reset}. Use {g}add/del/list{reset}."));
                }
            }
        } else if (setting instanceof BoolSetting boolSetting) {
            if (valueRaw == null) {
                boolSetting.set(!boolSetting.get());
                CHAT_SERVICE.sendPersistent(Feature.getName(),
                        CAT_FORMAT.format("{g}" + setting.getName() + "{reset} toggled to "+ (boolSetting.get() ? "{green}" : "{red}") + boolSetting.get() + "{reset} for {g}" + Feature.getName() + "{reset}."));
            } else {
                switch (valueRaw.toLowerCase()) {
                    case "true", "on" -> boolSetting.set(true);
                    case "false", "off" -> boolSetting.set(false);
                    case "toggle" -> boolSetting.set(!boolSetting.get());
                    default -> {
                        CHAT_SERVICE.sendPersistent(Feature.getName(),
                                CAT_FORMAT.format("Invalid bool value {g}" + valueRaw + "{reset}. Use {g}true/false/toggle{reset}."));
                        return;
                    }
                }
                CHAT_SERVICE.sendPersistent(Feature.getName(),
                        CAT_FORMAT.format("{g}" + setting.getName() + "{reset} set to {g}" + boolSetting.get() + "{reset}."));
            }
        } else if (setting instanceof IntSetting intSetting) {
            try {
                intSetting.set(Integer.parseInt(valueRaw));
                CHAT_SERVICE.sendPersistent(Feature.getName(),
                        CAT_FORMAT.format("{g}" + setting.getName() + "{reset} set to {g}" + intSetting.get() + "{reset}."));
            } catch (Exception e) {
                CHAT_SERVICE.sendPersistent(Feature.getName(),
                        CAT_FORMAT.format("Invalid integer {g}" + valueRaw + "{reset}."));
            }
        } else if (setting instanceof DoubleSetting doubleSetting) {
            try {
                doubleSetting.set(Double.parseDouble(valueRaw));
                CHAT_SERVICE.sendPersistent(Feature.getName(),
                        CAT_FORMAT.format("{g}" + setting.getName() + "{reset} set to {g}" + doubleSetting.get() + "{reset}."));
            } catch (Exception e) {
                CHAT_SERVICE.sendPersistent(Feature.getName(),
                        CAT_FORMAT.format("Invalid number {g}" + valueRaw + "{reset}."));
            }
        } else if (setting instanceof KeyBindSetting keyBindSetting) {
            try {
                keyBindSetting.set(Integer.parseInt(valueRaw));
                CHAT_SERVICE.sendPersistent(Feature.getName(),
                        CAT_FORMAT.format("{g}" + setting.getName() + "{reset} set to key code {g}" + keyBindSetting.get() + "{reset}."));
            } catch (Exception e) {
                CHAT_SERVICE.sendPersistent(Feature.getName(),
                        CAT_FORMAT.format("Invalid keybind {g}" + valueRaw + "{reset}. Must be int key code."));
            }
        } else if (setting instanceof EnumSetting<?> enumSetting) {
            if (valueRaw == null) {
                CHAT_SERVICE.sendPersistent(Feature.getName(),
                        CAT_FORMAT.format("{g}" + setting.getName() + "{reset} is currently {g}" + enumSetting.get().name() + "{reset}. " +
                                "Available: {g}" + String.join("{reset}, {g}",
                                Arrays.stream(enumSetting.getValues()).map(Enum::name).toList()) + "{reset}."));
                return;
            }

            if (valueRaw.equalsIgnoreCase("cycle")) {
                enumSetting.cycle(true);
                CHAT_SERVICE.sendPersistent(Feature.getName(),
                        CAT_FORMAT.format("{g}" + setting.getName() + "{reset} cycled to {g}" + enumSetting.get().name() + "{reset}."));
                return;
            }

            boolean matched = false;
            for (Enum<?> constant : enumSetting.getValues()) {
                if (constant.name().equalsIgnoreCase(valueRaw)) {
                    setEnumValue(enumSetting, constant);
                    matched = true;
                    CHAT_SERVICE.sendPersistent(Feature.getName(),
                            CAT_FORMAT.format("{g}" + setting.getName() + "{reset} set to {g}" + constant.name() + "{reset}."));
                    break;
                }
            }

            if (!matched) {
                CHAT_SERVICE.sendPersistent(Feature.getName(),
                        CAT_FORMAT.format("Invalid value {g}" + valueRaw + "{reset}. Available: {g}" +
                                String.join(", ", Arrays.stream(enumSetting.getValues()).map(Enum::name).toList()) + "{reset}."));
            }
        }

        else {
            CHAT_SERVICE.sendPersistent(Feature.getName(),
                    CAT_FORMAT.format("Unsupported setting type for {g}" + setting.getName() + "{reset}."));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"}) // god i love theese compiler errors in clear java
    private static <E extends Enum<E>> void setEnumValue(EnumSetting<?> setting, Enum<?> value) {
        ((EnumSetting) setting).set(value);
    }

    public static void registerFeatureCommands(CommandStorage storage) {
        FEATURE_SERVICE.getStorage().getAll().forEach(Feature -> {
            try {
                String name = Feature.getName().replace(" ", "");
                if (storage.getCommandByNameOrAlias(name) == null) {
                    storage.addCommand(new FeatureCommand(Feature));
                }
            } catch (Exception e) {
                API_LOGGER.error("Failed to initiate command for Feature: " + Feature.getName(), e);
            }
        });
    }
}
