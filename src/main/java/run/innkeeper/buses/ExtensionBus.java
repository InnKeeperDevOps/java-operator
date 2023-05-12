package run.innkeeper.buses;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import run.innkeeper.extensions.Extension;
import run.innkeeper.extensions.ExtensionStructure;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtension;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionState;


/**
 * The type Extension bus.
 */
public class ExtensionBus {
  private static ExtensionBus extensionBus = new ExtensionBus();
  private Map<String, ExtensionStructure> extensionHandlers = new HashMap<>();

  /**
   * Instantiates a new Extension bus.
   */
  public ExtensionBus() {

  }

  /**
   * Gets extension bus.
   *
   * @return the extension bus
   */
  public static ExtensionBus getExtensionBus() {
    return extensionBus;
  }

  /**
   * Init.
   */
  public void init() {
    Logging.debug("Registering extensions");
    Reflections reflections = new Reflections("run.innkeeper.extensions", Scanners.TypesAnnotated);
    reflections.getTypesAnnotatedWith(Extension.class).forEach(type -> {
      String extensionType = type.getAnnotation(Extension.class).value();
      Logging.debug(
          "Registering [" + type.getSimpleName() + "] " + type.getName() + " to " + extensionType
      );
      ExtensionStructure extension = extensionHandlers.get(extensionType);
      if (extension == null) {
        try {
          extensionHandlers
              .put(
                  extensionType,
                  (ExtensionStructure) type.getDeclaredConstructor().newInstance());
        } catch (InstantiationException
                 | InvocationTargetException
                 | IllegalAccessException
                 | NoSuchMethodException e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  /**
   * Get extension structure.
   *
   * @param event the event
   * @return the extension structure
   */
  public ExtensionStructure get(SimpleExtension event) {
    return extensionHandlers.get(event.getSpec().getType());
  }

  /**
   * Create simple extension state.
   *
   * @param event the event
   * @return the simple extension state
   */
  public SimpleExtensionState create(SimpleExtension event) {
    ExtensionStructure extensionStructure = get(event);
    if (extensionStructure != null) {
      return extensionStructure.create(event);
    }
    return null;
  }

  /**
   * Update simple extension state.
   *
   * @param event the event
   * @return the simple extension state
   */
  public SimpleExtensionState update(SimpleExtension event) {
    ExtensionStructure extensionStructure = get(event);
    if (extensionStructure != null) {
      return extensionStructure.update(event);
    }
    return null;
  }

  /**
   * Delete.
   *
   * @param event the event
   */
  public void delete(SimpleExtension event) {
    ExtensionStructure extensionStructure = get(event);
    if (extensionStructure != null) {
      extensionStructure.delete(event);
    }
  }

  /**
   * Check simple extension state.
   *
   * @param event the event
   * @return the simple extension state
   */
  public SimpleExtensionState check(SimpleExtension event) {
    ExtensionStructure extensionStructure = get(event);
    if (extensionStructure != null) {
      return extensionStructure.check(event);
    }
    return null;
  }
}
