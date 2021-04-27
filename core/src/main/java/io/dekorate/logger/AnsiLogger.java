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

import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;


import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.fusesource.jansi.*;

import io.dekorate.Logger;
import io.dekorate.LoggerFactory;

public class AnsiLogger extends LoggerFactory<PrintStream> implements Logger {

  private PrintStream stream;

  public Logger create(PrintStream stream) {
    return new AnsiLogger(stream);
  }

  //Should not be used by user code. Only needed for Java SPI.
  public AnsiLogger() {
    this.stream = AnsiConsole.out;
  }

  public AnsiLogger(PrintStream stream) {
    check();
    this.stream = stream != null ? AnsiConsole.wrapPrintStream(stream, 0) : AnsiConsole.out;
  }

  @Override
  public void debug(String message) {
    if (isDebugEnabled()) {
      check();
      stream.println(ansi().a("[").fg(CYAN).bold().a(Level.DEBUG.name()).reset().a("] ").a(message));
    }
  }

  @Override
  public void info(String message) {
    if (isInfoEnabled()) {
      check();
      stream.println(ansi().a("[").fg(BLUE).bold().a(Level.INFO.name()).reset().a("] ").a(message));
    }
  }

  @Override
  public void warning(String message) {
    if (isWarnEnabled()) {
      check();
      stream.println(ansi().a("[").fg(MAGENTA).bold().a(Level.WARN.name()).reset().a("] ").a(message));
    }
  }

  @Override
  public void error(String message) {
    if (isErrorEnabled()) {
      check();
      stream.println(ansi().a("[").fg(RED).bold().a(Level.ERROR.name()).reset().a("] ").a(message));
    }
  }

  private void check() {
    if (stream == null) {
      stream = new PrintStream(new FileOutputStream(FileDescriptor.out));
      stream.println("Invalid AnsiLogger Stream -> Swapping to default sdt out logger.");
    }
  }

}
