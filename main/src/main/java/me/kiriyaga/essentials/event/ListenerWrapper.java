package me.kiriyaga.essentials.event;

public class ListenerWrapper<T extends Event> implements Comparable<ListenerWrapper<?>> {
    private final EventListener<T> listener;
    private final EventPriority priority;

    public ListenerWrapper(EventListener<T> listener, EventPriority priority) {
        this.listener = listener;
        this.priority = priority;
    }

    public void invoke(T event) {
        listener.onEvent(event);
    }

    public EventPriority getPriority() {
        return priority;
    }

    @Override
    public int compareTo(ListenerWrapper<?> o) {
        return o.priority.ordinal() - this.priority.ordinal();
    }
}
