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


package io.ap4k.examples.openshift;

import io.ap4k.kubernetes.annotation.Port;
import io.ap4k.openshift.annotation.EnableS2iBuild;
import io.ap4k.openshift.annotation.OpenshiftApplication;

@OpenshiftApplication(
  name = Main.APP_NAME,
  ports = {@Port(name = Main.PORT_NAME, containerPort = Main.PORT_NB)},
  expose = true)
@EnableS2iBuild
public class Main {
  static final String APP_NAME = "build-route-it";
  static final int PORT_NB = 8080;
  static final String PORT_NAME = "http";

  public static void main(String[] args) {
  }

}
