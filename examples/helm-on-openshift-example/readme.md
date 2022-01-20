# Helm Example 

A very simple example that demonstrates the use of Helm deployments in its simplest form.
To access the Helm annotations or properties you just need to have the following dependency in your
class path:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>helm-annotations</artifactId>
      <version>${project.version}</version>
    </dependency>

Compile the project using:

    mvn clean install
    
You can find the generated Helm artifacts under: `target/classes/META-INF/dekorate/helm/<chart name>` that should look like:
- Chart.yaml
- values.yaml
- <chart name>-<chart version>-helmshift.tar.gz
- templates/*.yml the generated resources by Dekorate

How can we use it?

First, make sure you have installed [the Helm command line](https://helm.sh/docs/intro/install/) and have connected into a cluster.

Then, run the following Maven command in order to generate the Helm artifacts:

```shell
mvn clean package
```

Finally, let's use Helm to deploy it into the cluster:

```shell
helm install helm-example ./target/classes/META-INF/dekorate/helm/<chart name>
```

The above command will use the default values file `values.yaml`, we can select a different values file by doing:

```shell
helm install helm-example ./target/classes/META-INF/dekorate/helm/<chart name> --values ./target/classes/META-INF/dekorate/helm/<chart name>/values.dev.yaml
```

Now, we need to generate the image in OpenShift (s2i from binaries):

```shell
oc start-build helm-on-openshift-example --from-dir=target --follow=true --wait
```

How can we update my deployment?

- Via the `upgrade` option of Helm command line:

After doing changes in your project, you would need to regenerate the resources using Dekorate:

```shell
mvn clean package
```

And then you need to upgrade your deployment:

```shell
helm upgrade helm-example ./target/classes/META-INF/dekorate/helm/<chart name>
```

- Via the `set` option of Helm command line:

```shell
helm upgrade helm-example ./target/classes/META-INF/dekorate/helm/<chart name> --set helmOnOpenshiftExample.replicas=1
```

How can we delete my deployment?

```shell
helm uninstall helm-example
```
