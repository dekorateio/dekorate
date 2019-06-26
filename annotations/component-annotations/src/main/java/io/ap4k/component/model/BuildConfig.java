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
package io.ap4k.component.model;

import io.ap4k.deps.kubernetes.api.model.Doneable;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Inline;

import java.nio.file.Path;
import java.nio.file.Paths;

@Buildable(editableEnabled = false, builderPackage = "io.ap4k.deps.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
public class BuildConfig {
    private String type = "s2i";
    private String uri;
    private String ref = "master";
    private String moduleDirName = ".";

  public BuildConfig() {
  }

  public BuildConfig(String type, String uri, String ref, String moduleDirName) {
    this.type = type;
    this.uri = uri;
    this.ref = ref;
    this.moduleDirName = moduleDirName;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getRef() {
    return ref;
  }

  public void setRef(String ref) {
    this.ref = ref;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getModuleDirName() {
    return moduleDirName;
  }

  public void setModuleDirName(String moduleDirName) {
    this.moduleDirName = moduleDirName;
  }
}
