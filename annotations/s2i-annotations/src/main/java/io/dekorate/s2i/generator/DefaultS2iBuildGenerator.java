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

import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.WithProject;
import io.dekorate.config.DefaultConfiguration;
import io.dekorate.kubernetes.configurator.ApplyBuild;
import io.dekorate.kubernetes.configurator.ApplyDeploy;
import io.dekorate.project.ApplyProjectInfo;
import io.dekorate.s2i.config.S2iBuildConfig;
import io.dekorate.s2i.config.S2iBuildConfigBuilder;

public class DefaultS2iBuildGenerator implements S2iBuildGenerator, WithProject {

    public Logger LOGGER = LoggerFactory.getLogger();
  
    public DefaultS2iBuildGenerator () {
        LOGGER.info("Default s2i build generator....");
        on(new DefaultConfiguration<S2iBuildConfig>(new S2iBuildConfigBuilder()
                                                        .accept(new ApplyProjectInfo(getProject()))
                                                        .accept(new ApplyBuild())
                                                        .accept(new ApplyDeploy())));
   }
}
