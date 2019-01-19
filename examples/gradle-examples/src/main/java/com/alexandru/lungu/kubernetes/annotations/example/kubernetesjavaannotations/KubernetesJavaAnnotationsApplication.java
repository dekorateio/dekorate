package com.alexandru.lungu.kubernetes.annotations.example.kubernetesjavaannotations;

import io.ap4k.kubernetes.annotation.Env;
import io.ap4k.kubernetes.annotation.KubernetesApplication;
import io.ap4k.kubernetes.annotation.Port;
import io.ap4k.kubernetes.annotation.ServiceType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@KubernetesApplication(ports = @Port(name = "srv", hostPort = 8080, containerPort = 8080),
        serviceType = ServiceType.LoadBalancer,
        envVars = @Env(name = "key1", value = "value1"))
@SpringBootApplication
public class KubernetesJavaAnnotationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(KubernetesJavaAnnotationsApplication.class, args);
    }

}

