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

package io.dekorate.logger;

import java.io.PrintStream;

import io.dekorate.Logger;
import io.dekorate.LoggerFactory;

public class PrintStreamLogger extends LoggerFactory<PrintStream> implements Logger {

  private final PrintStream stream;

  public Logger create(PrintStream stream) {
    return new PrintStreamLogger(stream);
  }

  //Should not be used by user code. Only needed for Java SPI.
  public PrintStreamLogger() {
    this.stream = null;
  }

  public PrintStreamLogger (PrintStream stream) {
    this.stream = stream;
    check();
  }
  
	@Override
	public void debug(String message) {
    check();
    stream.println(String.format(DEBUG, message));
	}

	@Override
	public void info(String message) {
    check();
    stream.println(String.format(INFO, message));
	}

	@Override
	public void warning(String message) {
    check();
    stream.println(String.format(WARN, message));
	}

	@Override
	public void error(String message) {
    check();
    stream.println(String.format(ERROR, message));
	}

  private void check() {
    if (stream == null) {
      throw new IllegalStateException("PrintStreamLogger requires a PrintStream instance.");
    }
  }
}
