package io.ap4k.docker.processor;

import io.ap4k.Session;
import io.ap4k.config.ConfigurationSupplier;
import io.ap4k.docker.adapter.DockerBuildConfigAdapter;
import io.ap4k.docker.annotation.DockerBuild;
import io.ap4k.docker.config.DockerBuildConfig;
import io.ap4k.docker.hook.DockerBuildHook;
import io.ap4k.docker.visitor.ApplyHookConfig;
import io.ap4k.docker.visitor.ApplyProjectInfoToDockerBuildConfig;
import io.ap4k.processor.AbstractAnnotationProcessor;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Optional;
import java.util.Set;

@SupportedAnnotationTypes("io.ap4k.docker.annotation.DockerBuild")
public class DockerBuildAnnotationProcessor extends AbstractAnnotationProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Session session = Session.getSession();
        if  (roundEnv.processingOver()) {
            session.onClose(r -> write(r));
            Optional<DockerBuildConfig> config = session.configurators().get(DockerBuildConfig.class);
            if (config.isPresent() && config.get().isAutoBuildEnabled()) {
               DockerBuildHook hook = new DockerBuildHook(project, config.get());
               hook.register();
            }
            return true;
        }
        for (TypeElement typeElement : annotations) {
            for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
              session.configurators().add(configuration(mainClass));
            }
        }
        return false;
  }

  @Override
  public ConfigurationSupplier<DockerBuildConfig> configuration(Element mainClass) {
    DockerBuild dockerBuild = mainClass.getAnnotation(DockerBuild.class);
    return new ConfigurationSupplier<DockerBuildConfig>(DockerBuildConfigAdapter
      .newBuilder(dockerBuild)
      .accept(new ApplyProjectInfoToDockerBuildConfig(project))
      .accept(new ApplyHookConfig()));
  }

}
