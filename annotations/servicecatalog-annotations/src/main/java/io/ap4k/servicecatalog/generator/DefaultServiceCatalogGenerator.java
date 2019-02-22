package io.ap4k.servicecatalog.generator;

import io.ap4k.servicecatalog.annotation.ServiceCatalog;
import io.ap4k.servicecatalog.annotation.ServiceCatalogInstance;

import java.util.Arrays;
import java.util.List;

public class DefaultServiceCatalogGenerator implements ServiceCatalogGenerator {

    @Override
    public List<Class> getSupportedAnnotations() {
        return Arrays.asList(ServiceCatalog.class, ServiceCatalogInstance.class);
    }
}
