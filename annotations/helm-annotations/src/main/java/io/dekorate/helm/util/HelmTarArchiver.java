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
package io.dekorate.helm.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

public final class HelmTarArchiver {
  public static File createTarBall(File outputFile, File inputDirectory, List<File> fileList, String compression,
      Consumer<TarArchiveEntry> tarArchiveEntryCustomizer) throws IOException {

    try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {

      final TarArchiveOutputStream tarArchiveOutputStream;
      if (isGzip(compression)) {
        tarArchiveOutputStream = new TarArchiveOutputStream(new GzipCompressorOutputStream(bufferedOutputStream));
      } else if (isBzip2(compression)) {
        tarArchiveOutputStream = new TarArchiveOutputStream(new BZip2CompressorOutputStream(bufferedOutputStream));
      } else {
        tarArchiveOutputStream = new TarArchiveOutputStream(bufferedOutputStream);
      }
      tarArchiveOutputStream.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
      tarArchiveOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
      for (File currentFile : fileList) {

        String relativeFilePath = inputDirectory.toURI().relativize(
            new File(currentFile.getAbsolutePath()).toURI()).getPath();

        final TarArchiveEntry tarEntry = new TarArchiveEntry(currentFile, relativeFilePath);
        tarEntry.setSize(currentFile.length());
        if (currentFile.isDirectory()) {
          tarEntry.setSize(0L);
          tarEntry.setMode(TarArchiveEntry.DEFAULT_DIR_MODE);
        }
        Optional.ofNullable(tarArchiveEntryCustomizer).ifPresent(tac -> tac.accept(tarEntry));
        tarArchiveOutputStream.putArchiveEntry(tarEntry);
        if (currentFile.isFile()) {
          try (InputStream fis = new FileInputStream(currentFile)) {
            IOUtils.copy(fis, tarArchiveOutputStream);
          }
        }
        tarArchiveOutputStream.closeArchiveEntry();
      }
      tarArchiveOutputStream.close();
    }

    return outputFile;
  }

  private static boolean isGzip(String compression) {
    return compression.equalsIgnoreCase("tar.gz") || compression.equalsIgnoreCase("tgz");
  }

  private static boolean isBzip2(String compression) {
    return compression.equalsIgnoreCase("tar.bz")
        || compression.equalsIgnoreCase("tar.bzip2")
        || compression.equalsIgnoreCase("tar.bz2");
  }
}
