package namidevelopment.kiriyaga.nami.api.config;

import namidevelopment.kiriyaga.nami.impl.setting.Setting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.ColorSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.KeyBindSetting;

public enum ConfigMode {
    SETTINGS {
        @Override
        public boolean accept(Setting<?> setting) {
            return !(setting instanceof KeyBindSetting)
                    && !(setting instanceof ColorSetting);
        }
    },
    KEYBIND {
        @Override
        public boolean accept(Setting<?> setting) {
            return setting instanceof KeyBindSetting;
        }
    },
    COLOR {
        @Override
        public boolean accept(Setting<?> setting) {
            return setting instanceof ColorSetting;
        }
    },
    ALL {
        @Override
        public boolean accept(Setting<?> setting) {
            return true;
        }
    };

    public abstract boolean accept(Setting<?> setting);
}
