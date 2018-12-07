package io.ap4k.example.sbonopenshift

import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping

@RestController
class Controller {

  @RequestMapping("/")
   String hello() {
    "Hello world"
  }
}
