package com.fixhub.FixHub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.fixhub.FixHub")
public class FixHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(FixHubApplication.class, args);
	}

}
