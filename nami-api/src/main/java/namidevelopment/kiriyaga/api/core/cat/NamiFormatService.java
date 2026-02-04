package namidevelopment.kiriyaga.api.core.cat;

import net.minecraft.ChatFormatting;

public class NamiFormatService extends NamiFormat {

    public NamiFormatService() {
        this(true);
    }

    public NamiFormatService(boolean vanilla) {
        super();
        if (vanilla) {
            addVanilla();
        }
    }

    private void addVanilla() {
        for (ChatFormatting value : ChatFormatting.values()) {
            if (!value.isColor() || value.getColor() == null) continue;

            add(value.getName(), value.getColor());
        }
    }
}
