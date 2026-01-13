package me.kiriyaga.nami.core.config;

import me.kiriyaga.nami.feature.setting.Setting;
import me.kiriyaga.nami.feature.setting.impl.ColorSetting;
import me.kiriyaga.nami.feature.setting.impl.KeyBindSetting;

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
