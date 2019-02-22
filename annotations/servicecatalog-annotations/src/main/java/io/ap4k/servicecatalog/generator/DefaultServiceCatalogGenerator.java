package io.ap4k.servicecatalog.generator;

import javax.annotation.processing.SupportedAnnotationTypes;

@SupportedAnnotationTypes({"io.ap4k.servicecatalog.annotation.ServiceCatalog", "io.ap4k.servicecatalog.annotation.ServiceCatalogInstance"})
public class DefaultServiceCatalogGenerator implements ServiceCatalogGenerator {
}
