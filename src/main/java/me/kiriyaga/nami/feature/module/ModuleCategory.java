package me.kiriyaga.nami.feature.module;

import java.util.*;

public class ModuleCategory {
    private static final Map<String, ModuleCategory> CATEGORIES = new HashMap<>();

    private final String name;

    private ModuleCategory(String name) {
        this.name = name.toLowerCase();
    }

    public static ModuleCategory of(String name) {
        return CATEGORIES.computeIfAbsent(name.toLowerCase(), ModuleCategory::new);
    }

    public static Collection<ModuleCategory> getAll() {
        return CATEGORIES.values();
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
