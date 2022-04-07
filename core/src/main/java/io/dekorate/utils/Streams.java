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

package io.dekorate.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import io.dekorate.DekorateException;

public final class Streams {

  private static final String DEKORATE = "dekorate";
  private static final String _TMP = ".tmp";

  private Streams() {
    //Utility
  }

  public static File createTemporaryFile(InputStream is) {
    try {
      Path tmp = Files.createTempFile(DEKORATE, _TMP);
      File f = tmp.toFile();
      try (BufferedInputStream bis = new BufferedInputStream(is); FileOutputStream fos = new FileOutputStream(f)) {
        byte[] buffer = new byte[8 * 1024];
        int size = 0;
        while ((size = bis.read(buffer)) > 0) {
          fos.write(buffer, 0, size);
        }
      }
      return f;
    } catch (IOException e) {
      throw DekorateException.launderThrowable(e);
    }
  }

  public static FileInputStream crateTempFileInputStream(InputStream is) {
    try {
      return new FileInputStream(createTemporaryFile(is));
    } catch (FileNotFoundException e) {
      throw DekorateException.launderThrowable(e);
    }
  }
}
