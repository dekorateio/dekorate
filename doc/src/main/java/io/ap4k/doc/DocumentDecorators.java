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
package io.ap4k.doc;

import io.sundr.transform.annotations.VelocityTransformation;
import io.sundr.transform.annotations.VelocityTransformations;
import io.sundr.codegen.annotations.PackageSelector;


@VelocityTransformations(value = @VelocityTransformation(value="/decorator-doc.vm", outputPath="decorators-table.org", gather=true),
                         packages = {
                           @PackageSelector(value="io.ap4k.kubernetes.decorator"),
                           @PackageSelector(value="io.ap4k.component.decorator")
                         })
public class DocumentDecorators {

}
