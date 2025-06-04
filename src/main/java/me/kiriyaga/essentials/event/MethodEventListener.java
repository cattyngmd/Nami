package me.kiriyaga.essentials.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodEventListener<T extends Event> implements EventListener<T> {
    private final Object target;
    private final Method method;
    private final EventPriority priority;

    public MethodEventListener(Object target, Method method, EventPriority priority) {
        this.target = target;
        this.method = method;
        this.priority = priority;
        this.method.setAccessible(true);
    }

    public Object getTarget() {
        return target;
    }

    public EventPriority getPriority() {
        return priority;
    }

    @Override
    public void onEvent(T event) {
        try {
            method.invoke(target, event);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}

