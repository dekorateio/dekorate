/**
 * Copyright 2018 The original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dekorate.option.apt;

import io.dekorate.Session;
import io.dekorate.WithSession;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.doc.Description;
import io.dekorate.option.annotation.GeneratorOptions;
import io.dekorate.option.config.GeneratorConfig;
import io.dekorate.option.config.GeneratorConfigBuilder;
import io.dekorate.option.handler.GeneratorOptionsHandler;
import io.dekorate.processor.AbstractAnnotationProcessor;
import io.dekorate.utils.Strings;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Function;

@Description("Processing generator options, which are used for customizing the generation process")
@SupportedAnnotationTypes({"io.dekorate.annotation.Dekorate",
                           "io.dekorate.kubernetes.annotation.KubernetesApplication",
                           "io.dekorate.openshift.annotation.OpenshiftApplication",
                           "io.dekorate.knative.annotation.KnativeApplication",
                           "io.dekorate.option.annotation.GeneratorOptions"})
public class GeneratorOptionsProcessor extends AbstractAnnotationProcessor implements WithSession {

  private static final String INPUT_DIR = "dekorate.input.dir";
  private static final String OUTPUT_DIR = "dekorate.output.dir";

  private static final String FALLBACK_INPUT_DIR = "META-INF/fabric8";
  private static final String FALLBACK_OUTPUT_DIR = "META-INF/fabric8";

  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Session session = getSession();
    if (roundEnv.processingOver()) {
      session.close();
      return true;
    }

    for (TypeElement typeElement : annotations) {
      for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
        GeneratorOptions options = mainClass.getAnnotation(GeneratorOptions.class);
        if (options == null) {
          continue;
        }
        String inputPath = System.getProperty(INPUT_DIR, options.inputPath());
        String outputPath = System.getProperty(OUTPUT_DIR, options.outputPath());
        configurePaths(session, inputPath, outputPath);
        return false;
       }
    }
    // Let's check if fabric8 is being used.
    configurePaths(session, FALLBACK_INPUT_DIR, FALLBACK_OUTPUT_DIR);
    return false;
  }


  private void configurePaths(Session session, String inputPath, String outputPath) {
    if (Strings.isNotNullOrEmpty(inputPath) && resolve(getProject().getBuildInfo().getClassOutputDir(), inputPath).toFile().exists()) {
      applyToProject(p -> p.withDekorateInputDir(inputPath));
      session.configurators().add(new ConfigurationSupplier<GeneratorConfig>(new GeneratorConfigBuilder()));
      session.handlers().add(new GeneratorOptionsHandler(session.resources(), new ResourceReader(inputPath)));
    }
    if (Strings.isNotNullOrEmpty(inputPath) && resolve(getProject().getBuildInfo().getClassOutputDir(), outputPath).toFile().exists()) {
      applyToProject(p -> p.withDekorateOutputDir(outputPath));
    }
  }

  private Path resolve(Path path, String unixPath) {
    String[] dirs = unixPath.split("/");
    Path result = path;
    for (String dir : dirs) {
      result = result.resolve(dir);
    }
    return result;
  }

  /**
   * A Simple function for reading resources from class output.
   */
  private class ResourceReader implements Function<String, InputStream>  {
    private final String path;
    private ResourceReader(String path) {
      this.path = path;
    }

    @Override
    public InputStream apply(String resource) {
      try {
        FileObject fileObject = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", path + "/" + resource + DOT + YML);
        if (fileObject == null) {
          fileObject = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", path + "/" + resource + DOT + JSON);
        }
        return fileObject != null ? fileObject.openInputStream() : null;
      } catch (IOException e) {
        return null;
      }
    }
  }
}
