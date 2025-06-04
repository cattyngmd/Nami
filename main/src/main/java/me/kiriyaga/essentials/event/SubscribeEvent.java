package me.kiriyaga.essentials.event;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubscribeEvent {
    EventPriority priority() default EventPriority.NORMAL;
}
