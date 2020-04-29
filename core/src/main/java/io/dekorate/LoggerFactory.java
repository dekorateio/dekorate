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

package io.dekorate;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import io.dekorate.logger.AnsiLogger;

public abstract class LoggerFactory<C> {

  private static Logger LOGGER;

  public Class<C> getType()  {
    ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
    Type[] types = type.getActualTypeArguments();
    return (Class<C>) types[0];
  }

  protected abstract Logger create(C context);

  public static synchronized void setLogger(Logger logger) {
    LOGGER = logger;
  }
  
  public static Logger getLogger() {
    if (LOGGER != null) {
      return LOGGER;
    }
    synchronized (LoggerFactory.class) {
      if (LOGGER == null) {
        LOGGER = new AnsiLogger();
      }
    }
    return LOGGER;
  }

  public static <C> Logger getLogger(C context) {
    if (LOGGER != null) {
      return LOGGER;
    }
    synchronized (LoggerFactory.class) {
      if (LOGGER == null) {
        ServiceLoader<LoggerFactory> loader = ServiceLoader.load(LoggerFactory.class, LoggerFactory.class.getClassLoader());
        LOGGER = StreamSupport.stream(loader.spliterator(), false)
          .filter(l->l.getType().isAssignableFrom(context.getClass()))
          .findFirst()
          .orElseThrow(()->new IllegalStateException("Could not find LoggerFactory that supports: "+ context.getClass()+"."))
          .create(context);
      }
    }
    return LOGGER;
  }
}
