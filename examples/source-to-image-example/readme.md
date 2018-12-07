# Source To Image Example 

A very simple example that demonstrates how to use `@EnableS2iBuild` on top of `@OpenshiftApplication` to generate a `BuildConfig` and the required `ImageStreams`.
Check the [Main.java](src/main/java/io/ap4k/examples/openshift/Main.java) which bears the annotation.
To access the `@EnableS2iBuild` and `@OpenshiftApplication` annotation you just need to have the following dependency in your
class path:

    <dependency>
      <groupId>io.ap4k</groupId>
      <artifactId>openshift-annotations</artifactId>
      <version>${project.version}</version>
    </dependency>
    
Compile the project using:

    mvn clean install
    
You can find the generated deployment under: `target/classes/ap4k/openshfit.yml`.

The generated list should now contain the following item:

    ---
    - apiVersion: "image.openshift.io/v1"
      kind: "ImageStream"
      metadata:
        name: "s2i-java"
      spec:
        dockerImageRepository: "fabric8/s2i-java"
    - apiVersion: "image.openshift.io/v1"
      kind: "ImageStream"
      metadata:
        name: "source-to-image-example"
    - apiVersion: "build.openshift.io/v1"
      kind: "BuildConfig"
      metadata:
        name: "source-to-image-example"
      spec:
        output:
          to:
            kind: "ImageStreamTag"
            name: "source-to-image-example:1.0-SNAPSHOT"
        source:
          binary: {}
        strategy:
          sourceStrategy:
            from:
              kind: "ImageStreamTag"
              name: "s2i-java:2.3"
    
