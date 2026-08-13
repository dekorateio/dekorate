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

package io.dekorate.kubernetes.doc;

import io.sundr.builder.annotations.Pojo;
import io.sundr.model.Attributeable;
import io.sundr.transform.annotations.AnnotationSelector;
import io.sundr.transform.annotations.TemplateTransformation;
import io.sundr.transform.annotations.TemplateTransformations;

@TemplateTransformations(value = @TemplateTransformation(value = "/annotation-doc.vm", outputPath = "annotation-table.org", gather = true), annotations = {
    @AnnotationSelector(value = Pojo.class)
})
public class KindAnnotationsTableGenerator {

  private Object DEFAULT_VALUE = Attributeable.DEFAULT_VALUE;
}
