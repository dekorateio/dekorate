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

package io.dekorate.crd.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import io.sundr.builder.PathAwareTypedVisitor;
import io.sundr.builder.Predicate;
import io.sundr.codegen.model.AnnotationRefBuilder;
import io.sundr.codegen.model.ClassRef;
import io.sundr.codegen.model.PropertyBuilder;
import io.sundr.codegen.model.PropertyFluent;
import io.sundr.codegen.model.TypeDef;
import io.sundr.codegen.model.TypeDefFluent;
import io.sundr.codegen.model.TypeDefBuilder;

public class AnnotatedPathDetector extends PathAwareTypedVisitor<PropertyFluent<?>, TypeDefFluent<?>> {

  protected static final String DOT = ".";
  protected static final String STATUS = ".status.";

  private final String prefix;
  private final String annotationName;

  public AnnotatedPathDetector(String annotationName) {
    this(DOT, annotationName);
  }

  public AnnotatedPathDetector(String prefix, String annotationName) {
    this(prefix, annotationName, new ArrayList<>());
  }

  public AnnotatedPathDetector(String prefix, String annotationName, List<Object> path) {
    super(path);
    this.prefix = prefix;
    this.annotationName = annotationName;
  }

  public AnnotatedPathDetector(String prefix, String annotationName, List<Object> path, AnnotatedPathDetector detector) {
    super(path, detector);
    this.prefix = prefix;
    this.annotationName = annotationName;
  }

  private final AtomicReference<Optional<String>> path = new AtomicReference<>(Optional.empty());

  private final Predicate<AnnotationRefBuilder> predicate = new Predicate<>() {
        @Override
        public boolean apply(AnnotationRefBuilder ref) {
         return ref.getClassRef().getName().equals(annotationName);
        }
    };
 

	@Override
	public void visit(PropertyFluent<?> property) {
    System.out.println("Looking for " + annotationName + " in " + property.getTypeRef().toString() + " " + property.getName());
    if (property.hasMatchingAnnotation(predicate)) {
      path.set(Optional.of(getPath().stream().filter(i -> i instanceof PropertyBuilder).map(i -> ((PropertyBuilder)i).getName()).map(String::valueOf).collect(Collectors.joining(DOT))));
      System.out.println("Found @" + annotationName +" in:" + property.getTypeRef().toString() + " " + property.getName() + " full path:" + findPath().get());
    } else if (property.getTypeRef() instanceof ClassRef) {
      ClassRef classRef = (ClassRef) property.getTypeRef();
      if (classRef.getDefinition().getPackageName().startsWith("java") || classRef.getDefinition().getPackageName().startsWith("com.sun")) {
        return;
      }
      System.out.println("Looking for " + annotationName + " in nested class " + classRef.getFullyQualifiedName());
      TypeDef propertyDef = new TypeDefBuilder(classRef.getDefinition()).accept(next(classRef.getDefinition())).build();
    } 
	}
  
  @Override
  public PathAwareTypedVisitor<PropertyFluent<?>, TypeDefFluent<?>> next(Object item) {
     List<Object> path = new ArrayList<Object>(getPath());
     path.add(item);
     return new AnnotatedPathDetector(prefix, annotationName, path, this);
  }

  public Optional<String> findPath() {
    return path.get().map(p -> prefix + p);
  }


}
