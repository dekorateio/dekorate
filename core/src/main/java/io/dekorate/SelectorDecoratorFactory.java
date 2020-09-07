
package io.dekorate;

import io.dekorate.kubernetes.decorator.NamedResourceDecorator;

/**
 * A factory for creating {@link SelectorDecorator} instances.
 */
public interface SelectorDecoratorFactory {

  String kind();

 <D extends NamedResourceDecorator<?>> D createAddToSelectorDecorator(String name, String key, String value); 

 <D extends NamedResourceDecorator<?>> D createRemoveFromSelectorDecorator(String name, String key); 
  
}
