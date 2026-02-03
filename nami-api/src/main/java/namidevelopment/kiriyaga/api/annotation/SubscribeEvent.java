package namidevelopment.kiriyaga.api.annotation;

import namidevelopment.kiriyaga.api.event.EventPriority;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubscribeEvent {
    EventPriority priority() default EventPriority.NORMAL;
}
