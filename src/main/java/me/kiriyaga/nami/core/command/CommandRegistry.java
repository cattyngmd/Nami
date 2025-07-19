package me.kiriyaga.nami.core.command;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class CommandRegistry {

    private static final String COMMAND_PACKAGE = "me.kiriyaga.nami.feature.command.impl";

    public static void registerAnnotatedCommands(CommandStorage storage) {
        Set<Class<? extends Command>> classes = findAnnotatedCommandClasses();

        for (Class<? extends Command> clazz : classes) {
            try {
                Command command = clazz.getDeclaredConstructor().newInstance();
                storage.addCommand(command);
            } catch (Exception e) {
                System.err.println("Failed to instantiate command: " + clazz.getName());
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Set<Class<? extends Command>> findAnnotatedCommandClasses() {
        Set<Class<? extends Command>> result = new HashSet<>();

        try {
            String path = COMMAND_PACKAGE.replace('.', '/');
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
                        result.addAll(scanDirectory(dir, COMMAND_PACKAGE));
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static Set<Class<? extends Command>> scanDirectory(File dir, String packageName) throws ClassNotFoundException {
        Set<Class<? extends Command>> result = new HashSet<>();

        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                result.addAll(scanDirectory(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().replace(".class", "");
                Class<?> cls = Class.forName(className);

                if (Command.class.isAssignableFrom(cls) && cls.isAnnotationPresent(RegisterCommand.class)) {
                    result.add((Class<? extends Command>) cls);
                }
            }
        }

        return result;
    }

    private static Set<Class<? extends Command>> scanJar(JarFile jar) throws ClassNotFoundException {
        Set<Class<? extends Command>> result = new HashSet<>();

        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();

            if (name.endsWith(".class") && name.startsWith(COMMAND_PACKAGE.replace('.', '/'))) {
                String className = name.replace('/', '.').replace(".class", "");
                Class<?> cls = Class.forName(className);

                if (Command.class.isAssignableFrom(cls) && cls.isAnnotationPresent(RegisterCommand.class)) {
                    result.add((Class<? extends Command>) cls);
                }
            }
        }

        return result;
    }
}
