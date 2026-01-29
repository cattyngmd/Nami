package me.kiriyaga.nami.api.feature;

import me.kiriyaga.nami.impl.feature.Feature;
import me.kiriyaga.nami.impl.feature.RegisterFeature;
import me.kiriyaga.nami.util.ClasspathScanner;

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
