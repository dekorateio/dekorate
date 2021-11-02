/*
 * Copyright 2016-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.dekorate.example;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.isEmptyString;

import java.io.IOException;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import io.dekorate.testing.annotation.Inject;
import io.dekorate.testing.openshift.annotation.OpenshiftIntegrationTest;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.restassured.http.ContentType;

@OpenshiftIntegrationTest(additionalResources = "src/test/resources/database.yml")
public class SpringBootOnOpenShiftIT {

    private static final String FRUITS_PATH = "api/fruits";

    @Inject
    KubernetesClient client;

    @Inject
    Pod pod;

    @Test
    public void testPostGetAndDelete() throws IOException {
        try (LocalPortForward p = client.pods().withName(pod.getMetadata().getName()).portForward(8080)) {
            String url = "http://localhost:" + p.getLocalPort() + "/";
            Integer id = given()
                    .baseUri(url)
                    .contentType(ContentType.JSON)
                    .body(Collections.singletonMap("name", "Lemon"))
                    .post(FRUITS_PATH)
                    .then()
                    .statusCode(201)
                    .body("id", not(isEmptyString()))
                    .body("name", is("Lemon"))
                    .extract()
                    .response()
                    .path("id");

            given()
                    .baseUri(url)
                    .get(String.format("%s/%d", FRUITS_PATH, id))
                    .then()
                    .statusCode(200)
                    .body("id", is(id))
                    .body("name", is("Lemon"));

            given()
                    .baseUri(url)
                    .delete(String.format("%s/%d", FRUITS_PATH, id))
                    .then()
                    .statusCode(204);
        }

    }

}

