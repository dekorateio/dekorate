package io.dekorate.issue275;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.dekorate.component.annotation.ComponentApplication;

@SpringBootApplication
@ComponentApplication(exposeService=true)
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
