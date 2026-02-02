package namidevelopment.kiriyaga.nami.impl.feature;

import java.util.*;

public class FeatureCategory {
    private static final Map<String, FeatureCategory> CATEGORIES = new LinkedHashMap<>();

    private static final List<String> FIXED_ORDER = List.of(
            "Combat", "Exploits", "Miscellaneous", "Movement", "Render", "World", "HUD", "Client"
    );

    private final String name;

    private FeatureCategory(String name) {
        this.name = name;
    }

    public static FeatureCategory of(String name) {
        return CATEGORIES.computeIfAbsent(name, FeatureCategory::new);
    }

    public static List<FeatureCategory> getAll() {
        List<FeatureCategory> sorted = new ArrayList<>();

        Set<String> added = new HashSet<>();

        for (String key : FIXED_ORDER) {
            FeatureCategory cat = CATEGORIES.get(key);
            if (cat != null) {
                sorted.add(cat);
                added.add(cat.name);
            }
        }

        for (FeatureCategory cat : CATEGORIES.values()) {
            if (!added.contains(cat.name)) {
                sorted.add(cat);
            }
        }

        return sorted;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}