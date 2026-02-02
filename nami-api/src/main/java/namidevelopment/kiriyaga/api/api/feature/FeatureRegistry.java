package namidevelopment.kiriyaga.api.api.feature;

import static namidevelopment.kiriyaga.api.NamiApi.*;


import java.util.*;

public class FeatureRegistry {

        public static void registerAnnotatedFeatures(FeatureStorage storage) {
            Set<Class<? extends Feature>> classes = ClasspathScanner.findAnnotated(Feature.class, RegisterFeature.class);
            for (Class<? extends Feature> clazz : classes) {
                try {
                    Feature Feature = clazz.getDeclaredConstructor().newInstance();
                    storage.add(Feature);
                } catch (Exception e) {
                    System.err.println("Failed to instantiate Feature: " + clazz.getName());
                    e.printStackTrace();
                }
            }
        }
}
