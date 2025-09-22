package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.setting.Setting;
import me.kiriyaga.nami.feature.setting.impl.*;
import net.minecraft.util.Identifier;

import java.util.Arrays;

import static me.kiriyaga.nami.Nami.*;

public class ModuleCommand extends Command {
    private final Module module;

    public ModuleCommand(Module module) {
        super(
                module.getName().replace(" ", ""),
                new CommandArgument[]{
                        new CommandArgument.SettingArg("setting"),
                        new CommandArgument.StringArg("value", 1, 256) {
                            @Override
                            public boolean isRequired() { return false; }
                        }
                }
        );
        this.module = module;
    }

    @Override
    public void execute(Object[] parsedArgs) {
        String prefix = COMMAND_MANAGER.getExecutor().getPrefix();

        String settingNameRaw = ((String) parsedArgs[0]);
        String valueRaw = parsedArgs.length > 1 ? (String) parsedArgs[1] : null;

        String settingName = settingNameRaw.replace(" ", "");

        Setting<?> setting = null;
        for (Setting<?> s : module.getSettings()) {
            if (s.getName().replace(" ", "").equalsIgnoreCase(settingName)) {
                setting = s;
                break;
            }
        }

        if (setting == null) {
            CHAT_MANAGER.sendPersistent(module.getName(),
                    CAT_FORMAT.format("Setting {g}" + settingNameRaw + "{reset} not found in module {g}" + module.getName() + "{reset}."));
            return;
        }

        if (setting instanceof BoolSetting boolSetting) {
            if (valueRaw == null) {
                boolSetting.set(!boolSetting.get());
                CHAT_MANAGER.sendPersistent(module.getName(),
                        CAT_FORMAT.format("{g}" + setting.getName() + "{reset} toggled to {g}" + boolSetting.get() + "{reset}."));
            } else {
                switch (valueRaw.toLowerCase()) {
                    case "true", "on" -> boolSetting.set(true);
                    case "false", "off" -> boolSetting.set(false);
                    case "toggle" -> boolSetting.set(!boolSetting.get());
                    default -> {
                        CHAT_MANAGER.sendPersistent(module.getName(),
                                CAT_FORMAT.format("Invalid bool value {g}" + valueRaw + "{reset}. Use {g}true/false/toggle{reset}."));
                        return;
                    }
                }
                CHAT_MANAGER.sendPersistent(module.getName(),
                        CAT_FORMAT.format("{g}" + setting.getName() + "{reset} set to {g}" + boolSetting.get() + "{reset}."));
            }
        } else if (setting instanceof IntSetting intSetting) {
            try {
                intSetting.set(Integer.parseInt(valueRaw));
                CHAT_MANAGER.sendPersistent(module.getName(),
                        CAT_FORMAT.format("{g}" + setting.getName() + "{reset} set to {g}" + intSetting.get() + "{reset}."));
            } catch (Exception e) {
                CHAT_MANAGER.sendPersistent(module.getName(),
                        CAT_FORMAT.format("Invalid integer {g}" + valueRaw + "{reset}."));
            }
        } else if (setting instanceof DoubleSetting doubleSetting) {
            try {
                doubleSetting.set(Double.parseDouble(valueRaw));
                CHAT_MANAGER.sendPersistent(module.getName(),
                        CAT_FORMAT.format("{g}" + setting.getName() + "{reset} set to {g}" + doubleSetting.get() + "{reset}."));
            } catch (Exception e) {
                CHAT_MANAGER.sendPersistent(module.getName(),
                        CAT_FORMAT.format("Invalid number {g}" + valueRaw + "{reset}."));
            }
        } else if (setting instanceof KeyBindSetting keyBindSetting) {
            try {
                keyBindSetting.set(Integer.parseInt(valueRaw));
                CHAT_MANAGER.sendPersistent(module.getName(),
                        CAT_FORMAT.format("{g}" + setting.getName() + "{reset} set to key code {g}" + keyBindSetting.get() + "{reset}."));
            } catch (Exception e) {
                CHAT_MANAGER.sendPersistent(module.getName(),
                        CAT_FORMAT.format("Invalid keybind {g}" + valueRaw + "{reset}. Must be int key code."));
            }
        } else if (setting instanceof WhitelistSetting wlSetting) {
            if (valueRaw == null) {
                CHAT_MANAGER.sendPersistent(module.getName(),
                        CAT_FORMAT.format("Usage: {s}" + prefix + "{g}" + module.getName() +
                                " {g}" + setting.getName() + " add/del/list {s}[item]{reset}"));
                return;
            }

            String[] parts = valueRaw.split("\\s+", 2);
            String action = parts[0].toLowerCase();
            String item = parts.length > 1 ? parts[1].toLowerCase().replace("minecraft:", "").trim() : null;

            switch (action) {
                case "add" -> {
                    if (item == null || item.isEmpty()) {
                        CHAT_MANAGER.sendPersistent(module.getName(),
                                CAT_FORMAT.format("Usage: {s}" + prefix + "{g}" + module.getName() +
                                        " {g}" + setting.getName() + " add {s}<{g}item{s}>{reset}"));
                        return;
                    }
                    if (wlSetting.addToWhitelist(item)) {
                        CHAT_MANAGER.sendPersistent(module.getName(),
                                CAT_FORMAT.format("Added: {g}minecraft:" + item + "{reset} to {g}" +
                                        setting.getName() + "{reset}."));
                    } else {
                        CHAT_MANAGER.sendPersistent(module.getName(),
                                CAT_FORMAT.format("Invalid item id or already added: {g}minecraft:" +
                                        item + "{reset}."));
                    }
                }
                case "del" -> {
                    if (item == null || item.isEmpty()) {
                        CHAT_MANAGER.sendPersistent(module.getName(),
                                CAT_FORMAT.format("Usage: {s}" + prefix + "{g}" + module.getName() +
                                        " {g}" + setting.getName() + " del {s}<{g}item{s}>{reset}"));
                        return;
                    }
                    if (wlSetting.removeFromWhitelist(item)) {
                        CHAT_MANAGER.sendPersistent(module.getName(),
                                CAT_FORMAT.format("Removed: {g}minecraft:" + item + "{reset} from {g}" +
                                        setting.getName() + "{reset}."));
                    } else {
                        CHAT_MANAGER.sendPersistent(module.getName(),
                                CAT_FORMAT.format("Invalid or not in list: {g}minecraft:" +
                                        item + "{reset}."));
                    }
                }
                case "list" -> {
                    if (wlSetting.getWhitelist().isEmpty()) {
                        CHAT_MANAGER.sendPersistent(module.getName(),
                                CAT_FORMAT.format("List {g}" + setting.getName() + "{reset} is empty."));
                        return;
                    }
                    StringBuilder builder = new StringBuilder();
                    builder.append("List {g}").append(setting.getName()).append("{reset} items: ");
                    int i = 0;
                    int size = wlSetting.getWhitelist().size();
                    for (Identifier id : wlSetting.getWhitelist()) {
                        builder.append("{g}").append(id.toString()).append("{reset}");
                        if (i < size - 1) builder.append("{s}, {reset}");
                        i++;
                    }
                    CHAT_MANAGER.sendPersistent(module.getName(), CAT_FORMAT.format(builder.toString()));
                }
                default -> {
                    CHAT_MANAGER.sendPersistent(module.getName(),
                            CAT_FORMAT.format("Unknown action: {g}" + action +
                                    "{reset}. Use {g}add/del/list{reset}."));
                }
            }
        }else if (setting instanceof EnumSetting<?> enumSetting) {
            if (valueRaw == null) {
                CHAT_MANAGER.sendPersistent(module.getName(),
                        CAT_FORMAT.format("{g}" + setting.getName() + "{reset} is currently {g}" + enumSetting.get().name() + "{reset}. " +
                                "Available: {g}" + String.join("{reset}, {g}",
                                Arrays.stream(enumSetting.getValues()).map(Enum::name).toList()) + "{reset}."));
                return;
            }

            if (valueRaw.equalsIgnoreCase("cycle")) {
                enumSetting.cycle(true);
                CHAT_MANAGER.sendPersistent(module.getName(),
                        CAT_FORMAT.format("{g}" + setting.getName() + "{reset} cycled to {g}" + enumSetting.get().name() + "{reset}."));
                return;
            }

            boolean matched = false;
            for (Enum<?> constant : enumSetting.getValues()) {
                if (constant.name().equalsIgnoreCase(valueRaw)) {
                    setEnumValue(enumSetting, constant);
                    matched = true;
                    CHAT_MANAGER.sendPersistent(module.getName(),
                            CAT_FORMAT.format("{g}" + setting.getName() + "{reset} set to {g}" + constant.name() + "{reset}."));
                    break;
                }
            }

            if (!matched) {
                CHAT_MANAGER.sendPersistent(module.getName(),
                        CAT_FORMAT.format("Invalid value {g}" + valueRaw + "{reset}. Available: {g}" +
                                String.join(", ", Arrays.stream(enumSetting.getValues()).map(Enum::name).toList()) + "{reset}."));
            }
        }

        else {
            CHAT_MANAGER.sendPersistent(module.getName(),
                    CAT_FORMAT.format("Unsupported setting type for {g}" + setting.getName() + "{reset}."));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"}) // god i love theese compiler errors in clear java
    private static <E extends Enum<E>> void setEnumValue(EnumSetting<?> setting, Enum<?> value) {
        ((EnumSetting) setting).set(value);
    }
}
