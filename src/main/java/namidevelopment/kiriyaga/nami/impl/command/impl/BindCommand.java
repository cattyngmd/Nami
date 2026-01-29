package namidevelopment.kiriyaga.nami.impl.command.impl;

import namidevelopment.kiriyaga.nami.impl.command.Command;
import namidevelopment.kiriyaga.nami.impl.command.RegisterCommand;
import namidevelopment.kiriyaga.nami.impl.command.CommandArgument;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.KeyBindSetting;
import namidevelopment.kiriyaga.nami.util.KeyUtils;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;

import static namidevelopment.kiriyaga.nami.Nami.*;

@RegisterCommand
public class BindCommand extends Command {

    public BindCommand() {
        super(
                "bind",
                new CommandArgument[] {
                        new CommandArgument.FeatureArg("Feature"),
                        new CommandArgument.KeyBindArg("key")
                },
                "b"
        );
    }

    @Override
    public void execute(Object[] parsedArgs) {
        String FeatureName = ((String) parsedArgs[0]);
        String keyName = ((String) parsedArgs[1]).toUpperCase();

        Feature Feature = FEATURE_SERVICE.getStorage().getAll().stream()
                .filter(m -> m.getName().equalsIgnoreCase(FeatureName) || m.matches(FeatureName))
                .findFirst()
                .orElse(null);

        if (Feature == null) {
            Component message = CAT_FORMAT.format("Feature {g}" + FeatureName + " {reset}not found.");
            CHAT_SERVICE.sendPersistent(BindCommand.class.getName(), message);
            return;
        }

        KeyBindSetting bindSetting = Feature.getSettings().stream()
                .filter(s -> s instanceof KeyBindSetting)
                .map(s -> (KeyBindSetting) s)
                .findFirst()
                .orElse(null);

        if (bindSetting == null) {
            Component message = CAT_FORMAT.format("Feature {g}" + FeatureName + " {reset}does not have a keybind setting.");
            CHAT_SERVICE.sendPersistent(BindCommand.class.getName(), message);
            return;
        }

        int keyCode = KeyUtils.parseKey(keyName);

        if (keyCode == -1) {
            Component message = CAT_FORMAT.format("Invalid key name: {g}" + keyName + "{reset}.");
            CHAT_SERVICE.sendPersistent(BindCommand.class.getName(), message);
            return;
        }

        InputConstants.Key key = InputConstants.Type.KEYSYM.getOrCreate(keyCode);
        if (key == null) {
            Component message = CAT_FORMAT.format("Invalid key code: {g}" + keyCode + "{reset}.");
            CHAT_SERVICE.sendPersistent(BindCommand.class.getName(), message);
            return;
        }

        bindSetting.set(keyCode);

        Component message = CAT_FORMAT.format("Bound Feature {g}" + Feature.getName() + " {reset}to key {g}" + keyName + "{reset}.");
        CHAT_SERVICE.sendPersistent(BindCommand.class.getName(), message);
    }
}
