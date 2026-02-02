package namidevelopment.kiriyaga.api.api.config;

import static namidevelopment.kiriyaga.api.NamiApi.*;

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
