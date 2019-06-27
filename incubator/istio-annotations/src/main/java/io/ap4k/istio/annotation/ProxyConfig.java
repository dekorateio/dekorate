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
package io.ap4k.istio.annotation;

public @interface ProxyConfig {

  /**
   * The pseudo service name used for Envoy.
   * @return service cluster name.
   */
  String serviceCluster() default "istio-proxy";

  String initImage() default "istio.io/proxy_init:1.0.3";

  String proxyImage() default "istio.io/proxy:1.0.3";
  /**
   * Where should envoy's config be stored in the istio-proxy container.
   * @return The config path.
   */
  String configPath() default "/etc/istio/proxy";

  /**
   * Path to the envory binary.
   * @return The path.
   */
  String binaryPath() default "/usr/local/bin/envoy";

  /**
   * Set the following variable to true to disable policy checks by the Mixer.
   * Note that metrics will still be reported to the Mixer.\
   * @return True if disabled, otherwise false.
   */
  boolean disablePolicyChecks() default false;


  /**
   * Set enableTracing to false to disable request tracing.
   * @return True if enabled, false otherwise.
   */
  boolean enableTracing() default true;

  /**
   * Set accessLogFile to empty string to disable access log.
   * @return The path to the accessLogFile.
   */
  String accessLogFile() default "/dev/stdout";


  /**
   * Port where Envoy listens (on local host) for admin commands
   * You can exec into the istio-proxy container in a pod and
   * curl the admin port (curl http://localhost:15000/) to obtain
   * diagnostic information from Envoy. See
   * https://lyft.github.io/envoy/docs/operations/admin.html
   * for more details.
   * @return The proxy admin port.
   */
  int proxyAdminPort() default 15000;

  /**
   * If set to 0 (default), then start worker thread for each CPU thread/core.
   * @return the concurrency level.
   */
  int concurrency() default 0;

  /**
   * Zipkin trace collector.
   * @return The zipkin address.
   */
  String zipkinAddress() default "zipkin.istio-system:9411";

  /**
   * Statsd metrics collector converts statsd metrics into Prometheus metrics.
   * @return The statusd udp address.
   */
  String statsdUdpAddress() default "istio-statsd-prom-bridge.istio-system:9125";


  /**
   * Mutual TLS authentication between sidecars and istio control plane.\
   * @return The control plane auth policy.
   */
  String controlPlaneAuthPolicy() default "NONE";

  /**
   * Address where istio Pilot service is running.
   * @return The discovery address.
   */
  String discoveryAddress() default "istio-pilot.istio-system:15007";


  /**
   * Discovery refresh delay.
   * @return  The discovery refresh delay.
   */
  long discoveryRefershDelay() default 0;
  /**
   * These settings that determine how long an old Envoy
   * process should be kept alive after an occasional reload.
   * @return The drain duration.
   */
  long drainDuration() default 1L;

  /**
   * The parent shutdown duration (in minutes).
   * Defaults to one minute.
   * @return The parent shutdown duration.
   */
  long parentShutdownDuration() default 1L;


  long connectTimeout() default 0;
}
