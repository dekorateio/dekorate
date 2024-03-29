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
 */
package io.dekorate.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

public class Packaging {

  private static final String DEFAULT_DOCKERFILE = "Dockerfile";
  private static final String DOCKER_IGNORE = ".dockerignore";

  protected static final String DEFAULT_TEMP_DIR = System.getProperty("java.io.tmpdir", "/tmp");
  protected static final String DOCKER_PREFIX = "docker-";
  protected static final String BZIP2_SUFFIX = ".tar.bzip2";

  private static final Charset UTF_8 = Charset.forName("UTF-8");

  /**
   * Packages the content of the path as a tarball
   * 
   * @param path The path of the file or directory to package.
   * @return a file pointing to the generated tar.
   */
  @Deprecated
  public static File packageFile(String path) {
    return packageFile(path, (String) null);
  }

  /**
   * Packages the content of the path as a tarball
   * 
   * @param path The path of the file or directory to package.
   * @param destination The destination root in the tarball.
   * @return a file pointing to the generated tar.
   */
  @Deprecated
  public static File packageFile(String path, String destination) {
    return packageFile(Paths.get(path).getParent(), destination);
  }

  /**
   * Packages the content of the path as a tarball
   * 
   * @param path The path of the file or directory to package.
   * @param additional Additional entries to add to the tarball.
   * @return a file pointing to the generated tar.
   */
  public static File packageFile(Path root, Path... additional) {
    return packageFile(root, (String) null, additional);
  }

  /**
   * Packages the content of the path as a tarball
   * 
   * @param path The path of the file or directory to package.
   * @param destination The destination root in the tarball.
   * @param additional Additional entries to add to the tarball.
   * @return a file pointing to the generated tar.
   */
  public static File packageFile(Path root, String destination, Path... additional) {
    try {
      final Set<String> includes = Arrays
          .stream(additional)
          .map(p -> p.toAbsolutePath().toString())
          .collect(Collectors.toSet());

      File tempFile = Files.createTempFile(Paths.get(DEFAULT_TEMP_DIR), DOCKER_PREFIX, BZIP2_SUFFIX).toFile();
      try (final TarArchiveOutputStream tout = Packaging.buildTarStream(tempFile)) {
        tout.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        tout.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            String absolutePath = file.toAbsolutePath().toString();
            if (!shouldInclude(absolutePath, includes)) {
              return FileVisitResult.CONTINUE;
            }
            final Path relativePath = root.relativize(file);
            final boolean hasDestinationPath = Strings.isNotNullOrEmpty(destination);
            final TarArchiveEntry entry = hasDestinationPath
                ? new TarArchiveEntry(destination + File.separator + file.toFile())
                : new TarArchiveEntry(file.toFile());
            entry.setName(hasDestinationPath ? destination + File.separator + relativePath.toString()
                : relativePath.toString());
            entry.setMode(TarArchiveEntry.DEFAULT_FILE_MODE);
            if (!file.toFile().isDirectory() && file.toFile().canExecute()) {
              entry.setMode(entry.getMode() | 0755);
            }
            entry.setSize(attrs.size());
            Packaging.putTarEntry(tout, entry, file);
            return FileVisitResult.CONTINUE;
          }
        });
        tout.flush();
      }
      return tempFile;

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void putTarEntry(TarArchiveOutputStream tarArchiveOutputStream, TarArchiveEntry tarArchiveEntry,
      Path inputPath) throws IOException {
    tarArchiveEntry.setSize(Files.size(inputPath));
    tarArchiveOutputStream.putArchiveEntry(tarArchiveEntry);
    Files.copy(inputPath, tarArchiveOutputStream);
    tarArchiveOutputStream.closeArchiveEntry();
  }

  public static TarArchiveOutputStream buildTarStream(File outputPath) throws IOException {
    FileOutputStream fout = new FileOutputStream(outputPath);
    BufferedOutputStream bout = new BufferedOutputStream(fout);
    //BZip2CompressorOutputStream bzout = new BZip2CompressorOutputStream(bout);
    TarArchiveOutputStream stream = new TarArchiveOutputStream(bout);
    stream.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
    stream.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);

    return stream;
  }

  public static void tar(Path inputPath, Path outputPath) throws IOException {
    if (!Files.exists(inputPath)) {
      throw new FileNotFoundException("File not found " + inputPath);
    }

    try (TarArchiveOutputStream tarArchiveOutputStream = buildTarStream(outputPath.toFile())) {
      if (!Files.isDirectory(inputPath)) {
        TarArchiveEntry tarEntry = new TarArchiveEntry(inputPath.toFile().getName());
        if (inputPath.toFile().canExecute()) {
          tarEntry.setMode(tarEntry.getMode() | 0755);
        }
        putTarEntry(tarArchiveOutputStream, tarEntry, inputPath);
      } else {
        Files.walkFileTree(inputPath,
            new TarDirWalker(inputPath, tarArchiveOutputStream));
      }
      tarArchiveOutputStream.flush();
    }
  }

  private static boolean shouldInclude(String candidate, String path) {
    return candidate.equals(path) || candidate.startsWith(path);
  }

  private static boolean shouldInclude(String candidate, Set<String> paths) {
    for (String path : paths) {
      if (shouldInclude(candidate, path)) {
        return true;
      }
    }
    return false;
  }

  public static class TarDirWalker extends SimpleFileVisitor<Path> {
    private Path basePath;
    private TarArchiveOutputStream tarArchiveOutputStream;

    public TarDirWalker(Path basePath, TarArchiveOutputStream tarArchiveOutputStream) {
      this.basePath = basePath;
      this.tarArchiveOutputStream = tarArchiveOutputStream;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
      if (!dir.equals(basePath)) {
        tarArchiveOutputStream.putArchiveEntry(new TarArchiveEntry(basePath.relativize(dir).toFile()));
        tarArchiveOutputStream.closeArchiveEntry();
      }
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      TarArchiveEntry tarEntry = new TarArchiveEntry(basePath.relativize(file).toFile());
      tarEntry.setSize(attrs.size());
      if (!file.toFile().isDirectory() && file.toFile().canExecute()) {
        tarEntry.setMode(tarEntry.getMode() | 0755);
      }
      Packaging.putTarEntry(tarArchiveOutputStream, tarEntry, file);
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
      tarArchiveOutputStream.close();
      throw exc;
    }
  }

}
