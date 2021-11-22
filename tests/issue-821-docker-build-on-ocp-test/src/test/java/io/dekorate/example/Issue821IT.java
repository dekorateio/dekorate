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

package io.dekorate.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;

import org.junit.jupiter.api.Test;

import io.dekorate.testing.annotation.Inject;
import io.dekorate.testing.openshift.annotation.OpenshiftIntegrationTest;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@OpenshiftIntegrationTest
public class Issue821IT {

  @Inject
  private URL appUrl;

  @Test
  public void shouldRespondWithHelloWorld() throws Exception {
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder().get().url(appUrl).build();
    Response response = client.newCall(request).execute();
    assertEquals("Hello world", response.body().string());
  }

}
