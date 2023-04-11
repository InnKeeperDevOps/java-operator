package run.innkeeper.buses;

import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.reflections.scanners.Scanners;
import run.innkeeper.events.structure.Event;
import run.innkeeper.events.structure.Trigger;
import run.innkeeper.utilities.Logging;

import java.util.*;

public class EventBus {
    static EventBus singleton = new EventBus();

    public static EventBus get() {
        return singleton;
    }

    private Map<Class, Object> objects = new HashMap<>();
    private Map<String, Set<Method>> eventHandlers = new HashMap<>();

    public <T> void register() {
        Logging.debug("Registering methods");
        Reflections reflections = new Reflections("run.innkeeper.controllers", Scanners.MethodsAnnotated);
        reflections.getMethodsAnnotatedWith(Trigger.class).forEach(method -> {
            String eventName = method.getAnnotation(Trigger.class).value().getName();
            Logging.debug("Registering [" + method.getDeclaringClass().getSimpleName() + "] " + method.getName() + " to " + eventName);
            Set<Method> events = eventHandlers.get(eventName);
            if (events == null) {
                events = new HashSet<>();
                eventHandlers.put(eventName, events);
            }
            events.add(method);
            if (!objects.containsKey(method.getDeclaringClass())) {
                try {
                    objects.put(method.getDeclaringClass(), method.getDeclaringClass().getConstructor().newInstance());
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    private Set<Method> getEventHandlers(Event event) {
        return eventHandlers.getOrDefault(event.getClass().getName(), new HashSet<>());
    }

    public <T> void fire(Event event) {
        Map<Class<?>, Object> fireData = new HashMap<>() {{
            put(event.getClass(), event);
        }};
        Logging.debug("Triggered event " + event.getClass().getSimpleName());
        getEventHandlers(event).forEach(method -> {
            List<Object> objectList = new ArrayList<>();
            Class<?>[] types = method.getParameterTypes();
            for (int i = 0; i < types.length; i++) {
                objectList.add(fireData.getOrDefault(types[i], null));
            }

            Object obj = objects.get(method.getDeclaringClass());
            if (obj != null) {
                Logging.info("Invoking [" + obj.getClass().getSimpleName() + "] " + method.getName());
                try {
                    method.invoke(obj, objectList.toArray());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Logging.error("Object not found!");
            }

        });
    }

}
