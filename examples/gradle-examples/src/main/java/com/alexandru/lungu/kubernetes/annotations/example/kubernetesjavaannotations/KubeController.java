package com.alexandru.lungu.kubernetes.annotations.example.kubernetesjavaannotations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KubeController {
    @Value("${appName}")
    String appName;
    @GetMapping("/")
    public String getRoot() {
        return appName;
    }

}
