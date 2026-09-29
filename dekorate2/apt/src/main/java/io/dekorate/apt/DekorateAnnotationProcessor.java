package io.dekorate.apt;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import io.dekorate.core.PropertiesConfigurationGenerator;
import io.dekorate.core.Session;

/**
 * Annotation processor that bootstraps a Dekorate2 {@link Session} at compile time.
 *
 * <p>
 * Two trigger modes (checked in order):
 * <ol>
 * <li><b>System property / env variable</b> — {@code -Ddekorate.enabled=true} or
 * {@code DEKORATE_ENABLED=true}. No annotation needed; the processor runs
 * with default config ({@code application.properties}).</li>
 * <li><b>{@link EnableDekorate} annotation</b> — placed on any class. The annotation's
 * {@code resources()} attribute selects which config files to read.</li>
 * </ol>
 *
 * <p>
 * This processor is intended for POC / standalone usage. Consuming projects
 * should provide their own processor that bootstraps the Session with
 * project-specific conventions.
 */
@SupportedAnnotationTypes("*")
public class DekorateAnnotationProcessor extends AbstractProcessor {

  private static final String PROP_ENABLED = "dekorate.enabled";
  private static final String ENV_ENABLED = "DEKORATE_ENABLED";
  private static final String PROP_CONFIG = "dekorate.config";
  private static final String DEFAULT_CONFIG = "application.properties";

  private Session session;
  private boolean bootstrapped;

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      if (session != null) {
        session.close();
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
            "[dekorate2] Manifests exported to " + session.getExportPath());
      }
      return false;
    }

    if (bootstrapped) {
      return false;
    }

    // Trigger 1: system property or env variable
    if (isEnabledByProperty()) {
      String configName = System.getProperty(PROP_CONFIG, DEFAULT_CONFIG);
      bootstrap(configName);
      return false;
    }

    // Trigger 2: @EnableDekorate annotation
    for (Element element : roundEnv.getElementsAnnotatedWith(EnableDekorate.class)) {
      EnableDekorate config = element.getAnnotation(EnableDekorate.class);
      if (config != null) {
        String configName = config.resources().length > 0 ? config.resources()[0] : DEFAULT_CONFIG;
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
            "[dekorate2] Found @EnableDekorate on " + element);
        bootstrap(configName);
        return false;
      }
    }

    return false;
  }

  private boolean isEnabledByProperty() {
    if ("true".equalsIgnoreCase(System.getProperty(PROP_ENABLED))) {
      return true;
    }
    String env = System.getenv(ENV_ENABLED);
    return "true".equalsIgnoreCase(env);
  }

  private void bootstrap(String configFileName) {
    bootstrapped = true;

    Path resourceDir = resolveResourceDir();
    Path configFile = resourceDir.resolve(configFileName);
    Path outputDir = resolveOutputDir();

    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
        "[dekorate2] Reading config from " + configFile);
    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
        "[dekorate2] Export path: " + outputDir);

    try {
      session = new Session(new PropertiesConfigurationGenerator(configFile));
      session.withExportPath(outputDir);
      session.load();
      session.start();
    } catch (Exception e) {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
          "[dekorate2] Failed to bootstrap session: " + e.getMessage());
    }
  }

  private Path resolveResourceDir() {
    try {
      FileObject probe = processingEnv.getFiler()
          .getResource(StandardLocation.CLASS_OUTPUT, "", "probe");
      // probe URI points to target/classes/probe → walk up to project root
      Path classOutput = Paths.get(probe.toUri()).getParent();
      return classOutput.getParent().getParent()
          .resolve("src").resolve("main").resolve("resources");
    } catch (Exception e) {
      return Paths.get("src/main/resources");
    }
  }

  private Path resolveOutputDir() {
    try {
      FileObject probe = processingEnv.getFiler()
          .getResource(StandardLocation.CLASS_OUTPUT, "", "probe");
      Path classOutput = Paths.get(probe.toUri()).getParent();
      return classOutput.resolve("META-INF").resolve("dekorate");
    } catch (Exception e) {
      return Paths.get("target/classes/META-INF/dekorate");
    }
  }
}
