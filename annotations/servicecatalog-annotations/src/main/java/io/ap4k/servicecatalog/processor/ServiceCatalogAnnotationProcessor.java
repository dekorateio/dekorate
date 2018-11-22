package io.ap4k.servicecatalog.processor;


import io.ap4k.Session;
import io.ap4k.config.ConfigurationSupplier;
import io.ap4k.processor.AbstractAnnotationProcessor;
import io.ap4k.servicecatalog.ServiceCatalogGenerator;
import io.ap4k.servicecatalog.annotation.ServiceCatalog;
import io.ap4k.servicecatalog.annotation.ServiceCatalogInstance;
import io.ap4k.servicecatalog.config.ServiceCatalogConfig;
import io.ap4k.servicecatalog.config.ServiceCatalogConfigAdapter;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedAnnotationTypes({
  "io.ap4k.servicecatalog.annotation.ServiceCatalog",
  "io.ap4k.servicecatalog.annotation.ServiceCatalogInstance"
})
public class ServiceCatalogAnnotationProcessor extends AbstractAnnotationProcessor<ServiceCatalogConfig> {

  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Session session = Session.getSession();
    if  (roundEnv.processingOver()) {
      session.onClose(s -> write(s));
      return true;
    }
    for (TypeElement typeElement : annotations) {
      for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
        session.configurations().add(configuration(mainClass));
        session.generators().add(new ServiceCatalogGenerator(session.resources()));
      }
    }
    return false;
  }

  @Override
  public ConfigurationSupplier<ServiceCatalogConfig> configuration(Element mainClass) {
    ServiceCatalog serviceCatalog = mainClass.getAnnotation(ServiceCatalog.class);
    return serviceCatalog != null
      ? new ConfigurationSupplier<ServiceCatalogConfig>(ServiceCatalogConfigAdapter.newBuilder(serviceCatalog))
      : new ConfigurationSupplier<ServiceCatalogConfig>(ServiceCatalogConfigAdapter.newServiceCatalogBuilder());
  }
}
