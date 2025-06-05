package me.kiriyaga.essentials.feature.command;

import me.kiriyaga.essentials.Essentials;
import net.minecraft.text.Text;

import static me.kiriyaga.essentials.Essentials.NAME;
import static me.kiriyaga.essentials.Essentials.MINECRAFT;

public abstract class Command {
    protected final String name;
    protected final String[] aliases;
    protected final String description;

    public Command(String name, String description, String... aliases) {
        this.name = name.toLowerCase();
        this.description = description;
        this.aliases = aliases;
    }

    public boolean matches(String input) {
        String lower = input.toLowerCase();
        if (lower.equals(name)) return true;
        for (String alias : aliases) {
            if (lower.equals(alias)) return true;
        }
        return false;
    }

    public abstract void execute(String[] args);

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String[] getAliases() {
        return aliases;
    }
}
