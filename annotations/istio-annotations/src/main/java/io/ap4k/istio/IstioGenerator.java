package io.ap4k.istio;

import io.ap4k.Generator;
import io.ap4k.Resources;
import io.ap4k.annotation.ImagePullPolicy;
import io.ap4k.istio.config.IstioConfig;
import io.ap4k.visitor.ContainerVisitor;
import io.ap4k.visitor.VolumeMountVisitor;
import io.ap4k.visitor.VolumeVisitor;
import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;

public class IstioGenerator implements Generator<IstioConfig> {

    private static final String DEV_TERMINATION_LOG = "/dev/termination-log";
    private static final String FILE = "File";

    private static final String ISTIO_PROXY = "istio-proxy";
    private static final Long ISTIO_PROXY_USER = 1337L;

    private static final String ISTIO_SYSTEM = "istio-system";

    private static final String ISTIO_INIT = "istio-init";
    private static final String[] ISTIO_INIT_ARGS = {"-p", "15001", "-u", "1337"};

    private static final String ENABLE_CORE_DUMP = "enable-core-dump";
    private static final String[] CORE_DUMP_ARGS = {"-c", "sysctl -w kernel.core_pattern=/etc/istio/proxy/core.%e.%p.%t && ulimit -c unlimited"};


    private static final String POD_NAME = "POD_NAME";
    private static final String POD_NAMESPACE = "POD_NAMESPACE";
    private static final String INSTANCE_IP = "INSTANCE_IP";

    private static final String METADATA_NAME = "metadata.name";
    private static final String METADATA_NAMESPACE = "metadata.namespace";
    private static final String STATUS_PODIP = "status.podIp";

    private final Resources resources;

  public IstioGenerator(Resources resources) {
    this.resources = resources;
  }

  @Override
  public void generate(IstioConfig config) {
                    //Containers
                    resources.accept(createIstioInit(config));
                    resources.accept(createIstioProxy(config));
                    //Volumes
                    resources.accept(VolumeVisitor.createNew()
                            .withName("istio-envoy")
                            .withNewEmptyDir()
                            .withMedium("Memory")
                            .endEmptyDir());
                    resources.accept(VolumeVisitor.createNew()
                            .withName("istio-certs")
                            .withNewSecret()
                            .withSecretName("istio.default")
                            .withDefaultMode(420)
                            .endSecret());
                    //Mounts
                    resources.accept(VolumeMountVisitor.createNew()
                            .withName("istio-envoy")
                            .withMountPath("/etc/istio/proxy"));
                    resources.accept(VolumeMountVisitor.createNew()
                            .withName("istio-certs")
                            .withMountPath("/etc/certs"));
  }

  @Override
  public Class<? extends IstioConfig> getType() {
    return IstioConfig.class;
  }


    /**
     * Create a visitor that adds an istio proxy container to all pods.
     * @param config    The istio config.
     * @return          A visitor that adds the init container.
     */
    private TypedVisitor<PodSpecBuilder> createIstioProxy(IstioConfig config) {
        return ContainerVisitor.createNew()
                .withName(ISTIO_PROXY)
                .withImage(config.getProxyConfig().getProxyImage())
                .withArgs(IstioProxy.getArguments(config))
                .withTerminationMessagePath(DEV_TERMINATION_LOG)
                .addNewEnv().withName(POD_NAME).withNewValueFrom().withNewFieldRef(null, METADATA_NAME).endValueFrom().endEnv()
                .addNewEnv().withName(POD_NAMESPACE).withNewValueFrom().withNewFieldRef(null, METADATA_NAMESPACE).endValueFrom().endEnv()
                .addNewEnv().withName(INSTANCE_IP).withNewValueFrom().withNewFieldRef(null, STATUS_PODIP).endValueFrom().endEnv()
                .withNewSecurityContext()
                .withPrivileged(true)
                .withRunAsUser(ISTIO_PROXY_USER)
                .withReadOnlyRootFilesystem(false)
                .endSecurityContext()
                .done();
    }

    /**
     * Create a visitor that adds an istio init container to all pods.
     * @param config     The istio config.
     * @return          A visitor that adds the init container.
     */
    private TypedVisitor<PodSpecBuilder> createIstioInit(IstioConfig config) {
        return ContainerVisitor.createNewInit()
                .withName(ISTIO_INIT)
                .withImage(config.getProxyConfig().getInitImage())
                .withImagePullPolicy(ImagePullPolicy.IfNotPresent.name())
                .withTerminationMessagePath(DEV_TERMINATION_LOG)
                .withTerminationMessagePolicy(FILE)
                .withArgs("-p", "15001", "-u", "1337")
                .done();
    }
}
