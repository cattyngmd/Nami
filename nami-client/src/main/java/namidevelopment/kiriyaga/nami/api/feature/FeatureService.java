package namidevelopment.kiriyaga.nami.api.feature;

import namidevelopment.kiriyaga.nami.impl.feature.Feature;

import static namidevelopment.kiriyaga.nami.Nami.LOGGER;

public class FeatureService {

    private final FeatureStorage storage = new FeatureStorage();

    public void init() {
        FeatureRegistry.registerAnnotatedFeatures(storage);
        LOGGER.info("Registered " + storage.size() + " Features.");
    }

    public FeatureStorage getStorage() {
        return storage;
    }

    public void registerFeature(Feature Feature) {
        storage.add(Feature);
    }

    public void unregisterFeature(Feature Feature) {
        storage.remove(Feature);
    }
}
