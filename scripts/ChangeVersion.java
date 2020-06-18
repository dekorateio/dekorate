//usr/bin/env jbang "$0" "$@" ; exit $?
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;


public class ChangeVersion {

  public static void main(String[] args) {
    if (args.length < 2) {
      throw new RuntimeException("Usage: ChangeVersion.java <filename> <groupId> <version>");

    }
    String file = args[0];
    String groupId = args[1];
    String version = args[2];

    AtomicBoolean inDependency = new AtomicBoolean();
    AtomicBoolean inGroupId = new AtomicBoolean();

    AtomicBoolean inPath = new AtomicBoolean();

    try {
    Files.lines(Paths.get(file))
      .map(l -> {
        if (l.contains("<dependency>")) {
          inDependency.set(true);
        } else if (l.contains("</dependency>")) {
          inDependency.set(false);
          inGroupId.set(false);
        } else if (l.contains("<path>")) {
          inPath.set(true);
        } else if (l.contains("</path>")) {
          inPath.set(false);
          inGroupId.set(false);
        } else if ((inDependency.get() || inPath.get()) && l.contains("<groupId>") && l.contains(groupId)) {
          inGroupId.set(true);
        } else if ((inDependency.get() || inPath.get()) && l.contains("<groupId>") && !l.contains(groupId)) {
          inGroupId.set(false);
        } else if (inGroupId.get() && l.contains("<version>")) {
          String trimmed = l.replaceAll("[ ]*", "");
          return l.replace(trimmed, "<version>"+version+"</version>");
        } 
        return l;
      }).forEach(System.out::println);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
