package namidevelopment.kiriyaga.api.model.plugin;

import namidevelopment.kiriyaga.api.model.command.Command;
import namidevelopment.kiriyaga.api.model.feature.Feature;

import java.util.ArrayList;
import java.util.List;

public class Plugin {

    private final int id;
    private final String name;
    private final String version;
    private final String authors;
    private boolean enabled = false;
    private final List<Feature> registeredFeatures = new ArrayList<>();
    private final List<Command> registeredCommands = new ArrayList<>();

    public Plugin(int id, String name, String version, String authors) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.authors = authors;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getAuthors() { return authors; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public List<Feature> getRegisteredFeatures() { return registeredFeatures; }
    public List<Command> getRegisteredCommands() { return registeredCommands; }
}
