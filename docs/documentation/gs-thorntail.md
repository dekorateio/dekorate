### Hello Thorntail

Add the following dependency to your project:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>thorntail-spring-starter</artifactId>
  <version>2.6.0</version>
</dependency>
```

That's all! Next time you perform a build, using something like:

    mvn clean package

The generated manifests can be found under `target/classes/META-INF/dekorate`.


![asciicast](images/dekorate-thorntail-hello-world.gif "Dekorate Thorntail Hello World Asciicast")

#### related examples
- [thorntail on kubernetes example](examples/thorntail-on-kubernetes-example)
- [thorntail on openshift example](examples/thorntail-on-openshift-example)
