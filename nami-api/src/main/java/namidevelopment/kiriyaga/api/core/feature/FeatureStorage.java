package namidevelopment.kiriyaga.api.core.feature;

import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;

import static namidevelopment.kiriyaga.api.NamiApi.*;

import java.util.*;

public class FeatureStorage {

    private final List<Feature> Features = new ArrayList<>();
    private final Map<String, Feature> FeaturesByName = new HashMap<>();
    private final Map<Class<? extends Feature>, Feature> FeaturesByClass = new HashMap<>();

    public void add(Feature Feature) {
        Features.add(Feature);
        FeaturesByName.put(Feature.getName(), Feature);
        FeaturesByName.put(Feature.getName().replace(" ", ""), Feature);
        FeaturesByClass.put(Feature.getClass(), Feature);
    }

    public void remove(Feature Feature) {
        Features.remove(Feature);
        FeaturesByName.remove(Feature.getName());
        FeaturesByName.remove(Feature.getName().replace(" ", ""));
        FeaturesByClass.remove(Feature.getClass());
        Feature.setEnabled(false);
    }

    public List<Feature> getAll() {
        return Features;
    }

    public <T extends Feature> T getByClass(Class<T> clazz) {
        return clazz.cast(FeaturesByClass.get(clazz));
    }

    public Feature getByName(String name) {
        if (name == null) return null;
        String lower = name;
        Feature m = FeaturesByName.get(lower);
        if (m != null) return m;
        return FeaturesByName.get(lower.replace(" ", ""));
    }

    public List<Feature> getByCategory(FeatureCategory category) {
        return Features.stream()
                .filter(m -> m.getCategory().equals(category))
                .sorted(Comparator.comparing(Feature::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public int size() {
        return Features.size();
    }
}