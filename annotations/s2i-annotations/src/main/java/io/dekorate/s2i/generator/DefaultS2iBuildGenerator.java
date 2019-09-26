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
 * 
**/

package io.dekorate.s2i.generator;

import java.util.Collections;
import java.util.List;

import io.dekorate.Generator;
import io.dekorate.WithProject;
import io.dekorate.config.DefaultConfiguration;
import io.dekorate.kubernetes.configurator.ApplyBuild;
import io.dekorate.kubernetes.configurator.ApplyDeploy;
import io.dekorate.project.ApplyProjectInfo;
import io.dekorate.s2i.annotation.S2iBuild;
import io.dekorate.s2i.config.S2iBuildConfig;
import io.dekorate.s2i.config.S2iBuildConfigBuilder;

public class DefaultS2iBuildGenerator implements S2iBuildGenerator, WithProject {

    public static final String S2I = "s2i";
  
    public DefaultS2iBuildGenerator () {
        Generator.registerAnnotationClass(S2I, S2iBuild.class);
        Generator.registerGenerator(S2I, this);
        on(new DefaultConfiguration<S2iBuildConfig>(new S2iBuildConfigBuilder()
                                                        .accept(new ApplyProjectInfo(getProject()))
                                                        .accept(new ApplyBuild())
                                                        .accept(new ApplyDeploy())));
   }

    @Override
    public List<Class> getSupportedAnnotations() {
        return Collections.singletonList(S2iBuild.class);
    }

}
