package io.dekorate.logger;

import io.dekorate.Logger;

/**
 * Used to suppress logging completely
 */
public class NoopLogger implements Logger {

  @Override
  public void debug(String message) {

  }

  @Override
  public void info(String message) {

  }

  @Override
  public void warning(String message) {

  }

  @Override
  public void error(String message) {

  }
}
