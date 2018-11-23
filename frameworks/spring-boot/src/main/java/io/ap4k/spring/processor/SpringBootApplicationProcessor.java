package io.ap4k.spring.processor;

import io.ap4k.Session;
import io.ap4k.processor.AbstractAnnotationProcessor;
import io.ap4k.spring.visitor.AddRuntimeToComponent;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedAnnotationTypes({"org.springframework.boot.autoconfigure.SpringBootApplication"})
public class SpringBootApplicationProcessor extends AbstractAnnotationProcessor {

  private static String RUNTIME_SPRING_BOOT = "spring-boot";

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Session session = Session.getSession();
    if  (roundEnv.processingOver()) {
      session.onClose(r -> write(r));

      session.configurations().accept(new AddRuntimeToComponent());
      return true;
    }
    return false;
  }
}
