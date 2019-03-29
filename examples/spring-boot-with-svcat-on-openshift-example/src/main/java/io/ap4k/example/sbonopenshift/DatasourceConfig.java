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
 *
**/
package io.ap4k.example.sbonopenshift;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatasourceConfig {


  @Value("${DB_USER}")
  private String username;
  @Value("${DB_PASSWORD}")
  private String password;
  @Value("${DB_NAME}")
  private String databaseName;
  @Value("${DB_HOST}")
  private String host;
  @Value("${DB_PORT}")
  private String port;

  @Bean
  DataSource create() {
    return DataSourceBuilder.create()
      .username(username)
      .password(password)
      .url("jdbc:postgresql://" + host + ":" + port +  "/" + databaseName)
      .driverClassName("org.postgresql.Driver")
      .build();
  }
}
