package me.kiriyaga.nami.feature.gui.newgui.base;

import net.minecraft.text.Text;

public abstract class BaseEntry {
    protected Text displayText;

    public abstract Text getDisplayText();

    public abstract void refreshEntry();
}
