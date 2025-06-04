package me.kiriyaga.essentials.manager;

import me.kiriyaga.essentials.event.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventManager {

    private final Map<Class<? extends Event>, List<MethodEventListener<? extends Event>>> listeners = new ConcurrentHashMap<>();

    public void register(Object listenerObject) {
        for (Method method : listenerObject.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(SubscribeEvent.class)) continue;
            if (method.getParameterCount() != 1) continue;

            Class<?> paramType = method.getParameterTypes()[0];
            if (!Event.class.isAssignableFrom(paramType)) continue;

            @SuppressWarnings("unchecked")
            Class<? extends Event> eventClass = (Class<? extends Event>) paramType;

            SubscribeEvent annotation = method.getAnnotation(SubscribeEvent.class);
            EventPriority priority = annotation.priority();

            method.setAccessible(true);
            MethodEventListener<? extends Event> methodListener = new MethodEventListener<>(listenerObject, method, priority);

            List<MethodEventListener<? extends Event>> lst = listeners.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>());
            lst.add(methodListener);
            lst.sort(Comparator.comparing((MethodEventListener<? extends Event> listener) -> listener.getPriority()).reversed()); // im fucking in love with java lmao
        }
    }

    public void unregister(Object listenerObject) {
        for (List<MethodEventListener<? extends Event>> lst : listeners.values()) {
            lst.removeIf(listener -> listener.getTarget() == listenerObject);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Event> void post(T event) {
        List<MethodEventListener<? extends Event>> lst = listeners.get(event.getClass());
        if (lst == null) return;

        for (MethodEventListener<? extends Event> listener : lst) {
            try {
                ((MethodEventListener<T>) listener).onEvent(event);
                if (event.isCancelled()) break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
