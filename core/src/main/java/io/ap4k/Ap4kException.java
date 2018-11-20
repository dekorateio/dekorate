package io.ap4k;

public class Ap4kException extends RuntimeException {
  public Ap4kException() {
  }

  public Ap4kException(Throwable cause) {
    super(cause);
  }

  public Ap4kException(String message, Throwable cause) {
    super(message, cause);
  }

  public static RuntimeException launderThrowable(Throwable cause) {
    return launderThrowable(cause.getMessage(), cause);
  }
  public static RuntimeException launderThrowable(String message, Throwable cause) {
    if (cause instanceof RuntimeException) {
      return ((RuntimeException) cause);
    } else if (cause instanceof Error) {
      throw ((Error) cause);
    } else if (cause instanceof InterruptedException) {
      Thread.currentThread().interrupt();
    }
    return new Ap4kException(message, cause);
  }
}
