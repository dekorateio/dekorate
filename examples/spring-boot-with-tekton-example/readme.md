# Spring Boot with Tekton on Kubernetes 

The purpose of this example is to demonstrate the following:

- How you can use the tekton-annotations and the kubernetes-spring-starter modules.
- How you can end-to-end test the application.

The application is using:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>kubernetes-spring-starter</artifactId>
      <version>${project.version}</version>
    </dependency>

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>tekton-annotations</artifactId>
      <version>${project.version}</version>
    </dependency>
    
Which contains all the required modules, including the annotation processors that detect spring web applications.

The [Main.class](src/main/java/io/dekorate/example/Main.java) is annotated with `@KubernetesApplication` which triggers the resource generation.

The spring web application processor will detect our [Controller.java](src/main/java/io/dekorate/example/Controller.java), and will:

- add container port 8080
- expose port 8080 as a service
- add readiness and liveness probes.

## End-to-End testing

0. Set Up environment

This step will set up a Kubernetes cluster with Tekton installed. 

**Note:** If you have already logged in a Kubernetes environment, you can skip this step.

To use Tekton, it is needed to have a k8s cluster (>= 1.24) & local docker registry & the kind CLI installed (>= 0.17):
```bash
curl -s -L "https://raw.githubusercontent.com/snowdrop/k8s-infra/main/kind/kind.sh" | bash -s install --registry-name kind-registry.local
```

Next, let's install the release 0.39.0 of Tekton (or a specific release):
```bash
kubectl apply -f https://github.com/tektoncd/pipeline/releases/download/v0.39.0/release.yaml
```

1. Build the manifests:
```bash
mvn clean install -DskipTests -Ddekorate.docker.registry=quay.io -Ddekorate.tekton.use-local-docker-config-json=true -Ddekorate.tekton.projectBuilderArguments=clean,install,-Pwith-examples,-DskipTests,-Dformat.skip=true,-pl,examples/spring-boot-with-tekton-example,-am
```

Because we're going to use our private container registry `quay.io`, we need to configure Tekton to use our local docker config file, so Kubernetes can log into quay.io. 

Moreover, by default, the project will be built by Tekton using the command `mvn clean install`, however since this example is located at the Dekorate github repository, we need to turn on the Maven profile `with-examples` to also build the examples, and to skip the tests, so the build does not take much time to finish. The final property to configure the project builder is `-Ddekorate.tekton.projectBuilderArguments=clean,install,-Pwith-examples,-DskipTests,-Dformat.skip=true,-pl examples/spring-boot-with-tekton-example,-am`.

2. Run Tekton to build and start the application

There are two ways to do this: via pipelines and via tasks. We'll proceed with installing the app via tasks, which it's simpler procedure because Pipelines 
require to configure a `PersistentVolumeClaim` volume in order to share the workspace between steps.

We need first to install the Tekton tasks manifests:

```bash
kubectl apply -f target/classes/META-INF/dekorate/tekton-task.yml
```

And next, run the task workflow:
```bash
kubectl apply -f target/classes/META-INF/dekorate/tekton-task-run.yml
```

This workflow will download the sources, build the project and the image into your quay.io account and start the application. 

Let's wait until the pipeline is finished by doing:

```bash
kubectl wait --for=condition=Succeeded --timeout=800s TaskRun/spring-boot-with-tekton-example-run-now
```

**TIP:** You can see the logs of the workflow using `kubectl logs -f spring-boot-with-tekton-example-run-now-pod --all-containers --max-log-requests 10`.

After the Tekton task is finished, we can see our example is up and running using `kubectl get pods` and call our Hello World endpoint:

```bash
kubectl port-forward svc/spring-boot-with-tekton-example 8000:80
wget -qO- http://localhost:8000
> Hello world
```
