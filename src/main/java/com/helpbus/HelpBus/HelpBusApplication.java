package com.helpbus.HelpBus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.helpbus.HelpBus")
public class HelpBusApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelpBusApplication.class, args);
	}

}
