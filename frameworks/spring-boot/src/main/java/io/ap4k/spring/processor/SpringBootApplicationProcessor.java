package io.ap4k.spring.processor;

import io.ap4k.Session;
import io.ap4k.processor.AbstractAnnotationProcessor;
import io.ap4k.spring.configurator.SetSpringBootRuntime;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedAnnotationTypes({"org.springframework.boot.autoconfigure.SpringBootApplication"})
public class SpringBootApplicationProcessor extends AbstractAnnotationProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Session session = Session.getSession();
    if  (roundEnv.processingOver()) {
      session.onClose(r -> write(r));

      session.configurators().add(new SetSpringBootRuntime());
      return true;
    }
    return false;
  }
}
