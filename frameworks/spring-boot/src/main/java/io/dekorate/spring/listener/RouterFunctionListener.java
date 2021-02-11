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

package io.dekorate.spring.listener;

import io.dekorate.ConfigurationRegistry;
import io.dekorate.Session;
import io.dekorate.spring.BeanListener;
import io.dekorate.spring.generator.SpringBootWebAnnotationGenerator;

public class RouterFunctionListener implements BeanListener, SpringBootWebAnnotationGenerator {

  @Override
  public String getType() {
    return "org.springframework.web.reactive.function.server.RouterFunction";
  }

  @Override
  public void onBean() {
    addPropertyConfiguration(WEB_ANNOTATIONS);
  }

  @Override
  public ConfigurationRegistry getConfigurationRegistry() {
    return Session.getSession().getConfigurationRegistry();
  }
}
