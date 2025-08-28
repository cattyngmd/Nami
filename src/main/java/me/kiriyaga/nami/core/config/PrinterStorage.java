package me.kiriyaga.nami.core.config;

import com.google.gson.*;
import me.kiriyaga.nami.core.config.model.PrinterSchematic;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PrinterStorage {
    private final File dir;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public PrinterStorage(ConfigDirectoryProvider dirs) {
        this.dir = new File(dirs.getBaseDir(), "printer");
        if (!dir.exists()) dir.mkdirs();
    }

    public void save(String name, PrinterSchematic schematic) {
        try (FileWriter writer = new FileWriter(new File(dir, name + ".json"), StandardCharsets.UTF_8)) {
            gson.toJson(schematic.toJson(), writer); // 111
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PrinterSchematic load(String name) {
        File file = new File(dir, name + ".json");
        if (!file.exists()) return null;

        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
            return PrinterSchematic.fromJson(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> list() {
        List<String> names = new ArrayList<>();
        if (!dir.exists()) return names;

        for (String file : Objects.requireNonNull(dir.list())) {
            if (file.endsWith(".json")) {
                names.add(file.replace(".json", ""));
            }
        }
        return names;
    }
}
