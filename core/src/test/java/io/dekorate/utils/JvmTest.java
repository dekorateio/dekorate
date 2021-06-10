
package io.dekorate.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JvmTest {

  @Test
  public void shouldReturnVersionGreaterThan8() throws Exception {
    int version = Jvm.getVersion();
    assertTrue(version >= 8);
  }
}
