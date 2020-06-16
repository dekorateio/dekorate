
package io.dekorate.nodejs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;

import org.junit.jupiter.api.Test;

import io.dekorate.utils.Strings;

class NodeInfoReaderTest {

  @Test
  public void shouldReturnNpmVersionFromOutput() throws Exception {
    try (InputStream is = NodeInfoReader.class.getClassLoader().getResourceAsStream("npm/npm-version-output.json")) {
      String out = Strings.read(is);
      String result = NodeInfoReader.getVersionFromOutput(out);
      assertEquals("6.14.5", result);

    }
  }
}

