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
                    CAT_FORMAT.format("{gray}Setting {global}" + settingNameRaw + "{gray} not found in Feature {global}" + Feature.getName() + "{gray}."));
            return;
        }

        if (setting instanceof WhitelistSetting wlSetting) {
            String action = parsedArgs.length > 1 ? ((String) parsedArgs[1]).toLowerCase() : null;
            String item = parsedArgs.length > 2 ? ((String) parsedArgs[2]) : null;

            if (action == null) {
                CHAT_SERVICE.sendPersistent(Feature.getName(),
                        CAT_FORMAT.format("{gray}Usage: {secondary}" + prefix + "{global}" + Feature.getName() +
                                " {global}" + setting.getName() + " add{gray}/{global}del{gray}/{global}list {global}[item]{gray}."));
                return;
            }

            switch (action) {
                case "add" -> {
                    if (item == null || item.isEmpty()) {
                        CHAT_SERVICE.sendPersistent(Feature.getName(),
                                CAT_FORMAT.format("{gray}Usage: {secondary}" + prefix + "{global}" + Feature.getName() +
                                        " {global}" + setting.getName() + " add {secondary}<{global}item{secondary}>{gray}."));
                        return;
                    }
                    if (wlSetting.addToWhitelist(item)) {
                        CHAT_SERVICE.sendPersistent(Feature.getName(),
                                CAT_FORMAT.format("{gray}Added: {global}" + item + "{gray} to {global}" +
                                        setting.getName() + "{gray}."));
                    } else {
                        CHAT_SERVICE.sendPersistent(Feature.getName(),
                                CAT_FORMAT.format("{gray}Invalid item id or already added: {global}" +
                                        item + "{gray}."));
                    }
                }
                case "del" -> {
                    if (item == null || item.isEmpty()) {
                        CHAT_SERVICE.sendPersistent(Feature.getName(),
                                CAT_FORMAT.format("{gray}Usage: {secondary}" + prefix + "{global}" + Feature.getName() +
                                        " {global}" + setting.getName() + " del {secondary}<{global}item{secondary}>{gray}."));
                        return;
                    }
                    if (wlSetting.removeFromWhitelist(item)) {
                        CHAT_SERVICE.sendPersistent(Feature.getName(),
                                CAT_FORMAT.format("{gray}Removed: {global}" + item + "{gray} from {global}" +
                                        setting.getName() + "{gray}."));
                    } else {
                        CHAT_SERVICE.sendPersistent(Feature.getName(),
                                CAT_FORMAT.format("{gray}Invalid or not in list: {global}" +
                                        item + "{gray}."));
                    }
                }
                case "list" -> {
                    if (wlSetting.getWhitelist().isEmpty()) {
                        CHAT_SERVICE.sendPersistent(Feature.getName(),
                                CAT_FORMAT.format("{gray}List {global}" + setting.getName() + "{gray} is empty."));
                        return;
                    }
                    StringBuilder builder = new StringBuilder();
                    builder.append("{gray}List {global}").append(setting.getName()).append("{gray} items: ");
                    int i = 0;
                    int size = wlSetting.getWhitelist().size();
                    for (Identifier id : wlSetting.getWhitelist()) {
                        builder.append("{global}").append(id.toString()).append("{gray}.");
                        if (i < size - 1) builder.append("{secondary}, {gray}");
                        i++;
                    }
                    CHAT_SERVICE.sendPersistent(Feature.getName(), CAT_FORMAT.format(builder.toString()));
                }
                default -> {
                    CHAT_SERVICE.sendPersistent(Feature.getName(),
                            CAT_FORMAT.format("{gray}Unknown action: {global}" + action +
                                    "{gray}. Use {global}add/del/list{gray}."));
                }
            }
        } else if (setting instanceof BoolSetting boolSetting) {
            if (valueRaw == null) {
                boolSetting.set(!boolSetting.get());
                CHAT_SERVICE.sendPersistent(Feature.getName(),
                        CAT_FORMAT.format("{global}" + setting.getName() + "{gray} toggled to "+ (boolSetting.get() ? "{green}" : "{red}") + boolSetting.get() + "{gray} for {global}" + Feature.getName() + "{gray}."));
            } else {
                switch (valueRaw.toLowerCase()) {
                    case "true", "on" -> boolSetting.set(true);
                    case "false", "off" -> boolSetting.set(false);
                    case "toggle" -> boolSetting.set(!boolSetting.get());
                    default -> {
                        CHAT_SERVICE.sendPersistent(Feature.getName(),
                                CAT_FORMAT.format("{gray}Invalid bool value {global}" + valueRaw + "{gray}. Use {global}true/false/toggle{gray}."));
                        return;
                    }
                }
                CHAT_SERVICE.sendPersistent(Feature.getName(),
                        CAT_FORMAT.format("{global}" + setting.getName() + "{gray} set to {global}" + boolSetting.get() + "{gray}."));
            }
        } else if (setting instanceof IntSetting intSetting) {
            try {
                intSetting.set(Integer.parseInt(valueRaw));
                CHAT_SERVICE.sendPersistent(Feature.getName(),
                        CAT_FORMAT.format("{global}" + setting.getName() + "{gray} set to {global}" + intSetting.get() + "{gray}."));
            } catch (Exception e) {
                CHAT_SERVICE.sendPersistent(Feature.getName(),
                        CAT_FORMAT.format("{gray}Invalid integer {global}" + valueRaw + "{gray}."));
            }
        } else if (setting instanceof DoubleSetting doubleSetting) {
            try {
                doubleSetting.set(Double.parseDouble(valueRaw));
                CHAT_SERVICE.sendPersistent(Feature.getName(),
                        CAT_FORMAT.format("{global}" + setting.getName() + "{gray} set to {global}" + doubleSetting.get() + "{gray}."));
            } catch (Exception e) {
                CHAT_SERVICE.sendPersistent(Feature.getName(),
                        CAT_FORMAT.format("{gray}Invalid number {global}" + valueRaw + "{gray}."));
            }
        } else if (setting instanceof KeyBindSetting keyBindSetting) {
            try {
                keyBindSetting.set(Integer.parseInt(valueRaw));
                CHAT_SERVICE.sendPersistent(Feature.getName(),
                        CAT_FORMAT.format("{global}" + setting.getName() + "{gray} set to key code {global}" + keyBindSetting.get() + "{gray}."));
            } catch (Exception e) {
                CHAT_SERVICE.sendPersistent(Feature.getName(),
                        CAT_FORMAT.format("{gray}Invalid keybind {global}" + valueRaw + "{gray}. Must be int key code."));
            }
        } else if (setting instanceof EnumSetting<?> enumSetting) {
            if (valueRaw == null) {
                CHAT_SERVICE.sendPersistent(Feature.getName(),
                        CAT_FORMAT.format("{global}" + setting.getName() + "{gray} is currently {global}" + enumSetting.get().name() + "{gray}. " +
                                "Available: {global}" + String.join("{gray}, {global}",
                                Arrays.stream(enumSetting.getValues()).map(Enum::name).toList()) + "{gray}."));
                return;
            }

            if (valueRaw.equalsIgnoreCase("cycle")) {
                enumSetting.cycle(true);
                CHAT_SERVICE.sendPersistent(Feature.getName(),
                        CAT_FORMAT.format("{global}" + setting.getName() + "{gray} cycled to {global}" + enumSetting.get().name() + "{gray}."));
                return;
            }

            boolean matched = false;
            for (Enum<?> constant : enumSetting.getValues()) {
                if (constant.name().equalsIgnoreCase(valueRaw)) {
                    setEnumValue(enumSetting, constant);
                    matched = true;
                    CHAT_SERVICE.sendPersistent(Feature.getName(),
                            CAT_FORMAT.format("{global}" + setting.getName() + "{gray} set to {global}" + constant.name() + "{gray}."));
                    break;
                }
            }

            if (!matched) {
                CHAT_SERVICE.sendPersistent(Feature.getName(),
                        CAT_FORMAT.format("{gray}Invalid value {global}" + valueRaw + "{gray}. Available: {global}" +
                                String.join(", ", Arrays.stream(enumSetting.getValues()).map(Enum::name).toList()) + "{gray}."));
            }
        }

        else {
            CHAT_SERVICE.sendPersistent(Feature.getName(),
                    CAT_FORMAT.format("{gray}Unsupported setting type for {global}" + setting.getName() + "{gray}."));
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
                API_LOGGER.error("{gray}Failed to initiate command for Feature: {global}" + Feature.getName(), e);
            }
        });
    }
}
