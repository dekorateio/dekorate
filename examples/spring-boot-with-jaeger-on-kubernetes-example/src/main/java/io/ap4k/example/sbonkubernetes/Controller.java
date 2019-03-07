package io.ap4k.example.sbonkubernetes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class Controller {

  @Autowired
  private RestTemplate restTemplate;


  @RequestMapping("/")
  public String chaining() {
    ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8080/hello", String.class);
    return "Chaining + " + response.getBody();
  }

  @RequestMapping("/hello")
  public String hello() {
    return "Hello from Spring Boot!";
  }
}
