package me.kiriyaga.nami.core.module;

import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ModuleRegistry {

    private static final String MODULE_PACKAGE = "me.kiriyaga.nami.feature.module.impl";

    public static void registerAnnotatedModules(ModuleStorage storage) {
        Set<Class<? extends Module>> classes = findAnnotatedModuleClasses();

        for (Class<? extends Module> clazz : classes) {
            try {
                Module module = clazz.getDeclaredConstructor().newInstance();
                storage.add(module);
            } catch (Exception e) {
                System.err.println("Failed to instantiate module: " + clazz.getName());
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Set<Class<? extends Module>> findAnnotatedModuleClasses() {
        Set<Class<? extends Module>> result = new HashSet<>();

        try {
            String path = MODULE_PACKAGE.replace('.', '/');
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);

            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                String filePath = URLDecoder.decode(url.getFile(), "UTF-8");

                if (filePath.startsWith("file:") && filePath.contains("!")) {
                    String[] split = filePath.split("!");
                    try (JarFile jar = new JarFile(split[0].substring("file:".length()))) {
                        result.addAll(scanJar(jar));
                    }
                } else {
                    File dir = new File(filePath);
                    if (dir.exists() && dir.isDirectory()) {
                        result.addAll(scanDirectory(dir, MODULE_PACKAGE));
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static Set<Class<? extends Module>> scanDirectory(File dir, String packageName) throws ClassNotFoundException {
        Set<Class<? extends Module>> result = new HashSet<>();

        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                result.addAll(scanDirectory(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().replace(".class", "");
                Class<?> cls = Class.forName(className);

                if (Module.class.isAssignableFrom(cls) && cls.isAnnotationPresent(RegisterModule.class)) {
                    result.add((Class<? extends Module>) cls);
                }
            }
        }

        return result;
    }

    private static Set<Class<? extends Module>> scanJar(JarFile jar) throws ClassNotFoundException {
        Set<Class<? extends Module>> result = new HashSet<>();

        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();

            if (name.endsWith(".class") && name.startsWith(MODULE_PACKAGE.replace('.', '/'))) {
                String className = name.replace('/', '.').replace(".class", "");
                Class<?> cls = Class.forName(className);

                if (Module.class.isAssignableFrom(cls) && cls.isAnnotationPresent(RegisterModule.class)) {
                    result.add((Class<? extends Module>) cls);
                }
            }
        }

        return result;
    }
}
