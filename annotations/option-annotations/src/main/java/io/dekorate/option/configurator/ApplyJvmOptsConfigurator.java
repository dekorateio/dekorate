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

package io.dekorate.option.configurator;

import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.kubernetes.config.Env;
import io.dekorate.kubernetes.config.BaseConfigFluent;
import io.dekorate.option.annotation.GarbageCollector;
import io.dekorate.option.annotation.SecureRandomSource;
import io.dekorate.option.config.JvmConfig;

import java.util.Arrays;
import java.util.Optional;
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
public class ApplyJvmOptsConfigurator extends Configurator<BaseConfigFluent<?>> {

  private static final String JAVA_OPTS = "JAVA_OPTS";

  private static final String EQ = "=";
  private static final String _ = " ";
  private static final String M = "M";

  private static final String XMS = "-Xms";
  private static final String XMX = "-Xmx";

  private static final String SECURITY_EDG = "-Djava.security.egd";

  //Flags
  private static final String SERVER = "-server";
  private static final String STRING_DEDUPLICATION = "-XX:+UseStringDeduplication";
  private static final String PREFER_IPV4_STACK = "-Djava.net.preferIPv4Stack";
  private static final String GC_OVERHEAD_LIMIT = "-XX:+UseGCOverheadLimit";
  private static final String HEAP_DUMP_ON_MEMORY_ERROR = "-XX:+HeapDumpOnOutOfMemoryError";

  private final ConfigurationSupplier<JvmConfig> config;

  public ApplyJvmOptsConfigurator(ConfigurationSupplier<JvmConfig> config) {
    this.config = config;
  }


  @Override
  public void visit(BaseConfigFluent<?> kubernetesConfig) {
    JvmConfig config = this.config.get();

    Optional<String> existing = Arrays.stream(kubernetesConfig.getEnvVars()).filter(e -> e.getName().equals(JAVA_OPTS)).map(Env::getValue)
      .findFirst();

    if (existing.isPresent()) {
      kubernetesConfig.editMatchingEnvVar(e -> e.getName().equals(JAVA_OPTS))
        .withValue(mergeOptions(existing.get(), config))
        .endEnvVar();
    } else {
      kubernetesConfig.addNewEnvVar()
        .withName(JAVA_OPTS)
        .withValue(mergeOptions("", config))
        .endEnvVar();
    }
  }

  private static String mergeOptions(String existing, JvmConfig config) {
    StringBuilder sb = new StringBuilder().append(existing);
    if (!existing.contains(XMS) && config.getXms() > 0) {
      sb.append(XMS).append(EQ).append(config.getXms()).append(M).append(_);
    }

    if (!existing.contains(XMX) && config.getXmx() > 0) {
      sb.append(XMX).append(EQ).append(config.getXmx()).append(M).append(_);
    }

    if (config.getGc() != GarbageCollector.Undefined && !existing.contains(config.getGc().getValue())) {
      sb.append(config.getGc().getValue()).append(_);
    }

    if (config.getSecureRandom() != SecureRandomSource.Undefined && !existing.contains(SECURITY_EDG)) {
      sb.append(SECURITY_EDG).append(EQ).append(config.getSecureRandom().getValue()).append(_);
    }

    //Handle Flags
    if (!existing.contains(SERVER) && config.isServer()) {
      sb.append(SERVER).append(_);
    }

    if (!existing.contains(STRING_DEDUPLICATION) && config.isUseStringDeduplication()) {
      sb.append(STRING_DEDUPLICATION).append(_);
    }

    if (!existing.contains(PREFER_IPV4_STACK) && config.isPreferIPv4Stack()) {
      sb.append(PREFER_IPV4_STACK).append(EQ).append(true).append(_);
    }

    if (!existing.contains(GC_OVERHEAD_LIMIT) && config.isUseGCOverheadLimit()) {
      sb.append(GC_OVERHEAD_LIMIT).append(_);
    }
    if (!existing.contains(HEAP_DUMP_ON_MEMORY_ERROR) && config.isHeapDumpOnOutOfMemoryError()) {
      sb.append(HEAP_DUMP_ON_MEMORY_ERROR).append(_);
    }
    return sb.toString().trim();
  }
}
