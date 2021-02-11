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

import io.sundr.codegen.annotations.PackageSelector;
import io.sundr.transform.annotations.VelocityTransformation;
import io.sundr.transform.annotations.VelocityTransformations;

@VelocityTransformations(value = @VelocityTransformation(value = "/processor-doc.vm", outputPath = "processors-table.org", gather = true), packages = {
    @PackageSelector(value = "io.dekorate.processor"),
    @PackageSelector(value = "io.dekorate.kubernetes.processor"),
    @PackageSelector(value = "io.dekorate.openshift.processor"),
    @PackageSelector(value = "io.dekorate.spring.processor"),
    @PackageSelector(value = "io.dekorate.thorntail"),
    @PackageSelector(value = "io.dekorate.micronaut")
})
public class DocumentProcessors {

}
