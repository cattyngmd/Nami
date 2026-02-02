package namidevelopment.kiriyaga.api.core.feature;

import namidevelopment.kiriyaga.api.model.feature.Feature;

import static namidevelopment.kiriyaga.api.NamiApi.*;


public class FeatureService {

    private final FeatureStorage storage = new FeatureStorage();

    public void init() {
        FeatureRegistry.registerAnnotatedFeatures(storage);
        API_LOGGER.info("Registered " + storage.size() + " Features.");
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
