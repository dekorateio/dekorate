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
package io.dekorate.doc;

import io.sundr.transform.annotations.PackageSelector;
import io.sundr.transform.annotations.TemplateTransformation;
import io.sundr.transform.annotations.TemplateTransformations;

@TemplateTransformations(value = @TemplateTransformation(value = "/configurator-doc.vm", outputPath = "configurators-table.org", gather = true), packages = {
    @PackageSelector(value = "io.dekorate.kubernetes.configurator"),
    @PackageSelector(value = "io.dekorate.openshift.configurator")
})
public class DocumentConfigurators {

}
